# MONO Finance — Premium Dark Neo-Brutalist Expense & Wealth Tracker

MONO Finance is a high-craft Android personal finance application engineered around an unmistakable **Dark Neo-Brutalist + Acid Lime (`#D7FF00`)** aesthetic, pairing oversized tabular financial typography with spring-physics motion design, semantic Android haptics, interactive financial telemetry, and real-time Firebase Firestore cloud sync.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product and architectural choices reflect your confirmed preferences and are locked in for execution once you approve this plan:

- **Confirmed Application Replacement**: We will build the complete **MONO Finance** application replacing the existing project in the workspace, updating the app identity (`MONO`) and launcher assets while preserving the provisioned Firebase authentication and database configuration.
- **Confirmed Persistence & Backup Architecture**: Real-time cloud synchronization via **Firebase Firestore** (authenticated with Google Sign-In via Jetpack Credential Manager) paired with **one-tap local JSON backup export and restore** so your financial ledger is both cloud-synced and portable.
- **Confirmed Financial Suite Scope**:
  - **Core Ledger & Budgets**: Expense & income recording, category budgets with diagonal-stripe progress textures, and date-range filtering.
  - **Multi-Currency Engine**: Instant switching and formatting across major and custom currency symbols (`$`, `€`, `£`, `¥`, `₹`, `₩`, `CHF`, or custom symbol).
  - **Split Transactions & Receipt Notes**: Itemized bill splitting (track who owes what or split across categories) plus structured receipt notes and tags.
  - **Savings Goals**: Dedicated goal vaults with animated milestone progress, target dates, and one-tap contribution logging.
  - **Recurring Bills & Subscriptions**: Automated tracking of monthly/annual subscriptions and upcoming bill alerts with one-tap mark-as-paid.

---

## 1. Overview & Core Concept

- **What It Does**: MONO gives users complete command over cash flow, category budgets, recurring subscriptions, split expenses, and savings vaults through a tactile, zero-clutter interface where every tap, swipe, and balance update responds with calibrated spring physics and native haptics.
- **Target Audience / Persona**: Design-conscious individuals and power budgeters who want the speed of a native ledger with the visual authority of a modern fintech terminal.
- **Key Value**: Eliminates the friction and visual fatigue of traditional spreadsheet-like finance apps by turning daily expense logging, budget tracking, and cash-flow inspection into an instant, tactile 60–120Hz experience.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Dashboard & Live Net Balance**: Opens to an oversized tabular balance header with smooth number-interpolation counting, monthly burn-rate progress bar with diagonal acid-lime hazard/stripe geometry, quick-action triggers, upcoming recurring bills strip, and recent activity feed.
2. **Tactile Transaction Entry Sheet**: Slides up with a calibrated bottom-sheet spring (`280–400ms`), auto-focuses the oversized numeric keypad/input, supports instant Expense / Income / Split mode toggling, animated category pill selection with light haptic ticks, receipt note attachment, and a spring-loaded Save button that triggers a confirmed database write before playing a semantic success haptic and animated checkmark reveal.
3. **Interactive Analytics & Telemetry**: Smooth horizontal/shared-axis transition into interactive Bezier/bar cash-flow charts and category breakdown rings. Scrubbing or tapping data points triggers light haptic detents and floating inspection callouts. Swiping horizontally navigates between calendar months.
4. **History Ledger & Contextual Gestures**: Full searchable, filterable transaction stream supporting swipe-to-reveal actions (duplicate, split, delete with warning haptic), long-press contextual sheet, and tap-to-expand card details.
5. **Vaults, Budgets & Backup Control**: Dedicated management for category spending limits, savings goals with milestone celebrations, recurring subscription cadence, multi-currency formatting, reduced-motion override, and JSON backup/restore.

