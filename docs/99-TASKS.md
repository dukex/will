# TASKS.md - User Stories and Tasks for Will Project

## Prerequisite Tasks (Must be completed before any User Story)

### PT-01: Project Setup
- [x] Create Android Studio project with minimum SDK 21
- [x] Configure basic app structure with MainActivity
- [x] Set up AndroidManifest.xml with launcher category
- [x] Implement basic UI with Compose or XML layout
- [x] Configure Gradle dependencies for core AndroidX libraries

### PT-02: Core Architecture Foundation
- [x] Design and implement Task data model
- [x] Set up Room database for Task persistence
- [x] Create Repository pattern for data access
- [x] Implement basic Dependency Injection (Hilt or manual)
- [x] Create ViewModel for Task management

## User Stories

### US-01: Basic Launcher Functionality
**As a user, I want Will to function as a basic Android launcher so I can access my apps and return to home screen reliably.**

Subtasks:
- [x] Implement proper manifest configuration (launchMode, taskAffinity, etc.)
- [x] Handle HOME and DEFAULT intent categories
- [x] Implement singleTask behavior to prevent multiple instances
- [x] Ensure clearTaskOnLaunch functionality works
- [x] Handle configuration changes without activity destruction
- [x] Display app icons and names in a scrollable grid
- [x] Allow launching apps by tapping icons
- [x] Implement back button behavior to return to launcher (handled by singleTask + clearTaskOnLaunch at system level)

### US-02: Notification Capture and Task Creation ✅
**As a user, I want notifications to automatically become actionable tasks in Will so I don't miss important alerts.**

Subtasks:
- [x] Request NotificationListenerService permission
- [x] Implement NotificationListenerService to intercept notifications
- [x] Extract notification metadata (title, text, package, timestamp)
- [x] Associate PendingIntents with notifications for direct action
- [x] Create Task objects from notifications with createdAt timestamp
- [x] Store notification-based Tasks in Room database
- [x] Display notification tasks in the main feed
- [x] Allow tapping notification tasks to trigger their PendingIntent
- [x] Handle notification removal/updates from system
- [ ] Provide settings to filter notifications by app or priority → moved to US-08

### US-03: Manual Task Creation via Text Input ✅
**As a user, I want to create tasks by typing them so I can capture intentions without navigating apps.**

Subtasks:
- [x] Implement text input field for manual task entry (`IntentInputSheet`)
- [x] Store the typed text as a Task in Will

Will treats the typed text as the task, exactly as the user wrote it.

### US-04: Task Lifecycle Tracking ✅ (auto-completion intentionally deferred)
**As a user, I want Will to automatically track when I start a task and how often I switch context so I can measure my productivity accurately.**

Subtasks:
- [x] Add `startedAt`, `completedAt`, `contextSwitchCount`, `status` fields on Task/TaskEntity
- [x] Provide visual feedback on task status (`TaskStatusIndicator` — pending / in-progress / done)
- [x] Allow manual task completion marking (checkbox in TaskFeed; overlay actions)
- [x] Show lifecycle context in `TaskActionSheet` (state-aware copy and actions)
- [x] Implement startedAt timestamp when user engages with a task — set by `TaskViewModel.setActiveTask` and reaffirmed by the AccessibilityService when the source app surfaces; idempotent via `markStartedIfNeeded`
- [x] Create AccessibilityService to monitor app usage (`WillAccessibilityService` + `accessibility_service_config.xml`, opt-in under system Accessibility settings, exposes only `event.packageName`)
- [x] Detect when user switches between apps (`TaskLifecycleCoordinator` + `TaskLifecycleApplier`, eleven unit tests cover the rule matrix)
- [x] Increment contextSwitchCount for each app transition — handled by the coordinator/applier pair against `TaskRepository.incrementContextSwitch`
- [x] Settings entry to enable lifecycle tracking — `SettingsScreen` row reflects `PermissionInspector.hasAccessibilityService` and opens `Settings.ACTION_ACCESSIBILITY_SETTINGS`
- [ ] ~~Identify task completion signals in third-party apps~~ → out of scope. Reading window content per-app would require `canRetrieveWindowContent=true`, which expands the privacy surface beyond what the brand promise allows. Manual completion via the checkbox / overlay action remains the supported path.
- [ ] ~~Automatically set completedAt~~ → out of scope, see above.
- [ ] ~~Handle cases where automatic detection fails~~ → moot once auto-completion is out of scope.
- [ ] Implement task expiration / stale-task handling — deferred to post-MVP; no clear UX motivation yet.

