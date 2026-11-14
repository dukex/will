# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## FIRST RULE: Let's discovery the project together.

Always update the CLAUDE.md every new discovery. The CLAUDE.md is a index to documentation at `./docs/` the documentation should be good or to Agents or to Humans.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Run a single test class (the aggregate `test` task rejects --tests; use the variant task)
./gradlew testDebugUnitTest --tests "tech.benedicta.will.task.TaskViewModelTest"

# Run a single test method
./gradlew testDebugUnitTest --tests "tech.benedicta.will.task.TaskViewModelTest.someTestMethod"

# Type-check (Kotlin compile without APK)
./gradlew compileDebugKotlin

# Lint
./gradlew lint

# Render the Compose previews to PNGs (layoutlib on the JVM, no device needed)
./gradlew updateDebugScreenshotTest   # write/refresh the reference images
./gradlew validateDebugScreenshotTest # fail if the UI drifted from them

# Install on connected device
./gradlew installDebug

# The only question no JVM can settle: whether Gemini Nano answers *well*.
# `ModelAnswersWellTest` asks the real model the obvious cases and checks the
# obvious answers; it skips rather than fails on a phone without AICore. Also
# replays the Room migrations and the Android halves of the shortcut and the
# model against the SQLite and the AICore that actually ship, both of which
# `./gradlew test` already covers under Robolectric
./gradlew connectedDebugAndroidTest

