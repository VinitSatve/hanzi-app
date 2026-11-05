const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

function createValidProfilePayload(uid, serverTimestamp) {
  return {
    userId: uid,
    displayName: "Mei Lin",
    scriptMode: "SIMPLIFIED",
    hskStandard: "HSK_2",
    currentHskLevel: 1,
    dailyGoalMinutes: 20,
    studyMinutesToday: 15,
    totalStudyMinutes: 120,
    streakDays: 5,
    points: 250,
    completedLessonIds: ["pinyin_1", "hsk1_1"],
    masteredCharacterIds: ["char_ni", "char_hao"],
    unlockedBadgeIds: ["badge_first_lesson"],
    downloadedPackIds: ["pack_hsk1"],
    listeningAccuracy: 88,
    readingAccuracy: 92,
    speakingAccuracy: 85,
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

function createValidFlashcardPayload(uid, serverTimestamp) {
  return {
    userId: uid,
    simplified: "你好",
    traditional: "你好",
    pinyin: "nǐ hǎo",
    english: "Hello",
    partOfSpeech: "Greeting",
    exampleChinese: "你好，很高兴认识你。",
    examplePinyin: "Nǐ hǎo, hěn gāoxìng rènshí nǐ.",
    exampleEnglish: "Hello, nice to meet you.",
    hskLevel: 1,
    category: "Greetings",
    status: "LEARNING",
    intervalDays: 1,
    easeFactor: 250,
    repetitions: 1,
    nextReviewEpochDay: 20760,
    isFavorite: true,
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

function createValidNotePayload(uid, serverTimestamp) {
  return {
    userId: uid,
    title: "Using 是 (shì)",
    chineseSentence: "我是学生。",
    pinyin: "Wǒ shì xuéshēng.",
    englishTranslation: "I am a student.",
    grammarNotes: "A + 是 + B connects two nouns.",
    folder: "Grammar",
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

test("Unauthenticated user: cannot read or create user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read own profile, flashcards, and notes", async () => {
  const aliceCtx = testEnv.authenticatedContext(ALICE_UID);
  const aliceDb = aliceCtx.firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(createValidProfilePayload(ALICE_UID, now))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("flashcards")
      .doc("card_1")
      .set(createValidFlashcardPayload(ALICE_UID, now))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("notes")
      .doc("note_1")
      .set(createValidNotePayload(ALICE_UID, now))
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice's profile, flashcards, or notes", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(ALICE_UID).set(createValidProfilePayload(ALICE_UID, now));
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("flashcards")
      .doc("card_1")
      .set(createValidFlashcardPayload(ALICE_UID, now));
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("notes")
      .doc("note_1")
      .set(createValidNotePayload(ALICE_UID, now));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("flashcards").doc("card_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").get()
  );
});

test("Shadow Update & Value Poisoning: rejects ghost fields and invalid enum values", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  // Ghost field rejected by hasOnly
  const ghostPayload = {
    ...createValidProfilePayload(ALICE_UID, now),
    isVerifiedAdmin: true,
  };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(ghostPayload));

  // Invalid scriptMode rejected by isValidUserProfile
  const badEnumPayload = {
    ...createValidProfilePayload(ALICE_UID, now),
    scriptMode: "INVALID_MODE",
  };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badEnumPayload));
});