### US-05: Productivity Metrics Dashboard
**As a user, I want to see visualizations of my task completion patterns so I can understand and improve my productivity.**

Subtasks:
- [x] Calculate Time-on-Task (completedAt − startedAt) for each task (`FocusDashboardViewModel`)
- [x] Create daily focus score based on task completion metrics
- [x] Build dashboard using custom Canvas (no external chart lib)
- [x] Display time-on-task distribution (bar chart, normalized per-task)
- [x] Show context switch frequency (today's total)
- [ ] Implement GOMS model to estimate "Carga Estranha" (cognitive load saved) — now has the data it needs (US-12)
- [ ] Visualize focus score trends across days (line graph) — now has the data it needs (US-12)
- [ ] Provide filtering by date range (week, month) — currently only today; `TaskEventRepository.eventsSince` is the input
- [ ] ~~Export metrics~~ → out of MVP per scope.md
- [ ] ~~Goal setting / progress tracking~~ → out of MVP per scope.md

### US-06: Reactive Task Feed with Deep Linking
**As a user, I want a clean, text-focused interface that updates in real-time and takes me directly to the right app functionality.**

Subtasks:
- [x] Implement StateFlow for reactive UI updates (`TaskViewModel.tasks`)
- [x] Create vertical stream of task cards, text-focused, minimal icons (`TaskFeed`)
- [x] Design task cards to show essential info: title, source app, time
- [x] Lifecycle-based ordering (active → pending → done-today)
- [x] Implement deep linking to jump directly to relevant app screens (PendingIntent + `notificationKey` fallback)
- [ ] Provide visual feedback when deep linking is attempted (toast/state when PendingIntent is fired)
- [ ] Implement search/filter functionality within the task feed
- [ ] ~~Priority-based ordering (urgent/today/scheduled)~~ → not in current scope; lifecycle ordering replaces it
- [ ] ~~App Actions integration~~ → out of MVP per scope.md
- [ ] ~~Clipboard monitoring~~ → out of MVP per scope.md
- [ ] ~~Customizable feed density~~ → out of MVP per scope.md

### US-07: Privacy-First On-Device Processing
**As a user, I want all my task data processed locally on my device so my privacy is protected.**

Subtasks:
- [x] Store all Task data exclusively in Room database (local only)
- [x] Prevent any task metadata from leaving the device (no network code in app)
- [x] Implement transparent privacy settings explaining data usage (`PrivacySettings`)
- [x] Provide option to delete all task history (with confirm-then-execute pattern)
- [x] Provide clear explanation of required permissions and why
- [ ] Ensure accessibility service data is not logged or transmitted → enforce when US-04 AccessibilityService lands
- [ ] Allow selective deletion of tasks by time range or app
- [ ] ~~Data encryption~~ → out of MVP per scope.md
- [ ] ~~Local backups~~ → out of MVP per scope.md

### US-09: Brand-aligned Android Icon Set ✅
**As a user, I want Will to have a distinctive icon so I can recognize it on my home screen and in the app drawer at a glance.**

Subtasks:
- [x] Replace the default `ic_launcher` / `ic_launcher_round` with the Will mark (adaptive icon vectors at `res/drawable/ic_launcher_*`)
- [x] Provide an adaptive icon (foreground layer + `#000000` background per `will.yml`)
- [x] Generate density variants (mdpi / hdpi / xhdpi / xxhdpi / xxxhdpi) — rasterised from the vector via `scripts/render-icons.sh`
- [x] Provide a monochrome icon for Android 13+ themed icons (`ic_launcher_monochrome.xml`)
- [x] Replace the splash drawable so it matches the icon, not the platform default — `values-v31/themes.xml` overrides `windowSplashScreenBackground` to true black
- [x] Verify icon legibility on light, dark, and patterned wallpapers — programmatic audit at `.design/branding/will/identity/audits/icon-wallpaper-2026-05-01/`
- [x] Document the icon spec under `.design/branding/will/identity/launcher-icon.md` so future revisions stay consistent

### US-10: First-run Onboarding for Default Launcher and Permissions ✅ (core)
**As a user installing Will for the first time, I want a guided path to make Will my default launcher and grant only the permissions I want, without being dropped into a wall of system dialogs.**

Subtasks:
- [x] Detect first launch when no required permissions or default-launcher role is set (`PermissionInspector` + `OnboardingRepository`)
- [x] Build a single onboarding flow with sequential steps
  - [x] step 1 — set Will as the default home (RoleManager `ROLE_HOME` on Android 10+, fall back to `ACTION_HOME_SETTINGS` on older devices)
  - [x] step 2 — grant `SYSTEM_ALERT_WINDOW` (required for the active-task overlay)
  - [x] step 3 — grant the notification listener (optional; the screen explains what is unlocked)
- [x] Each step renders in the brand voice: title, one-sentence rationale, the system action button, and a "skip" affordance for optional steps only
- [x] Detect when each permission is granted (lifecycle resume check) and auto-advance (`OnboardingViewModel.refresh()`)
- [x] Persist a `onboarding_completed` flag so onboarding does not replay (`SharedPrefsOnboardingRepository`)
- [x] Allow re-running onboarding from `SettingsScreen`
- [x] Honour the design system: 0dp cards, 1dp `#2A2A2A` borders, accent only on the action button
- [x] Tests
  - [x] unit — step state machine (`OnboardingStateTest`)
  - [x] unit — onboarding repository contract (`OnboardingRepositoryTest`)
  - [x] unit — permission inspector contract (`PermissionInspectorTest`)
  - [x] unit — view model wiring (`OnboardingViewModelTest`)
  - [ ] e2e / integration — full happy path: install → onboarding → home renders with feed (deferred — requires instrumented tests)

### US-08: Notification Filter Settings ✅
**As a user, I want to control which apps can create tasks from notifications so my feed stays relevant.**

Subtasks:
- [x] Create settings screen accessible from the task feed (`SettingsScreen`, reachable from `HomeTopBar`)
- [x] List all apps that have sent notifications, with enable/disable toggle (`AppToggleRow`)
- [x] Persist filter preferences (`SharedPrefsAppFilterRepository`)
- [x] Apply filter in `WillNotificationListenerService` before creating tasks
- [x] Link to data & privacy from the same screen (`onNavigateToPrivacy`)

### US-11: Share-to-Will ✅
**As a user, I want to send anything from any app into Will so an intention I notice elsewhere lands on my home screen without me retyping it.**

Subtasks:
- [x] Pure parser for the share-sheet extras and `will://task` links (`SharedIntentParser`, 21 unit tests)
- [x] `ACTION_SEND` / `text/plain` intent filter so Will appears in the Android share sheet
- [x] `will://task?title=…&text=…` scheme so a script, automation tool or on-device agent can create a task by firing an intent
- [x] No-UI share target (`ShareTargetActivity`, translucent, `noHistory`) so sharing never pulls the user out of the app they were in
- [x] Keep the sharing app as `sourcePackage` (via `referrer`) so tapping the task returns there
- [x] Nothing is interpreted: the text arrives as written, only trimmed and split into title plus body
- [x] An empty share creates nothing
- [ ] Instrumented test for the share flow — deferred, same reason as US-10's e2e

### US-12: Task event log ✅
**As a user, I want Will to keep how a task went, not just where it ended up, so the dashboard can eventually tell me when I actually get things done.**

Subtasks:
- [x] `TaskEvent` + `TaskEventType` domain model, no Android deps
- [x] `TaskEventRepository` append-only contract, chronological reads with ties by insertion order (13 contract tests)
- [x] `TaskEventRecorder` deciding STARTED vs RESUMED (9 tests)
- [x] Room table, DAO without `@Update`, repository impl, migration 8→9
- [x] Fed from `TaskLifecycleApplier` (STARTED, SWITCHED_AWAY with the package jumped to) and `TaskViewModel` (CREATED, PAUSED, RESUMED, COMPLETED, REOPENED)
- [x] Notification-created tasks logged once per conversation, not once per message (`TaskUpsert.created`)
- [x] Deleting a task drops its events; clearing all data clears the log (US-07)
- [ ] Surface it: US-05's trends, date ranges and cognitive-load estimate are what this unblocks, and none of it is built yet

### US-13: Task origin ✅
**As a user, I want to see which tasks I did not put there myself, now that other apps and agents can write to my home screen.**

Subtasks:
- [x] `TaskOrigin { NOTIFICATION, MANUAL, SHARED, AGENT }` on the domain model
- [x] Column plus migration 7→8 backfilling from the `"manual"` source package
- [x] Unknown stored value reads as NOTIFICATION rather than throwing
- [x] `Task.isAuthoredElsewhere` for SHARED and AGENT
- [ ] Show it in the feed — the data is there, `TaskFeed` does not render it yet

### US-16: Inferred completion ✅
**As a user, I want Will to point out the tasks I have probably already dealt with, so clearing the feed is one tap instead of a memory exercise.**

US-04 gave up on automatic completion because deciding from inside another app would have meant `canRetrieveWindowContent=true`, and that costs more privacy than the brand promise allows. The task log from US-12 reopens the question on the right side of that line: the inference sees timestamps, event types and package names, which is all the accessibility service ever exposed, and no content at all.

Subtasks:
- [x] `CompletionInference`, pure and with no Android dependency (15 unit tests)
- [x] The rule, in order: the task is open; it was actually started; the user spent at least 30 seconds on it; nothing has happened for 20 minutes; the last event is under 24 hours old
- [x] A notification nobody opened is never suggested, however long it sits. That is the property that keeps the feature from quietly burying unread work
- [x] `TaskViewModel.completionSuggestions`, recomputed on a slow tick because a task becomes suggestable by time passing with no event to wake a flow
- [x] The feed says `looks finished` as the last metadata segment. No badge, no accent, no new touch target: the checkbox that was already there is still the action
- [x] End-to-end test over repository, recorder, log and view model (`CompletionSuggestionIntegrationTest`)
- [x] Rendered and checked before handover (`TaskFeedSuggestionsPreview`)
- [ ] Tune the thresholds against real use. 20 minutes and 30 seconds are reasoned, not measured, and they are constructor parameters so they can move without touching the rule
- [ ] Decide whether an ignored suggestion should eventually stop being shown. Today it is derived, so it persists until the task changes

This is also the first thing that reads the task log. Before it, US-12 was write-only.

### US-17: Typed intentions find their app ✅
**As a user, when I type "mandar o contrato no WhatsApp" I want tapping that task to open WhatsApp, instead of nothing happening.**

A typed task was created with `sourcePackage = "manual"`, which is the sentinel for "no app to open". Tapping it called `getLaunchIntentForPackage("manual")`, got null, and did nothing. Every typed task was a dead end.

Subtasks:
- [x] `AppMentionResolver`: finds the installed app whose name the user wrote, accent-folded and case-insensitive, whole words only (13 unit tests)
- [x] Wired into the typed-task path, so the task is created pointing at that app
- [x] The launcher itself is never the target, which would be a loop
- [x] One- and two-letter app names are ignored: they collide with ordinary words too often to be a signal
- [x] Earliest mention wins, longest name breaks a tie, so "Google Chat" beats a bare "Chat"
- [x] The resolution is visible before it is used: the feed already shows the app on the metadata line, so the user sees where the tap will go

Deliberately literal. It resolves a name the user actually typed and does not guess that "pagar boleto" means a bank, because being wrong sends them into the wrong app. When it is unsure the task stays untargeted, which is what used to happen to every typed task.

- [ ] Resolving an intention to a *screen* rather than an app, which is what `01-IDEA.md` wants from deep links. That needs reading the installed apps' declared intent filters and is a story of its own.

### US-18: Search that survives Portuguese ✅
**As a user typing in the app drawer, I want to find Itaú by typing "itau", and Google Drive by typing "gd".**

The drawer filtered with `name.contains(query, ignoreCase = true)`. Ignoring case does nothing about accents, so `itau` never found "Itaú" and `configuracoes` never found "Configurações". On a phone used in Portuguese that failed several times a day.

Subtasks:
- [x] `AppMatcher`, pure and with no Android dependency (15 unit tests)
- [x] Accents folded on both sides, so the query and the name meet in the middle
- [x] Initials of a multi-word name: `gd` finds Google Drive
- [x] Subsequence of the letters in order: `wapp` finds WhatsApp, `tlgrm` finds Telegram, and `ppaw` finds nothing
- [x] Ranked, so a name that starts with the query beats one that merely contains it, and a scattered subsequence comes last
- [x] Ties keep the repository's alphabetical order
- [x] Replaces the filter in `LauncherViewModel`; its eight existing tests still pass unchanged

- [ ] The same matcher over the task feed. US-06 still lists feed search as open, and it needs a search field in the home screen, which is a UI decision of its own.
- [ ] This is matching, not meaning. "money" will not find the bank app. That needs embeddings, which is the blocked US-15 problem.

### US-19: A notification becomes what to do about it ✅
**As a user, I want a message from Maria to read "responder Maria" in my feed, instead of just "Maria".**

This is the thesis of `01-IDEA.md`: "um alerta de mensagem se transforme na tarefa 'Responder Contato'". Until now the feed mirrored the notification; the task was titled with the sender's name and the user had to supply the verb themselves.

Subtasks:
- [x] `IntentionExtractor`, pure, reading the notification's own category (15 unit tests)
- [x] A message or an email becomes replying to the sender; a missed call becomes calling back
- [x] `Sender: message` titles yield the sender, and `(3 messages)` suffixes are not part of anyone's name
- [x] A title long enough to be the message itself is left alone: that notification carries no sender to reply to
- [x] Everything that is not a conversation arrives exactly as it was, which is most notifications
- [x] `Task.rawTitle` keeps the original, so the derivation is reversible and auditable. Migration 9→10
- [x] The phrasing comes from a string resource, so it follows the phone's language. `values-pt` ships the Portuguese
- [x] `NotificationToTask` holds the whole conversion so it can be tested; the service keeps only what needs Android (8 tests)
- [x] The conversation key still follows the original title, so two messages from Maria stay one task
- [x] Triage judges the original title, not the derived one: the noise patterns are written against vendor wording
- [x] Rendered and checked before handover (`TaskFeedDerivedPreview`)

No language model, and no guessing at what a sentence means. It reads a category the system already attaches, which is why it can be exact. Typed intentions never pass through here: `01-IDEA.md` promises those stay "exatamente como o usuário a escreveu", and that promise is intact.

- [ ] Show the original title in the feed. It is in the data; nothing renders it yet, and the derived title already contains the sender, so nothing is currently hidden.
- [ ] Richer kinds than reply and call back. "Confirme sua presença" is an intention too, but reading that needs the model work blocked under US-15.

### US-15: On-device notification triage ⬜ blocked on data, not on code
**As a user, I want Will to recognise housekeeping notifications it has never seen before, so the feed stays clean without me maintaining a list of patterns.**

Shipped so far: the `IntentionClassifier` seam and `PatternIntentionClassifier`, with English and Portuguese patterns, accent-folded. That part is done and in use.

The model behind the seam is not, and three routes were measured rather than guessed:

**ML Kit (`com.google.mlkit:genai-prompt`)** — works, and the Kotlin 2.4.21 move unblocked it. Costs `+2.16 MB` on the APK, brings Play Services and `com.google.android.datatransport:transport-backend-cct` (a telemetry upload transport) into a launcher that promises nothing leaves the device, needs `tools:overrideLibrary` in both manifests because the library is minSdk 26 against the project's 24, and only runs where AICore supports Gemini Nano, never on an unlocked bootloader.

**MediaPipe (`com.google.mediapipe:tasks-text`)** — rejected. `tasks-core` pulls the same `transport-backend-cct`, so it does not avoid the telemetry transport, and it costs 16 MB of native code on arm64 alone.

**LiteRT (`com.google.ai.edge.litert:litert:1.4.2`)** — the only genuinely clean runtime: its sole dependency is `litert-api`, no Play, no telemetry, 4.5 MB native on arm64. Pin 1.x; 2.x adds `com.google.android.play:ai-delivery`. The blocker is the model, not the runtime. The two small text models served publicly do not fit: `universal_sentence_encoder.tflite` (5.9 MB) references `RaggedTensor`, so it needs the select-TF-ops delegate, which is tens of MB; `average_word_classifier.tflite` (758 KB) runs on builtin ops and carries its own vocab, but it is a trained sentiment classifier whose head cannot be relabelled without retraining.

**A linear model in pure Kotlin** — built and measured here, and it does not earn its place yet. Logistic regression over hashed character n-grams, 347 hand-written labelled titles in `scripts/triage/`, trained with `scripts/triage/train.py`, five-fold cross-validation:

| threshold | noise caught (of 180) | intentions wrongly dropped (of 167) |
|-|-|-|
| 0.95 | 93 (51.7%) | 4 |
| 0.99 | 48 (26.7%) | 0 |

At a threshold safe enough to lose no real message it catches about a quarter of the noise, which the pattern list already covers with far better precision. At a threshold where it catches half, it silently eats 2.4% of real messages, which this product cannot do. And "zero dropped" rests on 167 examples, so the true rate could still be near 2%.

The failures say what is missing. Out of fold the model scored `Tom: alarm did not go off, running late` at 0.989 noise and `Meeting starts in 10 minutes` at 0.93: exactly the boundary where a message mentions a housekeeping word. 347 invented examples cannot teach that.

**What would unblock it** is data, and Will is sitting on the source. The launcher already sees every notification, and `TaskEvent` already records what the user does with each task. An opt-in, on-device record of titles against keep-or-dismiss gives a training set that is both larger and drawn from the user's own stream rather than from someone's guesses. That is the prerequisite, and it is a user story of its own before any model is worth adding.

### US-14: AppFunctions surface ⬜ deferred
**As a user, I want an assistant or agent on my phone to be able to put work on my home screen and see what I am holding, without anything leaving the device.**

Built once and reverted (`961682c`, reverted). It worked: four functions, 16 unit tests, and the generated metadata verified in the APK. It was reverted because the cost does not pay off yet:

- `androidx.appfunctions:appfunctions:1.0.0-beta01` refuses to build against `compileSdk 36` and forces 37
- about 1.8 MB of transitive dependencies (appsearch, guava) on a launcher that cares about size
- beta API, explicitly allowed to break
- the Gemini side of the pipeline was a private preview with trusted testers as of May 2026, so almost nothing can call it

`will://task` from US-11 covers agent writes meanwhile, on `minSdk 24`, with none of that.

**What the beta01 processor actually requires**, so the next attempt does not rediscover it. The published example at developer.android.com/ai/appfunctions is out of date:

- the annotation is `@AppFunctionDeclaration`, not `@AppFunction` (that name is now an interface)
- functions must be members of the class annotated `@AppFunctionServiceEntryPoint`; top-level functions are rejected
- that class must be abstract and must hold at least one function
- functions take **no** `AppFunctionContext` parameter, despite the processor carrying a message that reads "the first parameter of an app function must be"
- parameter and return types must be annotated `@AppFunctionSerializable`
- the AppFunctions exception types cannot be constructed in a JVM unit test (`NullPointerException: EMPTY must not be null`), so keep the behaviour in a plain class throwing plain Kotlin exceptions and translate at the service boundary. That also keeps framework types out of the task layer, which the architecture already requires
- the service needs `android:permission="android.permission.BIND_APP_FUNCTION_SERVICE"` plus the `android.app.appfunctions.AppFunctionService` action; a caller needs `android.permission.EXECUTE_APP_FUNCTIONS`
- lint fails on the generated service without `tools:targetApi="36"`, since its platform superclass only exists there
- verify registration with `adb shell cmd app_function list-app-functions`

When to pick it up: AppFunctions reaches stable, or Gemini's side opens up, whichever comes first. Open question to settle before then: whether `PrivacySettings` should list which callers are allowed, which is the US-07 question this raises.

## Technical Debt and Improvement Tasks

### TD-01: Performance Optimization
- [ ] Optimize Room database queries for large task sets
- [ ] Implement pagination for task feed loading
- [ ] Use WorkManager for background cleanup tasks
- [ ] Optimize AccessibilityService to minimize battery impact
- [ ] Implement efficient diffing algorithms for UI updates
- [ ] Use proper lifecycle observers to prevent memory leaks

### TD-02: Testing and Quality Assurance
- [ ] Write unit tests for Task model and repository
- [ ] Create integration tests for notification processing
- [ ] Implement UI tests for core user flows
- [ ] Add automated accessibility testing
- [ ] Create performance benchmarks for key operations
- [ ] Implement error reporting and crash analytics (opt-in)

### TD-03: Polish and User Experience
- [ ] Implement proper error handling and edge cases
- [ ] Add animations and transitions for smoother UX
- [ ] Ensure accessibility compliance (content descriptions, touch targets)
- [ ] Support dark theme and system theme settings
- [ ] Implement proper backup and restore functionality
- [ ] ~~Add first-run experience and feature tour~~ → tracked as US-10
- [ ] Create settings screen for all configurable options
- [ ] Implement proper app shortcuts and widgets
- [ ] Add support for different screen sizes and orientations