# Where Robolectric cannot download its own `android-all` jar (an offline or
# sandboxed build), hand it a directory that already holds them
./gradlew test -Probolectric.dependency.dir=/path/to/jars
```

## Index

| file | description |
|-|-|
| ./docs/01-IDEA.md | The main project idea, here we put the macro idea about the project, always read it to understand what we want to build |
| ./docs/99-TASKS.md | User Stories and Tasks for Will Project - organized by prerequisite tasks, user stories, and technical improvements |

## Current State

| item | status | notes |
|-|-|-|
| PT-01: Project Setup | ✅ done | minSdk 24, Compose, Room, KSP configured |
| PT-02: Core Architecture | ✅ done | Task model (Room entity), TaskDao, RoomTaskRepository, TaskViewModel |
| US-01: Basic Launcher | ✅ done | app grid + launch works; recent apps deferred (API restricted) |
| US-02: Notification Capture | ✅ done | NotificationListenerService → Task → Room; PendingIntent fired on tap; removal synced via NotificationKeyTracker; per-app filter moved to US-08 |
| US-Pin-01: Pin up to 5 apps | ✅ done | long-press in app drawer toggles pin; pinned apps render as the header of TaskFeed's `LazyColumn` so they share the scroll with tasks; persisted via SharedPreferences |
| US-03: Manual Task Creation | ✅ done | text input via `IntentInputSheet` creates a Task with the typed text. |
| US-04: Lifecycle Tracking | ✅ done (auto-completion deferred) | Task fields, `TaskStatusIndicator`, manual completion, `WillAccessibilityService` + `TaskLifecycleCoordinator`/`Applier` set `startedAt` on first source-app surface and increment `contextSwitchCount` on every non-launcher / non-source jump. Per-app window-content scraping for auto-completion is out of scope (privacy cost). |
| US-05: Focus Dashboard | ✅ done (cognitive-load estimate deferred) | `FocusDashboard` + `FocusDashboardViewModel`: today's focus score, time-on-task and switches, plus `AttentionHistory` over the task log as a bar per day across 7 or 30 days. The clock and the calendar are injected, so the whole screen is testable (39 tests). GOMS cognitive-load estimate still open; its inputs are already on `DayOfAttention` |
| US-06: Reactive Feed + Deep Linking | 🟡 partial | StateFlow feed, lifecycle ordering, PendingIntent deep linking shipped; swipe actions, search, deep-link feedback pending |
| US-07: Privacy-First | 🟡 partial | `PrivacySettings` + delete-all + permissions rationale shipped; selective deletion and accessibility on-device guarantees still to enforce |
| US-08: Notification Filter Settings | ✅ done | `SettingsScreen` + `AppFilterViewModel` + `SharedPrefsAppFilterRepository`; filter applied in `WillNotificationListenerService` |
| US-09: Brand Android Icon Set | ✅ done | adaptive icon vectors, monochrome variant, density-variant raster mipmaps regenerated from `scripts/render-icons.sh`, true-black splash via `values-v31`, programmatic wallpaper legibility audit archived under `.design/branding/will/identity/audits/` |
| US-11: Share-to-Will | ✅ done | `ACTION_SEND` (text/plain) + `will://task` via `ShareTargetActivity`; pure `SharedIntentParser` (21 tests); works on minSdk 24 |
| US-12: Task event log | ✅ done | append-only `TaskEvent` / `TaskEventRepository` / `TaskEventRecorder`; Room table + migration 8→9; fed by `TaskLifecycleApplier` and `TaskViewModel`; scalars on `Task` are now a view over it |
| US-13: Task origin | ✅ done | `TaskOrigin { NOTIFICATION, MANUAL, SHARED, AGENT }` on `Task`; migration 7→8 backfills from the `"manual"` source package |
| US-16: Inferred completion | ✅ done | `CompletionInference` reads the task log and marks tasks that look handled; feed shows `looks finished`, the existing checkbox stays the action. Metadata only, never window content. First reader of the US-12 log |
| US-17: Typed task finds its app | ✅ done | `AppMentionResolver` reads the app name out of a typed intention, so the task opens that app instead of going nowhere. Literal matching only, never a guess at what a sentence implies |
| US-18: Search survives Portuguese | ✅ done | `AppMatcher` replaces `contains(ignoreCase)` in the drawer: accents folded, initials and subsequence, ranked. Fixes "itau" never finding Itaú. Feed search still open under US-06 |
| US-19: Notification becomes an intention | ✅ done; richer kinds read by the model | `IntentionExtractor` + `NotificationToTask`: a message reads "responder Maria", a missed call "ligar de volta para Miguel". Category-driven, no model. `Task.rawTitle` keeps the original; migration 9→10. `CONFIRM`, `PAY` and `ATTEND` are read from the words by Gemini Nano via `IntentionPrompt`, opt-in and off by default, consulted only when no category answered |
| US-20: Will learns what you throw away | ✅ done | `LearnedNoiseRepository` + `LearnedIntentionClassifier`: three dismissals of the same app and title silence it, one completion undoes it. Taught by actions the user already performs. Listed and reversible in `data & privacy` |
| US-21: Active task as a shortcut | ✅ done; verified | `ActiveTaskShortcut` (pure rules, 11 tests) + `ActiveTaskShortcutPublisher`; a long press on the icon offers the task in progress. `ShortcutManagerCompat` is exercised on the JVM under Robolectric (`DeviceFeaturesJvmTest`): the shortcut is read back out of the system, published, labelled, carrying its task id, and removed |
| US-22: Intention opens a screen | ✅ done | `IntentionActionResolver` + `IntentionActionLauncher`: "ligar para Miguel" opens the dialer with Miguel. `ACTION_DIAL`, never `ACTION_CALL`. Falls back to the app |
| US-23: Search by what an app is for | ✅ done | `AppCategory` from `ApplicationInfo.category`, matched by Portuguese and English words in `AppMatcher`. No category for money, so "banco" finds nothing |
| US-24: One search box over tasks and apps | ✅ done; one box, one behaviour | `TaskMatcher` shares `SearchText` with the drawer; matching tasks sit above the apps. Closes US-06's feed search without home-screen chrome. `TaskSearchWithMeaning` closes the half that was left: apps were found by meaning and tasks only by their letters, so "banco" found the bank app and never the task about a boleto |
| US-25: Search by meaning | ✅ done; degradation verified, inference needs a device | `genai-prompt` (Gemini Nano via AICore), opt-in and off by default, `+2.3 MB`. Name match first; the model only when that finds nothing, behind a 2s timeout. Prompt and parse tested. `DeviceFeaturesJvmTest` pins the half a machine without AICore can answer: no model reports `UNAVAILABLE` rather than throwing, and the drawer search behaves exactly as it did before. Whether Nano answers *well* needs `connectedDebugAndroidTest` on an AICore phone |
| US-15: On-device triage model | ✅ done | `TriagePrompt` + `ModelReviewPass` + `ModelTriageLog`: the model reads what the pattern list missed, behind the cheap rules, and every drop is listed in `data & privacy` with a way back before it takes effect. "Blocked on data" was true only of the four *trained* routes measured (ML Kit, MediaPipe, LiteRT, a pure-Kotlin linear model, cross-validated here); a prompt needs none, and `genai-prompt` was already in the APK from US-25. Opt-in, off by default, and the only switch that can keep something from the user. Numbers and the reasoning in `./docs/99-TASKS.md` |
| US-14: AppFunctions surface | ⬜ deferred | built and reverted: the cost (`compileSdk 37`, ~1.8 MB of transitive deps, beta API, Gemini side in private preview) does not pay off yet. What the beta01 processor actually requires is recorded in `./docs/99-TASKS.md` so the next attempt does not rediscover it. `will://task` (US-11) covers agent writes meanwhile |
| US-10: First-run Onboarding | ✅ done (core) | guided flow over default launcher + overlay + notification listener; persisted completion flag, re-run from settings, lifecycle-aware auto-advance; instrumented e2e test deferred |

## Key Files

All source under `app/src/main/java/tech/benedicta/will/`.