### Visual Identity & Theme
- **Aesthetic Direction**: High-Contrast Dark Neo-Brutalism — architectural sharp borders, exposed structural hairlines, diagonal stripe progress textures, and zero decorative fluff.
- **Color Palette & Mood**:
  - `--background`: Deep Carbon Black (`#070708`)
  - `--surface`: Elevated Obsidian (`#111114`)
  - `--surface-elevated`: Brutal Slate Card (`#18181D`)
  - `--primary` / `--accent`: Electric Acid Lime (`#D7FF00`)
  - `--on-primary`: Pure Ink Black (`#070708`)
  - `--text-primary`: Crisp Alabaster (`#F6F6F4`)
  - `--text-secondary`: Muted Zinc (`#8E8E98`)
  - `--semantic-income`: Acid Lime / Cyber Mint (`#D7FF00` / `#00F59B`)
  - `--semantic-warning`: High-Voltage Amber (`#FFB800`)
  - `--semantic-danger`: Signal Crimson (`#FF3B5C`)
  - `--border-structural`: Crisp 1.5dp–2dp structural framing (`#2A2A32` resting, `#D7FF00` active focus)
- **Typography & Hierarchy**:
  - **Display & Financial Numerals**: `Space Grotesk` & `JetBrains Mono` (`tabular-nums` alignment so animated counting never jitters horizontally) — Hero Balances at `44sp–52sp` Bold, Card Totals at `24sp–28sp`, Section Headers in uppercase tracked mono labels (`11sp–12sp`, letter-spacing `1.5sp`).
  - **Body & UI Copy**: `Plus Jakarta Sans` for crisp legibility across transaction merchants, notes, and split participants.
- **Interactive Feedback & Motion System**:
  - **Button Press**: `100–140ms` scale-down (`0.96f`) with spring release (`dampingRatio = 0.65f, stiffness = 600f`).
  - **Micro-Interactions & Switches**: `150–220ms` color and icon rotation/scale interpolation.
  - **Card Expansion & Layout Changes**: `250–350ms` spring bounds animation preserving scroll state.
  - **Screen & Sheet Transitions**: `300–450ms` directional slide + fade envelopes; `280–400ms` modal sheet physics.
  - **Financial Number Counting**: `500–800ms` `FastOutSlowInEasing` interpolated value counter that skips intermediate noise when reduced-motion is enabled.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Central Motion & Haptic Coordinator (`MonoMotion` & `MonoHaptics`)**
  - *Chosen Approach*: Centralize all animation specs, reduced-motion checks (`Settings.Global.ANIMATOR_DURATION_SCALE` + in-app toggle), and semantic Android `HapticFeedbackConstants` (`CLOCK_TICK`, `CONTEXT_CLICK`, `CONFIRM`, `REJECT`, `LONG_PRESS`) in dedicated utility engines.
  - *Why*: Guarantees uniform feel across every button, chart point, and sheet while respecting system accessibility and battery/performance constraints on 60Hz, 90Hz, and 120Hz displays.
  - *Alternatives Considered*: Ad-hoc per-composable `tween()` calls, which lead to inconsistent easing curves and ignored accessibility settings.

- **Decision 2: Cloud Firestore + Instant Local State & JSON Portability**
  - *Chosen Approach*: Store user profiles, transactions (including split items and receipt notes), category budgets, savings goals, and recurring bills under `/users/{userId}/...` in Firebase Firestore with real-time `snapshots()` flows, complemented by local JSON export/restore.
  - *Why*: Delivers durable multi-device cloud persistence without showing disruptive spinning loaders when cached snapshots are already in memory, and directly fulfills the backup/restore haptic and data portability flow.
  - *Alternatives Considered*: Local-only SQLite, which would lose data across device switches.

- **Decision 3: Custom Canvas Charts & Diagonal Stripe Progress Bars**
  - *Chosen Approach*: Build hardware-accelerated Jetpack Compose `Canvas` charts and diagonal-stripe progress indicators using `remember` and draw-phase reading rather than heavy third-party charting views.
  - *Why*: Prevents unnecessary recompositions during touch scrubbing and progress animations, maintaining locked 60–120 FPS performance.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        MAIN ACTIVITY & AUTH GATE                            │
