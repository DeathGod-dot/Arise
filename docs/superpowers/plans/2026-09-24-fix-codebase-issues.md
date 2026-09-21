# Fix Codebase Issues Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Resolve all 22 identified issues across build, security, concurrency, database, game engine logic, platform background restrictions, performance, and UI.

**Architecture:** Fix issues in logical dependency order: clean broken test files first to achieve green compilation, patch security/manifest, fix database converters and entities, eliminate coroutine/media player leaks, patch game engine calculations (RewardEngine, streak logic, worker), resolve Android 14+ alarm constraints, prevent bitmap OOMs, and fix UI theme/state retention bugs.

**Tech Stack:** Kotlin, Jetpack Compose, Room DB, Kotlinx Coroutines, AndroidX Lifecycle, WorkManager, Hilt, Firebase Auth & Firestore, Coil 3.

## Review Focus

1. Test compilation: Ensure `./gradlew testDebugUnitTest` compiles and passes.
2. Room database integrity: Converters must safely handle malformed or empty strings without crashing.
3. Streak & Reward math: RewardEngine level up while loop must never freeze, and Sunday rest day must preserve streaks.
4. Concurrency: `GlobalScope` eliminated; sync coroutine scopes properly managed with `SupervisorJob`.
5. Android platform compliance: Exact alarms must check `canScheduleExactAlarms()`.

---

### Task 1: Fix Build & Unit Test Compilation

**Files:**
- Delete: `app/src/test/java/com/example/arise/ui/main/MainScreenViewModelTest.kt`
- Delete: `app/src/androidTest/java/com/example/arise/ui/main/MainScreenTest.kt`
- Create: `app/src/test/java/com/example/arise/domain/RewardEngineTest.kt`

- [ ] **Step 1: Remove obsolete template tests**
- [ ] **Step 2: Add RewardEngineTest covering XP, Level Up, and Rank Up**
- [ ] **Step 3: Run `./gradlew testDebugUnitTest` and verify green build**

---

### Task 2: Security & Manifest Hardening

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/example/arise/ui/screens/AuthScreen.kt`

- [ ] **Step 1: Set `android:usesCleartextTraffic="false"` in AndroidManifest.xml**
- [ ] **Step 2: Define `default_web_client_id` in strings.xml and reference in AuthScreen**

---

### Task 3: Database Converters & Entity/DAO Completeness

**Files:**
- Modify: `app/src/main/java/com/example/arise/data/Converters.kt`
- Modify: `app/src/main/java/com/example/arise/data/Daos.kt`
- Modify: `app/src/main/java/com/example/arise/data/AriseDatabase.kt`
- Modify: `app/src/main/java/com/example/arise/di/AppModule.kt`

- [ ] **Step 1: Make `toAttributeRewardList` robust against malformed tokens**
- [ ] **Step 2: Remove redundant `fromLong` converter**
- [ ] **Step 3: Add `PenaltySessionDao` and wire to `AriseDatabase` & `AppModule`**

---

### Task 4: Coroutine Leaks & Service Cleanup

**Files:**
- Modify: `app/src/main/java/com/example/arise/service/StudyTimerService.kt`
- Modify: `app/src/main/java/com/example/arise/data/FirestoreSyncRepository.kt`
- Modify: `app/src/main/java/com/example/arise/service/AriseAlarmReceiver.kt`

- [ ] **Step 1: Replace `GlobalScope` in `StudyTimerService` with `serviceScope`**
- [ ] **Step 2: Add `SupervisorJob()` and cancel prior sync jobs in `FirestoreSyncRepository`**
- [ ] **Step 3: Safely release `MediaPlayer` on error and cleanup in `AriseAlarmReceiver`**

---

### Task 5: Game Engine & Logic Fixes

**Files:**
- Modify: `app/src/main/java/com/example/arise/domain/RewardEngine.kt`
- Modify: `app/src/main/java/com/example/arise/viewmodel/QuestsViewModel.kt`
- Modify: `app/src/main/java/com/example/arise/service/DailyReminderWorker.kt`

- [ ] **Step 1: Protect `RewardEngine` against zero/negative `currentXpToNext`**
- [ ] **Step 2: Fix Sunday streak calculation so skipping Sunday does not reset streak on Monday**
- [ ] **Step 3: Wire actual pending quest count into `DailyReminderWorker`**

---

### Task 6: Android 14+ (API 34+) Platform Requirements

**Files:**
- Modify: `app/src/main/java/com/example/arise/service/AlarmScheduler.kt`

- [ ] **Step 1: Add `canScheduleExactAlarms()` check on Build.VERSION_CODES.S+**
- [ ] **Step 2: Fall back to `setWindow()` or `setAndAllowWhileIdle()` when exact alarm permission is absent**

---

### Task 7: Memory (Bitmap OOM) & Quick Add UI

**Files:**
- Modify: `app/src/main/java/com/example/arise/ui/screens/QuestsScreen.kt`

- [ ] **Step 1: Replace `ImageDecoder.decodeBitmap` / `MediaStore.Images.Media.getBitmap` with Coil `AsyncImage` for workout proof preview**
- [ ] **Step 2: Fix text clipping in `QuickAddPill` on compact screens**

---

### Task 8: Light Theme Contrast in Settings Dialogs

**Files:**
- Modify: `app/src/main/java/com/example/arise/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Replace hardcoded `Color(0xFF090B10)` and `Color.White` with `MaterialTheme.colorScheme` tokens in all settings dialogs**
- [ ] **Step 2: Ensure dark & light modes both have crisp, legible contrast**