| file | purpose |
|-|-|
| `AndroidManifest.xml` | launcher intent filters, singleTask, clearTaskOnLaunch, overlay/notification permissions |
| `WillApplication.kt` | Application class; singleton entry point |
| `MainActivity.kt` | wires ViewModels; swipe-up to app drawer; broadcasts for overlay actions |
| **Launcher** | |
| `launcher/LauncherApp.kt` | app data model (packageName, name) |
| `launcher/AppRepository.kt` | interface for app list source |
| `launcher/SystemAppRepository.kt` | PackageManager impl, sorted by name |
| `launcher/LauncherViewModel.kt` | StateFlow<List<LauncherApp>>, IO dispatcher; filters through `AppMatcher`, never `contains` |
| `launcher/SemanticAppSearch.kt` | the port, with an `Unavailable` default that every device gets when the model is off |
| `launcher/SemanticSearchPrompt.kt` | pure: numbers the apps, asks for numbers, reads the reply; the answer can only name an app that was offered |
| `launcher/SearchWithMeaning.kt` | exact first, meaning only when that finds nothing; the model can never remove a result or invent one |
| `launcher/GeminiNanoAppSearch.kt` | the Gemini Nano half: availability-gated, 2s timeout, never downloads the model itself |
| `launcher/SemanticSearchPreference.kt` | the opt-in, off by default |
| `launcher/SearchText.kt` | folding and scoring shared by the app and task search, so one box behaves one way |
| `launcher/IntentionActionResolver.kt` | pure: the verb starting an intention mapped to a standard Android action |
| `launcher/IntentionActionLauncher.kt` | the Android half; returns false when nothing handles it so the caller falls back |
| `task/TaskSearchPrompt.kt` | pure: numbers the open tasks with their messages, asks for numbers, reads the reply; the answer can only name a task the user already has |
| `task/TaskSearchWithMeaning.kt` | words first, meaning only when they find nothing; the model can never remove a result, invent a task, or see a finished one |
| `task/SemanticTaskSearch.kt` | the port, with an `Unavailable` default for every device without the model |
| `task/GeminiNanoTaskSearch.kt` | the Gemini Nano half: needs both model switches, 2s timeout, runs in the drawer's foreground |
| `task/TaskMatcher.kt` | finds open tasks for the drawer's search box: title, the title the notification arrived with, and the message, ranked so a title hit always wins. The message is matched literally, because a subsequence over a paragraph matches almost anything. Capped, finished tasks excluded |
| `launcher/ActiveTaskShortcut.kt` | pure rules for the active-task shortcut: when to publish, how to label it |
| `launcher/ActiveTaskShortcutPublisher.kt` | the Android half; pushes and removes, swallowing failures |
| `launcher/AppMatcher.kt` | drawer search: folds accents, accepts initials and subsequences, ranks results. Pure |
| `launcher/AppMentionResolver.kt` | finds the installed app named in a typed intention; accent-folded, whole words, never the launcher itself; null when unsure |
| `launcher/PinnedAppsRepository.kt` | interface for pinned apps storage; `MAX_PINNED = 5` |
| `launcher/PinnedAppsViewModel.kt` | combines pinned packageNames + installed apps → StateFlow<List<LauncherApp>> in pin order; `canPinMore`, `togglePin`, `pin`, `unpin` |
| **Task domain** | |
| `task/Task.kt` | pure domain model — no Android/Room deps; includes `notificationKey` for deep-link persistence |
| `task/TaskRepository.kt` | repository interface (domain layer) |
| `task/TaskViewModel.kt` | StateFlow<List<Task>>, addTask, deleteTask, markActive; records every user action in the task log |
| `task/TaskOrigin.kt` | where a task came from; `fromStored` tolerates an unknown value, `forLegacySourcePackage` is the migration rule; `Task.isAuthoredElsewhere` |
| `task/TaskEvent.kt` | `TaskEvent` + `TaskEventType` (no `DELETED`: removing a task removes its history) |
| `task/TaskEventRepository.kt` | append-only contract; chronological reads, ties by insertion order |
| `task/TaskEventRecorder.kt` | turns state changes into events; decides STARTED vs RESUMED |
| `task/CompletionInference.kt` | reads the task log and points out tasks that look finished; pure, thresholds injectable, suggests only |
| `task/AttentionHistory.kt` | reads the task log into one `DayOfAttention` per day: time on task, longest unbroken stretch, switches, tasks touched and finished. Overlapping work is merged before measuring, and a stretch crossing midnight is split. `distractions` ranks the apps that pulled the user away. Pure; the calendar is injected |
| `task/FocusDashboardViewModel.kt` | today's figures plus the trend; clock and calendar injected, so what it reports is a rule rather than the day the test ran |
| `ui/AttentionFormatter.kt` | pure duration formatter for the chart labels; seconds round down, because rounding up reports attention nobody spent |
| `task/TaskUpsert.kt` | upsert outcome, so the notification path can tell a new conversation from a bumped one |
| **Infrastructure** | |
| `infrastructure/room/TaskEntity.kt` | Room @Entity; `toTask()` / `Task.toEntity()` mappers |
| `infrastructure/room/TaskDao.kt` | Room DAO — insert, getAll (Flow), update, delete |
| `infrastructure/room/WillDatabase.kt` | singleton RoomDatabase |
| `infrastructure/room/RoomTaskRepository.kt` | TaskRepository impl backed by Room |
| `infrastructure/room/TaskEventEntity.kt` | Room @Entity for the log; `toTaskEvent()` returns null for an unknown type so the row is dropped, never guessed |
| `infrastructure/room/TaskEventDao.kt` | append and read only; deliberately no `@Update` |
| `infrastructure/room/RoomTaskEventRepository.kt` | TaskEventRepository impl backed by Room |
| `infrastructure/memory/MemoryTaskEventRepository.kt` | in-memory TaskEventRepository (used in tests) |
| `infrastructure/memory/MemoryTaskRepository.kt` | in-memory TaskRepository (used in tests) |
| `infrastructure/memory/MemoryPinnedAppsRepository.kt` | in-memory PinnedAppsRepository (used in tests) |
| `infrastructure/SharedPrefsPinnedAppsRepository.kt` | PinnedAppsRepository impl backed by SharedPreferences (`pinned_apps` prefs file, pipe-separated package list) |
| **Notification** | |
| `notification/NotificationProcessor.kt` | structural mapping only: blank checks, conversation key, flags. Does **not** judge whether content is worth showing |
| `notification/TriagePrompt.kt` | the housekeeping question put to the model and the reading of its answer; pure. The answer has to *be* the verdict, not contain it, so a model answering in an unasked-for shape is disregarded rather than read creatively |
| `notification/ModelReviewPass.kt` | everything the model does to notifications, run from `MainActivity.onResume` because ML Kit refuses to infer outside the foreground. Only notification-origin tasks, never a typed one; a title the category already derived is never overwritten; every drop is logged before it takes effect |
| `notification/DerivedIntentionPreference.kt` / `ModelTriagePreference.kt` | the two notification opt-ins, each its own switch and its own prefs file |
| `infrastructure/SharedPrefsModelReviewWatermark.kt` | how far the model has reviewed the feed, so a pass does not redo it |
| `notification/ModelTriageLog.kt` | what the model hid and the user's way back; `showAgain` restores it and stops that shape being judged again |
| `notification/SemanticNoiseReader.kt` | the port, whose `Unavailable` default keeps everything: a classifier that cannot think should not be hiding anything |
| `notification/GeminiNanoTriage.kt` | the Gemini Nano half: 5s timeout, never downloads the model, answers "not noise" on anything going wrong |
| `infrastructure/SharedPrefsModelTriageLog.kt` | the persistent log and the opt-in; capped at 50, oldest evicted, covered by Robolectric tests |
| `notification/IntentionPrompt.kt` | the question put to the on-device model and the reading of its answer; pure. The answer can only name a kind from a closed list and words that appear in the notification, so it cannot invent a person, a sum or an appointment |
| `notification/ReadIntentionWithMeaning.kt` | category first on every device, the model only for what no category answered. It can never drop a notification, only name what it asks for |
| `notification/SemanticIntentionReader.kt` | the port, with an `Unavailable` default so a missing model leaves the old behaviour rather than a crash |
| `notification/GeminiNanoIntentions.kt` | the Gemini Nano half: opt-in, availability-gated, 5s timeout, never downloads the model itself |
| `notification/IntentionExtractor.kt` | reads a conversation notification as what to do about it, from the system's own category; returns a kind plus subject, never words, so it stays testable and localisable |
| `notification/NotificationToTask.kt` | the whole notification-to-Task conversion, so the path has tests; the service keeps only what needs Android |
| `notification/LearnedNoiseRepository.kt` | what this user keeps throwing away, per app and title; `keyOf` folds case, accents and spacing |
| `notification/LearnedIntentionClassifier.kt` | composes the patterns with what was learned; patterns first because they are cheaper |
| `infrastructure/SharedPrefsLearnedNoiseRepository.kt` | the persistent store, capped at 200 entries, evicting the least-dismissed |
| `notification/IntentionClassifier.kt` | `fun interface` deciding intention vs noise, plus `PatternIntentionClassifier`, the default and fallback: English and Portuguese housekeeping patterns, accent-folded, biased toward keeping |
| `notification/NotificationActionStore.kt` | holds PendingIntents keyed by notification key (in-memory) |
| `notification/NotificationKeyTracker.kt` | **deprecated** — dedup now via Room unique index on `conversationKey` |
| `notification/WillNotificationListenerService.kt` | intercepts notifications; inserts Tasks; fires PendingIntent on tap |
| **Onboarding** | |
| `onboarding/OnboardingStep.kt` *(in OnboardingState.kt)* | enum of the three sequential steps + Done; `required` flag distinguishes optional notifications step |
| `onboarding/OnboardingState.kt` | pure-Kotlin state machine: `next`, `canSkip`, `isComplete`, `advanceToFirstUngranted` |
| `onboarding/OnboardingRepository.kt` | persists the completion flag; `markCompleted` / `reset` |
| `onboarding/PermissionInspector.kt` | reads default-launcher / overlay / listener grants into an `OnboardingState` snapshot |
| `onboarding/OnboardingViewModel.kt` | binds repository + inspector + state machine; `refresh` for resume, `next`/`skip` for taps, `rerun` for the settings entry |
| `infrastructure/SharedPrefsOnboardingRepository.kt` | OnboardingRepository backed by `SharedPreferences` (`onboarding` prefs file, `completed` boolean) |
| `infrastructure/AndroidPermissionInspector.kt` | PermissionInspector backed by RoleManager.ROLE_HOME (Q+) / PackageManager fallback, `Settings.canDrawOverlays`, `NotificationManagerCompat.getEnabledListenerPackages` |
| `ui/OnboardingScreen.kt` | brand-aligned three-step Compose screen consumed by MainActivity before WillScreen; primary action launches the right system intent, optional step also offers "skip for now" |
| **Share** | |
| `share/SharedIntentParser.kt` | pure parser for `ACTION_SEND` extras and `will://task` links → `SharedTask`; no Android types |
| `share/ShareTargetActivity.kt` | no-UI share target; hands the task to `WillApplication.acceptTaskFromOutside` and finishes |
| **Overlay** | |
| `overlay/ActiveTaskOverlayService.kt` | TYPE_APPLICATION_OVERLAY bubble; draggable; expands to panel with done/later/delete |
| **UI** | |
| `ui/TaskFeed.kt` | LazyColumn of TaskCards with optional `header: (LazyListScope.() -> Unit)?` slot for prepending content (used by pinned apps); tap opens source app; visible checkbox marks done; subtitle shows `N new` when `unreadCount > 1` |
| `ui/DoneTasksScreen.kt` | separate screen for completed tasks — reopen or delete |
| `ui/TaskActionSheet.kt` | bottom sheet for task actions (legacy — unused) |
| `ui/HomeTopBar.kt` | minimal top bar with date/time; done count badge; nav to filters/dashboard/done |
| `ui/AppGrid.kt` | adaptive grid composable, icon + name, tap to launch, long-press to toggle pin |
| `ui/PinnedAppsRow.kt` | exposes `LazyListScope.pinnedAppsItems(...)` extension consumed by `TaskFeed`'s header slot, so up to 5 pinned apps scroll together with the feed; same row layout as TaskItem (48dp dot + name + packageName); tap to launch, long-press to unpin (haptic feedback) |
| `ui/NotificationPermissionPrompt.kt` | banner shown when listener permission not granted |
| `ui/ThemePreference.kt` | persisted theme preference (light/dark/system) |
| `ui/TaskMetadataFormatter.kt` | pure builder for the metadata line under a task title; drops empty segments, and keeps NOTIFICATION / MANUAL silent so only origins the user did not author are named |
| `app/src/test/.../infrastructure/room/WillDatabaseMigrationJvmTest.kt` | the same replay from version 7 on the JVM under Robolectric, so `./gradlew test` answers it: real SQLite runs the migration SQL and Room validates every table on open |
| `app/src/test/.../launcher/DeviceFeaturesJvmTest.kt` | the shortcut read back out of the system, and the model's absence reported rather than thrown |
| `app/src/test/.../infrastructure/SharedPrefsLearnedNoiseRepositoryTest.kt` | the learned-noise store that actually ships, under Robolectric: a lesson surviving a restart, a title with a `|` in it, and the 200-entry cap keeping what was dismissed most |
| `app/src/androidTest/.../WillDatabaseMigrationTest.kt` | the same replay against the device's own SQLite, which is the one that ships |
| `app/src/androidTest/.../DeviceOnlyFeaturesTest.kt` | what a phone *with* AICore says about the model's presence |
| `app/src/androidTest/.../notification/ModelAnswersWellTest.kt` | whether Nano answers *well*: obvious housekeeping judged noise, a message from a person never judged noise, an invitation read as something to confirm. The floor, not the hard cases; if these fail the prompts need work before either switch is worth offering |
| `app/src/screenshotTest/.../TaskFeedScreenshotTest.kt` | `@PreviewTest` previews rendered to PNGs under `app/src/screenshotTestDebug/reference/`; add one whenever a UI change needs looking at |
| **Tests** | |
| `test/.../task/TaskRepositoryTest.kt` | TaskRepository contract tests against MemoryTaskRepository |
| `test/.../notification/NotificationProcessorTest.kt` | processor mapping + edge cases |
| `test/.../notification/NotificationActionStoreTest.kt` | store keying + eviction |
| `test/.../notification/NotificationKeyTrackerTest.kt` | key tracking + removal |
| `gradle/libs.versions.toml` | dependency catalog |
| `app/build.gradle.kts` | app-level build config |