│  • Edge-to-Edge System Bars (Deep Black #070708)                            │
│  • Google Sign-In via Jetpack CredentialManager                             │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Authenticated UID
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     MONO FINANCE VIEWMODEL (StateFlow)                      │
│  • Net Balance, Monthly Burn, Filtered Ledger, Chart Aggregations           │
│  • Multi-Currency Formatter, Budget Alerts, Backup/Restore Serializer       │
└───────┬──────────────────┬───────────────────┬──────────────────┬───────────┘
        │                  │                   │                  │
        ▼                  ▼                   ▼                  ▼
┌──────────────┐  ┌─────────────────┐  ┌───────────────┐  ┌───────────────────┐
│  DASHBOARD   │  │ ANALYTICS &     │  │ LEDGER &      │  │ VAULTS, BILLS &   │
│  SCREEN      │  │ TELEMETRY       │  │ GESTURE FEED  │  │ SETTINGS SCREEN   │
│ • Hero Count │  │ • Touch Canvas  │  │ • Swipe Reveal│  │ • Savings Goals   │
│ • Budget Bar │  │   Cashflow Chart│  │ • Card Expand │  │ • Recurring Bills │
│ • Quick Add  │  │ • Category Rings│  │ • Month Swipe │  │ • Currency & JSON │
│ • Shimmer UI │  │ • Point Haptics │  │ • Filter Chips│  │ • Reduced Motion  │
└──────┬───────┘  └────────┬────────┘  └───────┬───────┘  └─────────┬─────────┘
       │                   │                   │                    │
       └───────────────────┴─────────┬─────────┴────────────────────┘
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│              SHARED MOTION, HAPTIC & TRANSACTION SHEET ENGINE               │
│  • Add / Edit / Split Transaction Bottom Sheet (DB-confirmed Success Flow)  │
│  • MonoHaptics (Light / Medium / Success / Warning / Error native APIs)     │
│  • MonoMotion (Spring specs, AnimatedMoneyCounter, StripedProgressBar)      │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                MONO FIRESTORE REPOSITORY (/users/{userId}/...)              │
│  • profile/settings   • transactions   • budgets   • goals   • bills        │
│  • Hardened Zero-Trust Security Rules + Emulator Rule Tests                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **`MonoUserProfile`**: `userId`, `displayName`, `currencyCode` (`USD`, `EUR`, `GBP`, `JPY`, `INR`, `KRW`, `CUSTOM`), `customCurrencySymbol`, `monthlyBudgetCents`, `reducedMotionEnabled`, `hapticsEnabled`, `updatedAt`.
- **`MonoTransaction`**: `id`, `userId`, `title`, `amountCents`, `type` (`EXPENSE` | `INCOME`), `categoryId`, `categoryName`, `timestampMillis`, `receiptNote`, `isSplit`, `splitSummary` (JSON/serialized participant shares), `isRecurringInstance`, `createdAt`, `updatedAt`.
- **`MonoCategoryBudget`**: `id`, `userId`, `categoryId`, `categoryName`, `limitCents`, `alertThresholdPercent`, `createdAt`, `updatedAt`.
- **`MonoSavingsGoal`**: `id`, `userId`, `title`, `targetCents`, `savedCents`, `deadlineMillis`, `accentHex`, `isCompleted`, `createdAt`, `updatedAt`.
- **`MonoRecurringBill`**: `id`, `userId`, `title`, `amountCents`, `categoryId`, `cadence` (`MONTHLY` | `WEEKLY` | `YEARLY`), `nextDueDayOfMonth`, `isAutoRemind`, `lastPaidMonthKey`, `createdAt`, `updatedAt`.

### Interactive Component & State Mapping
- **Add / Split Transaction Modal**: Validates non-zero amount and category selection (triggers `ERROR` haptic + horizontal shake animation on invalid submit); awaits Firestore write completion before triggering `SUCCESS` haptic, animated lime checkmark, and dashboard counter roll.
- **Swipeable & Expandable Transaction Cards**: Horizontal drag reveals contextual `Split`, `Duplicate`, and `Delete` actions with threshold haptic detents; tapping expands inline to reveal full receipt notes, split breakdown, and timestamp metadata; accessible action buttons are also exposed inside the expanded detail view.
- **Interactive Canvas Charts**: Pointer input tracks horizontal drag/tap across daily/monthly data points, snapping to the nearest point with a `LIGHT` haptic tick and rendering an acid-lime crosshair + amount badge.
- **Budget & Goal Progress**: Exceeding 100% of a category or monthly budget triggers a `WARNING` haptic and shifts the diagonal stripe progress bar from `#D7FF00` to high-alert amber/crimson; reaching 100% of a savings goal triggers a `SUCCESS` haptic and milestone badge unlock.
