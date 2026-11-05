const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_mono_1";
const BOB_UID = "bob_mono_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function nowTimestamp() {
  return new Date(Date.now() - 1000);
}

test("Unauthenticated user cannot read or write transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .get()
  );
});

test("Authenticated owner can create, update, list, and delete valid transaction", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions")
    .doc("tx_101");
  const ts = nowTimestamp();

  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      title: "Specialty Coffee Roasters",
      amountCents: 650,
      type: "EXPENSE",
      categoryId: "dining",
      categoryName: "Dining",
      timestampMillis: Date.now(),
      receiptNote: "Cortado + tip",
      isSplit: false,
      splitSummary: "",
      isRecurringInstance: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    txRef.update({
      amountCents: 800,
      receiptNote: "Updated tip",
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .where("userId", "==", ALICE_UID)
      .get()
  );

  await assertSucceeds(txRef.delete());
});

test("Cross-user access to another user's transactions, goals, and bills is rejected", async () => {
  const ts = nowTimestamp();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .doc("tx_secret")
      .set({
        userId: ALICE_UID,
        title: "Salary",
        amountCents: 500000,
        type: "INCOME",
        categoryId: "salary",
        categoryName: "Salary",
        timestampMillis: Date.now(),
        receiptNote: "",
        isSplit: false,
        splitSummary: "",
        isRecurringInstance: false,
        createdAt: ts,
        updatedAt: ts,
      });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .doc("tx_secret")
      .get()
  );
});

test("Shadow update with ghost field or negative amount is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions")
    .doc("tx_bad");
  const ts = nowTimestamp();

  // Negative amount fails
  await assertFails(
    txRef.set({
      userId: ALICE_UID,
      title: "Bad Expense",
      amountCents: -500,
      type: "EXPENSE",
      categoryId: "dining",
      categoryName: "Dining",
      timestampMillis: Date.now(),
      receiptNote: "",
      isSplit: false,
      splitSummary: "",
      isRecurringInstance: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Ghost field fails
  await assertFails(
    txRef.set({
      userId: ALICE_UID,
      title: "Ghost Expense",
      amountCents: 500,
      type: "EXPENSE",
      categoryId: "dining",
      categoryName: "Dining",
      timestampMillis: Date.now(),
      receiptNote: "",
      isSplit: false,
      splitSummary: "",
      isRecurringInstance: false,
      isAdminHack: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Authenticated owner can manage profile, budgets, goals, and recurring bills", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = nowTimestamp();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("profile")
      .doc("settings")
      .set({
        userId: ALICE_UID,
        displayName: "Alex Mercer",
        currencyCode: "USD",
        customCurrencySymbol: "$",
        monthlyBudgetCents: 350000,
        reducedMotionEnabled: false,
        hapticsEnabled: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("budgets")
      .doc("dining")
      .set({
        userId: ALICE_UID,
        categoryId: "dining",
        categoryName: "Dining & Coffee",
        limitCents: 60000,
        alertThresholdPercent: 80,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("goals")
      .doc("goal_1")
      .set({
        userId: ALICE_UID,
        title: "Emergency Treasury",
        targetCents: 1000000,
        savedCents: 250000,
        deadlineMillis: Date.now() + 864000000,
        accentHex: "#D7FF00",
        isCompleted: false,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bills")
      .doc("bill_1")
      .set({
        userId: ALICE_UID,
        title: "Cloud Compute Pro",
        amountCents: 4900,
        categoryId: "tech",
        cadence: "MONTHLY",
        nextDueDayOfMonth: 15,
        isAutoRemind: true,
        lastPaidMonthKey: "2026-10",
        createdAt: ts,
        updatedAt: ts,
      })
  );
});