## Design System

Will has a complete brand identity at `.design/branding/will/patterns/`. **Always follow this when implementing UI.**

| file | purpose |
|-|-|
| `.design/branding/will/patterns/will.yml` | Single source of truth — all brand tokens |
| `.design/branding/will/patterns/STYLE.md` | Agent contract — implementation rules, bold bets, constraints |
| `.design/branding/will/patterns/components/token-mapping.md` | Compose M3 mapping — ColorScheme, Typography, Shapes, Spacing |
| `.design/branding/will/patterns/guidelines.html` | Visual reference — open in browser |
| `.design/branding/will/BRIEF.md` | **Persona: Lucas** (developer 25–35, primary user) — always use Lucas, never invent personas |
| `.design/system/CONCERNS.md` | Known design debt to fix when touching affected components |

### Design project exports

Full screen specs, research, and critique at `.design/projects/will/exports/INDEX.md`.

| section | what's inside |
|-|-|
| Brief | scope, target adaptations, gap analysis, file references |
| Research | UX patterns, competitor UX, technical research, accessibility, content strategy, recommendations |
| Design | screen specs (HomeScreen, TaskActionSheet, IntentInputSheet, NotificationFilterSettings, FocusDashboard, PrivacySettings), personas, IA, navigation, micro-interactions, component plan |
| Critique | critique, prioritized fixes, alternative directions, strengths, accessibility audit & fixes |

