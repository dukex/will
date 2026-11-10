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
| US-05: Focus Dashboard | 🟡 partial | `FocusDashboard` + `FocusDashboardViewModel` (focus score, time-on-task bar chart, context switches — today only); date-range filtering and trends pending |
| US-06: Reactive Feed + Deep Linking | 🟡 partial | StateFlow feed, lifecycle ordering, PendingIntent deep linking shipped; swipe actions, search, deep-link feedback pending |
| US-07: Privacy-First | 🟡 partial | `PrivacySettings` + delete-all + permissions rationale shipped; selective deletion and accessibility on-device guarantees still to enforce |
| US-08: Notification Filter Settings | ✅ done | `SettingsScreen` + `AppFilterViewModel` + `SharedPrefsAppFilterRepository`; filter applied in `WillNotificationListenerService` |
| US-09: Brand Android Icon Set | ✅ done | adaptive icon vectors, monochrome variant, density-variant raster mipmaps regenerated from `scripts/render-icons.sh`, true-black splash via `values-v31`, programmatic wallpaper legibility audit archived under `.design/branding/will/identity/audits/` |
| US-11: Share-to-Will | ✅ done | `ACTION_SEND` (text/plain) + `will://task` via `ShareTargetActivity`; pure `SharedIntentParser` (21 tests); works on minSdk 24 |
| US-12: Task event log | ✅ done | append-only `TaskEvent` / `TaskEventRepository` / `TaskEventRecorder`; Room table + migration 8→9; fed by `TaskLifecycleApplier` and `TaskViewModel`; scalars on `Task` are now a view over it |
| US-13: Task origin | ✅ done | `TaskOrigin { NOTIFICATION, MANUAL, SHARED, AGENT }` on `Task`; migration 7→8 backfills from the `"manual"` source package |
| US-16: Inferred completion | ✅ done | `CompletionInference` reads the task log and marks tasks that look handled; feed shows `looks finished`, the existing checkbox stays the action. Metadata only, never window content. First reader of the US-12 log |
| US-15: On-device triage model | ⬜ blocked on data | the `IntentionClassifier` seam and the pattern implementation are shipped and in use. ML Kit, MediaPipe, LiteRT and a pure-Kotlin linear model were each measured; none earns its place yet, and the linear one was built and cross-validated here. Numbers and the reason in `./docs/99-TASKS.md` |
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
| `launcher/LauncherViewModel.kt` | StateFlow<List<LauncherApp>>, IO dispatcher |
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
- **Room migration SQL is copied from Room's exported schema**, not hand-written: Room validates the table against the entity on open, and a near-miss fails at runtime. To add one, flip `exportSchema = true` plus `ksp { arg("room.schemaLocation", ...) }` temporarily, read `createSql` from the generated JSON, then revert.
- **UI changes are rendered before they are handed over** — `./gradlew updateDebugScreenshotTest` renders the `@PreviewTest` previews in `app/src/screenshotTest/` through layoutlib on the JVM, so there is no emulator or device in the loop. The reference PNGs are committed; `validateDebugScreenshotTest` fails when the UI drifts from them. Enabling it needs the flag in **both** places: `android.experimental.enableScreenshotTest=true` in `gradle.properties` (so the plugin applies) and `experimentalProperties["android.experimental.enableScreenshotTest"] = true` in the module's `android` block (so the source set exists). `@PreviewTest` comes from `com.android.tools.screenshot:screenshot-validation-api`, which the plugin does not add for you; without it the task fails with "did not discover any tests".
- **The `"manual"` source package never reaches the screen** — it is the sentinel for a task with no app to open, not a name to read. `TaskFeed` maps it to a null app label and `taskMetadataLine` drops the segment.
- **Triage is a seam, not a regex buried in the processor** — `NotificationProcessor` answers structural questions; `IntentionClassifier` answers "is this an intention or noise". The pattern implementation is the default and the fallback on every device. It is biased toward keeping: a false keep costs one dismissable row, a false drop means a message the user never sees and the feed cannot tell them about.
- **Kotlin 2.4 removed the `kotlinOptions` DSL** — the JVM target is set through `kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }` at the top level of `app/build.gradle.kts`, not inside the `android` block. On 2.4 the old form is a build error, not a warning.
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