### Non-negotiable rules (enforced in codebase)
- `background = Color(0xFF000000)` — OLED true black, never dark gray
- Accent `#4F6EF7` on **interactive elements only** — `primary` in M3 leaks to Snackbar/Checkbox/Switch/CircularProgressIndicator/Chip; audit and override explicitly
- `FontWeight.Normal` (400) for content, `FontWeight.Medium` (500) for section headers — **never SemiBold (600) or Bold (700)**
- `MaterialTheme.typography.labelSmall` uses Geist Mono — timestamps and source labels only, not titles
- `extraSmall = RoundedCornerShape(0.dp)` — task cards always 0dp radius, override M3 defaults explicitly
- No shadows, no gradients, no elevation values
- 48dp minimum touch targets (`Spacing.touchTarget`)
- `Spacing` object in `ui/theme/Theme.kt` — use `Spacing.lg` (16dp), `Spacing.xl` (24dp), etc. instead of hardcoded dp

## Launcher UX Constraints

Will is a **launcher**, not a regular app. These rules apply to every layout decision:

- **No app chrome** — no TabRow, no TopAppBar, no bottom navigation bar. These are app patterns, not home screen patterns.
- **Task feed is the home screen** — full screen, edge-to-edge, status bar overlays content.
- **App grid is secondary** — accessible via explicit button in top bar (+ swipe up as bonus gesture), never a tab.
- **No decorative containers** around the main content — the screen IS the launcher.
- **Permission prompts** = small non-intrusive banners (bottom of screen), never full-screen dialogs that block the home screen.

## Architecture Decisions

- **The task log is the record; the scalars are a view** — `startedAt`, `pausedAt`, `completedAt` and `contextSwitchCount` on `Task` answer what the state is now. `TaskEvent` answers how it got there, and is append-only. Both are written together by `TaskLifecycleApplier` and `TaskViewModel`. Never derive one from the other at read time; keep writing both.
- **No `DELETED` event** — deleting a task deletes its events (US-07). A tombstone would point at a row nothing can resolve. `TaskEventRecorder.forgot` wipes, it does not append.
- **Room migration SQL is copied from Room's exported schema**, not hand-written: Room validates the table against the entity on open, and a near-miss fails at runtime. Schemas are exported to `app/schemas` and committed, so a new migration is written from `createSql` in the generated JSON. `WillDatabaseMigrationTest` replays an upgrade from version 7 on a real device; opening the database is the assertion, because Room throws when a migrated table does not match its entity.
- **A device is the confirmation, not the gate** — the migration replay and the Android halves of the shortcut and the model run on the JVM under Robolectric, so `./gradlew test` catches them. The migration test has teeth: changing one migrated column from `TEXT` to `INTEGER` fails all five, because Room compares every migrated table against its entity when it opens the database and opening is the assertion. The instrumented copies stay, because real SQLite and real AICore are what ship. Robolectric fetches its own `android-all` jar with its own HTTP client, which a sandboxed build cannot always reach; `-Probolectric.dependency.dir=` points it at a directory instead and turns its offline mode on.
- **UI changes are rendered before they are handed over** — `./gradlew updateDebugScreenshotTest` renders the `@PreviewTest` previews in `app/src/screenshotTest/` through layoutlib on the JVM, so there is no emulator or device in the loop. The reference PNGs are committed; `validateDebugScreenshotTest` fails when the UI drifts from them. Enabling it needs the flag in **both** places: `android.experimental.enableScreenshotTest=true` in `gradle.properties` (so the plugin applies) and `experimentalProperties["android.experimental.enableScreenshotTest"] = true` in the module's `android` block (so the source set exists). `@PreviewTest` comes from `com.android.tools.screenshot:screenshot-validation-api`, which the plugin does not add for you; without it the task fails with "did not discover any tests".
- **The `"manual"` source package never reaches the screen** — it is the sentinel for a task with no app to open, not a name to read. `TaskFeed` maps it to a null app label and `taskMetadataLine` drops the segment.
- **Triage is a seam, not a regex buried in the processor** — `NotificationProcessor` answers structural questions; `IntentionClassifier` answers "is this an intention or noise". The pattern implementation is the default and the fallback on every device. It is biased toward keeping: a false keep costs one dismissable row, a false drop means a message the user never sees and the feed cannot tell them about.
- **Kotlin 2.4 removed the `kotlinOptions` DSL** — the JVM target is set through `kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }` at the top level of `app/build.gradle.kts`, not inside the `android` block. On 2.4 the old form is a build error, not a warning.
- **What Will learns to hide has to be visible and undoable** — `LearnedNoiseRepository` silences a notification after three dismissals, so `data & privacy` lists every silenced title with a way back, and "delete all task history" forgets the lot. Hiding something the user never asked to hide is only acceptable while they can see it and undo it. Only notification tasks teach it, and only when deleted unfinished: tidying the done screen is not a complaint.
- **The model only runs in the foreground, so the model's work happens there** — ML Kit's GenAI APIs return `BACKGROUND_USE_BLOCKED` outside the top foreground app, and a foreground service does not get around it. `WillNotificationListenerService` is a background service, so the first wiring of both notification features would have done nothing on a real phone while spending a 5s timeout per notification. `ModelReviewPass` runs from `MainActivity.onResume` instead. The split it forces is the better one: everything that needs no model still happens the instant a notification lands (the task, the category, the patterns, what the user taught Will), and the model only ever catches up, so a failure leaves the feed exactly as the model-free path built it. Semantic app search was never affected, running in the launcher's own foreground.
- **Nothing is hidden that cannot be found** — a model is allowed to drop a notification only because `ModelTriageLog` records the drop before it takes effect, `data & privacy` lists it, and "show again" both restores it and stops that shape being judged again. That is CLAUDE.md's own condition on hiding anything, and it is what made triage worth building rather than the model being the reason. The rest of the design is the same asymmetry stated three ways: the cheap rules decide first, the model is never asked about something already decided, and only an unambiguous verdict of noise drops anything. A false keep costs one dismissable row; a false drop costs a message nobody ever sees.
- **The model may name a notification, never bury one** — `ReadIntentionWithMeaning` can only turn a notification into a better title. Everything that can go wrong with it, the switch being off, a device without AICore, a timeout, an unparseable answer, ends in the title the notification arrived with, which is what every device got before. Triage is the opposite shape: a wrong drop is a message nobody ever sees, which is why it was built only once `ModelTriageLog` existed to make every drop visible and undoable. The cheap triage still runs inside `NotificationToTask`, before anything else, so noise never reaches `ModelReviewPass` at all.
- **One opt-in does not carry another** — `DerivedIntentionPreference` is its own switch and its own prefs file, not a second key under `semantic_search`. Agreeing to send the model a search term and a list of app names is not agreeing to send it what a notification said.
- **A derived title never loses the original** — `IntentionExtractor` rewrites a conversation notification's title into what to do about it, and `Task.rawTitle` keeps what arrived. The conversation key still follows the original, so deriving a title cannot split one conversation into two tasks, and triage judges the original, because the noise patterns are written against vendor wording. Typed intentions are never derived: `01-IDEA.md` promises those are kept exactly as written.
- **One box, one behaviour, on both halves** — the drawer searched apps by meaning and tasks only by their letters, so "banco" found the bank app and never the task about paying a boleto. `TaskSearchWithMeaning` gives the task half the same contract `SearchWithMeaning` gives the app half: words first, the model only when they find nothing, numbers from a list Will offered so it cannot invent a row, finished tasks never offered, and the old behaviour on every failure. It runs in the drawer, which is the launcher's own foreground, so ML Kit's foreground-only rule needs no deferral here. It needs **both** model switches, because it combines what each covers: the search switch is consent for the model to answer searches, the reading switch is consent for it to see what a notification said, and a task title is usually the second.
- **The model is never the only thing between the user and their apps** — `SearchWithMeaning` runs `AppMatcher` first and asks `SemanticAppSearch` only when that finds nothing. The model cannot remove a result the matcher found, cannot name an app that is not installed (it answers with numbers from a list Will gave it), and when it is off, unavailable, slow or broken the search behaves exactly as it did before. It is off until the user turns it on, and `GeminiNanoAppSearch` never downloads the model on its own.
- **A tap follows the intention's own verb** — `launchTask` asks `IntentionActionResolver` first, so "ligar para Miguel" opens the dialer rather than the phone app's front door, and falls back to the task's app when no verb matches. Always `ACTION_DIAL`, never `ACTION_CALL`: Will does not place a call on anyone's behalf, and needs no permission to do it this way.
- **Every text states its colour** — on true black an inherited `LocalContentColor` disappears outside a `Surface`. `AppItem` was the one place that relied on it, which only showed up when a preview rendered it without one.
- **Resolution is literal, never a guess** — `AppMentionResolver` targets a task at an app the user actually named. It does not infer that "pagar boleto" means a bank: being wrong opens the wrong app, and the cost of that is higher than the cost of leaving the task untargeted, which is what every typed task used to be.
- **Data collected and never shown is a bug** — `SWITCHED_AWAY` has carried the app the user jumped to since US-12, and for that whole time nothing read it: Will was recording what interrupts people and never telling them. `AttentionHistory.distractions` closes that. The rule it sets: if the log keeps a field, something has to answer for it or it should not be stored. Applied to this change itself: `longestStretchMillis` went on the screen, and `tasksTouched` was deleted rather than left computed for a reader that does not exist.
- **A day's attention is the union of its tasks, never their sum** — `AttentionHistory` merges overlapping stretches before measuring, so two tasks open across the same hour cost the user one hour. Summing them would let a day report more attention than it contains, which is the fastest way to make a dashboard worth ignoring. A stretch crossing midnight is split between the days in the proportion it fell in each, because attributing it to the day it started quietly empties every morning after a late night. Days are walked with a twelve-hour margin rather than by adding 24 hours, so daylight saving cannot shift them.
- **Inference suggests, the user completes** — `CompletionInference` reads the task log and marks a task as probably handled; nothing writes `completedAt` on its own. A task that was never started is never suggested, whatever its age, so an unread notification cannot be buried by a guess. It reads timestamps, event types and package names only, which is the same surface the accessibility service already exposed.
- **Domain/Infrastructure separation** — `task/Task.kt` is a pure Kotlin data class with zero Android deps. `infrastructure/room/TaskEntity.kt` mirrors it with Room annotations and provides `toTask()` / `Task.toEntity()` mappers. Never add Android/Room imports to domain classes.
- No Hilt — manual `ViewModelProvider.Factory` for DI
- `CoroutineDispatcher` injected into ViewModel for testability
- App icon loaded in composable (not in ViewModel) to keep model Android-framework-free
- `getRecentTasks()` deferred — restricted API on Android 5+
- `excludeFromRecents=true` — launcher must never appear in the recents screen
- `stateNotNeeded=true` — launcher doesn't need onSaveInstanceState
- `screenOrientation=portrait` — portrait only, matches minimal launcher convention
- `windowSoftInputMode=stateAlwaysHidden` — keyboard must never auto-pop on home screen
- `Task.notificationKey` persisted in Room — on tap, used to look up `contentIntent` from active notifications even after app restart; `NotificationActionStore` is a fast in-memory cache, `notificationKey` is the durable fallback
- **Conversation dedup via title-based key + upsert** — `Task.conversationKey` is `packageName:normalizedTitle` (lowercase + trim) and has a unique index. `WillNotificationListenerService` calls `upsertByConversationKey`: when an open task with that key already exists, it refreshes `notificationKey`, `createdAt`, `title`, `text` and increments `unreadCount` instead of dropping the new notification. Completed tasks (`completedAt IS NOT NULL`) are excluded from the upsert match, so a finished conversation never gets resurrected. `clearConversationKey()` is still called on done/delete to release the unique-index slot for future notifications. `shortcutId` is no longer used because messengers (notably WhatsApp) emit it inconsistently across messages, groups, and media — the title is the reliable conversation identifier
- `NotificationKeyTracker` is **deprecated** — all dedup is now handled by Room's unique index on `conversationKey`, surviving service restarts
- `WillNotificationListenerService` uses its own `CoroutineScope(Dispatchers.IO + SupervisorJob)` — not tied to Activity lifecycle
- `WillDatabase` is a singleton created in Activity/Service via `WillDatabase.getInstance(context)`
- Swipe-up to open app drawer uses `nestedScroll` — `pointerInput` on an outer `Box` doesn't work because `LazyColumn` consumes all vertical drag events first. `nestedScroll.onPostScroll` intercepts the leftover scroll that the list couldn't consume.
- `ActiveTaskOverlayService` uses `TYPE_APPLICATION_OVERLAY` — requires `SYSTEM_ALERT_WINDOW` permission; communicates results back to MainActivity via local broadcast (`ACTION_DONE`, `ACTION_LATER`, `ACTION_DELETE`)

## Development workflow

We work as XP (eXtreme Programming) pair programmers. These rules are **mandatory** and override all other defaults:

1. **[IMPORTANT] XP pair programming** — Claude is the pair, the user drives. All decisions are made together.
2. **[IMPORTANT] Tests first** — Before any implementation, write tests that reflect what the User Story expects. Tests must be red/failing before writing production code.
3. **Edge cases** — Always consider edge cases when writing test scenarios.
4. **INVEST User Stories** — All US must be Independent, Negotiable, Valuable, Estimable, Small, Testable. If a US is not INVEST, break it into smaller parts until it is.
5. **Baby steps** — Only write production code after tests are broken/red. Implement the minimum to make tests pass.
6. **TDD cycle** — Red → Green → Refactor. No exceptions.
7. **[IMPORTANT] E2E / regression tests** — After implementing a full feature or user flow, write an E2E test that exercises the integrated path. Unit tests catch isolated logic errors; E2E tests catch integration bugs that are invisible to unit tests.
8. **Atomic conventional commits** — Commit often, one logical change per commit. Use conventional commit format. `feat:` and `fix:` messages target the final user (they generate the changelog) — write them as user-facing descriptions, promoting the new feature, not technical notes.
9. **[IMPORTANT] Regression test after a bug report** — When a bug is reported, write a test that reproduces the bug before fixing it. This ensures the bug is fully understood and prevents regressions in the future.
10. **site/docs/changelog.md** — After finish a feature, update the changelog with a user-facing description of the change. Group changes by user story or feature, not by technical details.

## Basic Rules

- Avoid add not necessary comments in code, comment with not clear propose is a bad practice
- Always use meaningful variable names, avoid using single letter variables
- Use consistent naming conventions for functions and variables
- Always add a task for yourself to check if the code is working