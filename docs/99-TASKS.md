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
- [x] Visualise attention across days — `AttentionHistory` reads the event log into one `DayOfAttention` per day and `FocusDashboard` draws it as a bar per day. A bar chart rather than a line: the days are discrete and a line between them would imply attention flowing from one to the next
- [x] Filtering by date range (week, month) — `AttentionRange`, picked on the screen; `TaskEventRepository.eventsSince` is the input, queried one day wider than the window so a stretch that began just before it still opens inside it
- [ ] ~~Export metrics~~ → out of MVP per scope.md
- [ ] ~~Goal setting / progress tracking~~ → out of MVP per scope.md

**What the log made possible, and what the scalars never could.** The dashboard
used to read `Task` alone, so "time on task" meant `completedAt − startedAt`:
a task opened on Monday and finished on Thursday counted as three days of
attention, including the two nobody touched it. `AttentionHistory` replays the
event log instead and attributes each stretch to the day it actually happened
on. Three rules make the figures honest, and each is a test:

- overlapping stretches are merged before anything is measured, so two tasks
  open across the same hour cost one hour, not two;
- a stretch crossing midnight is split between both days in the proportion it
  fell in each;
- a stretch still open closes at `now`, so a task left running overnight
  cannot claim time that has not passed.

**Fixed on the way:** the whole screen lived inside the branch that renders
when something happened today, so a quiet day showed "nothing yet" over a full
week of history. Same shape as the settings-screen bug under US-25. The empty
state now covers the screen only when there is no history either.

**Still reasoned rather than measured:** the focus score's weights (50 / 30 /
20), its four-hour target day and its thirty-switch ceiling. They are named
constants now instead of literals buried in the arithmetic, and the tests pin
the properties that matter (the score stays in 0..100, finishing beats not
finishing) rather than the exact number, so the weights can move without
rewriting the suite.

### US-06: Reactive Task Feed with Deep Linking
**As a user, I want a clean, text-focused interface that updates in real-time and takes me directly to the right app functionality.**

Subtasks:
- [x] Implement StateFlow for reactive UI updates (`TaskViewModel.tasks`)
- [x] Create vertical stream of task cards, text-focused, minimal icons (`TaskFeed`)
- [x] Design task cards to show essential info: title, source app, time
- [x] Lifecycle-based ordering (active → pending → done-today)
- [x] Implement deep linking to jump directly to relevant app screens (PendingIntent + `notificationKey` fallback)
- [ ] Provide visual feedback when deep linking is attempted (toast/state when PendingIntent is fired)
- [x] Implement search/filter functionality within the task feed — done in US-24, through the drawer's search box rather than new chrome on the home screen
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
- [x] Surfaced. `AttentionHistory` is the second reader of the log after `CompletionInference`, and the first to use it for something the `Task` scalars cannot answer at all: which days the user actually spent attention on, rather than when a task began and ended (19 unit tests)
- [x] `SWITCHED_AWAY` carried the app the user jumped to from the start and nothing ever read it, so Will was recording what interrupts people and never telling them. `AttentionHistory.distractions` ranks them and the focus screen names them (11 unit tests). Package names and counts only, which is the same surface the accessibility service already exposes
- [x] The longest unbroken stretch is on the screen next to the total: the total says how much attention the range held, this says whether any of it was in one piece
- [ ] The cognitive-load estimate is still open. `DayOfAttention` carries the per-day switch counts and longest stretch that GOMS would need, and both now have a reader, so nothing is being computed on spec for it

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

- [x] The same matcher over the task feed — shipped under US-24, which put tasks and apps in one box rather than adding a field to the home screen.
- [x] Searching what the notification said, not only the title it was given. `TaskMatcher` reads `title`, `rawTitle` and `text`, ranked so any title hit beats any message hit. The message is matched literally: `hasSubsequence` over a paragraph matches nearly any short query, which would make every task match everything (6 tests, one of them that exact trap).
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
- [x] Richer kinds than reply and call back, read by the on-device model. `IntentionKind` gained `CONFIRM`, `PAY` and `ATTEND`; `IntentionPrompt` asks Gemini Nano which one a notification is asking for and `ReadIntentionWithMeaning` consults it only when no category answered (25 unit tests). Opt-in, off by default, and separate from the search switch.

### US-20: Will learns what you throw away ✅
**As a user, I want Will to stop showing me the notification I keep deleting, without me having to go and configure anything.**

The pattern list knows the housekeeping notifications someone wrote down in advance. It cannot know that *this* phone gets a weekly promo from one shop that this user never once acted on. That part has to be learned from the user, and the signal already exists in what they do.

Subtasks:
- [x] `LearnedNoiseRepository`: counts dismissals per app and title, with a SharedPreferences implementation capped at 200 entries (13 unit tests)
- [x] The SharedPreferences implementation is tested too, not just its in-memory twin (11 tests under Robolectric, `SharedPrefsLearnedNoiseRepositoryTest`). The twin cannot answer the three questions a real store can get wrong: whether a lesson survives the process dying, whether a title containing the `|` separator comes back intact, and whether the cap evicts the single-dismissal filler rather than the title dismissed five times.
- [x] Three dismissals silence a title; one completion unlearns it, because acting on one proves it mattered
- [x] The lesson is per app **and** per title: silencing one promo must not silence the whole app
- [x] Titles are folded for case, accents and spacing, so a vendor reformatting its own message does not reset the count
- [x] `LearnedIntentionClassifier` composes it with the patterns; cheap check first (7 unit tests)
- [x] Taught by actions the user already performs: deleting an unfinished notification task teaches noise, finishing one teaches the opposite
- [x] Only notification tasks teach. A task you typed or an app shared in is never a complaint about a notification
- [x] Tidying up an already-finished task from the done screen teaches nothing
- [x] Visible and reversible: `data & privacy` lists what was silenced, with "show again" per entry
- [x] "delete all task history" forgets everything learned
- [x] End-to-end test of the loop as the user lives it (6 tests)
- [x] Rendered and checked before handover (`PrivacySettingsSilencedPreview`)

This is the "learn from the user's own stream" idea from US-15, built without a model. Three dismissals is a reasoned threshold, not a measured one.

- [ ] The same signal, kept as labelled examples, is what US-15's model needs. Nothing here stores enough for that yet, and doing so is a privacy decision of its own.

### US-21: What you are working on, reachable from outside Will ✅
**As a user, I want a long press on the Will icon to offer the task I am in the middle of.**

Completes the agent-surface list from the AI-readiness direction. `will://task` lets anything put a task in; this lets the system read out the one task that matters right now.

Subtasks:
- [x] `ActiveTaskShortcut`, pure, deciding what to publish and how to label it (11 unit tests)
- [x] Only a task that is actually in progress; nothing finished and nothing untouched
- [x] A stable shortcut id, so the shortcut is replaced rather than piling up
- [x] Labels cut to what the system renders, rather than the shortcut being rejected
- [x] `ActiveTaskShortcutPublisher` pushes and removes it, following the active task
- [x] Failures are swallowed: no launcher should crash because a system surface refused a shortcut
- [x] No new dependency; `androidx.core` was already here

- [x] Checked, not just written down. `DeviceFeaturesJvmTest` runs on the JVM under Robolectric, so `./gradlew test` proves the shortcut reaches the system, carries the right task id, does not pile up when published twice, and goes away when the task does. The publisher swallows every failure by design, so these read the shortcut back out of the system rather than asking it not to crash. **Executed here: 9 tests, 0 failures.** `DeviceOnlyFeaturesTest` keeps the same checks against a real phone's own `ShortcutManager`, via `./gradlew connectedDebugAndroidTest`.

### US-22: An intention opens the screen, not the app ✅
**As a user, when a task says "ligar para Miguel" I want the tap to open the dialer with Miguel in it, not the phone app's front door.**

`01-IDEA.md` asks for deep links. US-17 got the task as far as the right app; this gets it to the right screen for the part Android standardises, where the user's own verb names the action.

Subtasks:
- [x] `IntentionActionResolver`, pure, reading the verb that starts the intention (15 unit tests)
- [x] Calling, writing, going somewhere and opening a link map to `ACTION_DIAL`, `ACTION_SENDTO`, a `geo:` link and `ACTION_VIEW`
- [x] Portuguese and English verbs, accent- and case-insensitive
- [x] The verb has to start the intention: "a ligação com o Miguel caiu" is about a call, not an instruction to make one
- [x] English trailing particles handled, so "call Sarah back" is about Sarah
- [x] A link anywhere in the text wins, because a URL is unambiguous
- [x] `ACTION_DIAL`, never `ACTION_CALL`: the dialer opens with the number ready and the user presses call. Will does not place calls on anyone's behalf, and needs no permission
- [x] `IntentionActionLauncher` falls back to opening the app when nothing on the phone handles the action

- [ ] Resolving an intention to a screen *inside a third-party app*, which would mean reading each app's declared intent filters and knowing what they mean. Not attempted.

### US-23: Searching by what an app is for ✅
**As a user, I want to type "musica" and find Spotify, without knowing what I installed.**

Android attaches a category to each app, so this is searching by meaning without anyone guessing what a word implies.

Subtasks:
- [x] `AppCategory` on `LauncherApp`, read from `ApplicationInfo.category` (API 26+, `UNKNOWN` below that)
- [x] Portuguese and English words per category in `AppMatcher`
- [x] A name match always beats a category match
- [x] An app with no declared category is not found this way

The limit is Android's list, and it is worth stating: there is no category for money, so "banco" finds nothing. That gap is what embeddings would close, and they are still blocked under US-15.

### US-24: One search box over tasks and apps ✅
**As a user, I want the search I already use for apps to find the task I am looking for too.**

Closes the feed-search item that US-06 had left open, without putting a search field on the home screen: the drawer is already a search surface, and the home screen stays free of app chrome as the brand requires.

Subtasks:
- [x] `TaskMatcher` sharing the drawer's rules through `SearchText`, so one box behaves one way (9 unit tests)
- [x] Matching tasks appear above the apps, because a task is more specific than the app it lives in
- [x] Finished tasks are left out; they have their own screen
- [x] An empty query finds no tasks: the feed is already on screen
- [x] Capped at five results so the search never buries the apps
- [x] Rendered and checked before handover (`AppGridWithTaskResultsPreview`)
- [x] Fixed on the way: `AppItem` was the one text in the launcher that inherited its colour instead of stating it, which made it invisible outside a `Surface`

### US-25: Search by meaning ✅ (shipped, inference unverified on device)
**As a user, I want to type "banco" and find Itaú, even though the word is nowhere in the name.**

The last item of the direction document's layer 3, and the one `AppMatcher` could never reach: no amount of string comparison connects "banco" to "Itaú". This is the piece that needs a model, and the route was the one already agreed: accept Google's on-device model in the Play Store build, opt-in at runtime.

Subtasks:
- [x] `com.google.mlkit:genai-prompt` (Gemini Nano through AICore). `+2.3 MB` on the APK
- [x] `tools:overrideLibrary` in both manifests: the library declares minSdk 26, Will stays installable on 24
- [x] `SemanticSearchPrompt`, pure: numbers the apps, asks for numbers back, reads the reply (13 unit tests)
- [x] The answer can only ever name an app that was offered. Prose, invented names, out-of-range numbers and a model that lists everything all yield nothing or a bounded subset
- [x] `SearchWithMeaning`: the name match runs first and the model is asked only when it found nothing (8 unit tests)
- [x] The model cannot remove a result the matcher found, cannot name an app that is not installed, and when it is off, unavailable, slow or broken the search behaves exactly as before
- [x] Two-second timeout: a search box that waits longer has already failed
- [x] Never downloads the model on its own. Megabytes over someone's connection is not a search box's decision
- [x] Off until asked for, with a row in settings saying plainly that turning it on brings Google's libraries
- [x] `data & privacy` says the same, and still says searches go nowhere
- [x] Rendered and checked before handover (`SettingsSemanticSearchOffPreview`, `...OnPreview`)

- [x] Offer the model download from settings when the status is DOWNLOADABLE. `SemanticSearchStatus` keeps the five states apart (9 unit tests) so settings can never show "on" while nothing happens, and the download only ever starts because the user tapped it.
- [x] The half that does not need AICore is checked here. `DeviceFeaturesJvmTest` pins what a machine with no model must do: `availability()` answers `UNAVAILABLE` instead of throwing, `rank` comes back empty, `download()` leaves the model absent rather than crashing, and `SearchWithMeaning` still finds Itaú from "itau" while "banco" finds nothing invented. That is the promise the whole feature rests on, and it is now a test rather than a claim. **Executed here, inside the 9 above.**
- [ ] Whether Gemini Nano answers *well* is the one question left, and only a phone with AICore can settle it. It is now a test rather than something to remember to look at: `ModelAnswersWellTest` asks the real model the obvious cases (a download finishing is noise, a message from a person never is, "Confirme sua presença" reads as something to confirm) and skips rather than fails without AICore. `./gradlew connectedDebugAndroidTest` runs it. Not run here: no emulator in this container (no `/dev/kvm`, no `vmx`/`svm`), and no device attached. If any of those fail on a real phone, the prompts need work before either model switch is worth offering.

**Fixed on the way, and worth its own line:** the entire settings screen lived inside the branch that renders when at least one app has sent a notification. On a fresh install, `data & privacy`, `re-run onboarding` and `lifecycle tracking` were all unreachable. The apps section now carries its own empty state and the rest of the screen is always there.

### US-15: On-device notification triage ✅ done, with the audit surface that makes it allowed

**As a user, I want Will to recognise housekeeping notifications it has never seen before, so the feed stays clean without me maintaining a list of patterns.**

Shipped: `TriagePrompt` asks Gemini Nano whether a notification is housekeeping, `ModelIntentionClassifier` places it behind the cheap rules, and `ModelTriageLog` makes every drop visible and undoable. 43 unit tests, 10 of them against the store that ships, under Robolectric.

The order is the design:

1. the patterns run first, then what the user taught Will by dismissing things. Both are free and more trustworthy than a guess, and most notifications never get past them;
2. a shape the user has already rescued once is never judged again;
3. only then is the model asked, and only when the switch is on.

**The reading is lopsided because the costs are.** Only an unambiguous verdict of noise drops anything. Silence, prose, a hedge, two verdicts at once, a refusal, a timeout, an error and an answer in a shape the parser will not read all keep. The parser requires the answer to *be* the verdict rather than contain it, which a test caught: `{"verdict":"noise"}` matched a containment check, and a model answering in a shape it was not asked for is one to disregard rather than read creatively.

**The audit surface is the precondition, not a nicety.** CLAUDE.md says what Will learns to hide has to be visible and undoable. Every drop is written to the log before it takes effect, `data & privacy` lists it, and "show again" both restores it and stops the model judging that shape again, because an undo that only applied to the past would have the user undoing the same notification every week. "Delete all task history" takes the list and the rescues with it.

**Its own switch and its own prefs file**, off by default. This is the only switch in Will that can keep something from the user, so neither of the other two model switches carries it.

### The routes measured before any of that ⬜ kept for the record

**2026-11 correction.** "Blocked on data" was true of every route measured below, and all of them were *trained* classifiers: ML Kit, MediaPipe, LiteRT and the hand-rolled linear model each need labelled examples. A **prompt** needs none, and `com.google.mlkit:genai-prompt` has shipped since US-25 for semantic app search, so the size, the Play Services dependency, the telemetry transport and the `tools:overrideLibrary` in both manifests are already paid for. The conclusion below ("what would unblock it is data") held only because nobody asked the model in words.

That route is now built, and used for **reading** a notification rather than dropping one: `IntentionPrompt` + `ReadIntentionWithMeaning`, closing US-19's open item.

**Triage by model is still not done, and that is a decision rather than a gap.** Reading can only change a title, so a wrong answer costs a title the user disagrees with over an original `rawTitle` still holds. Dropping is different: a wrong answer is a message the user never sees and the feed cannot tell them about, which is the one failure this product has said repeatedly it will not accept. CLAUDE.md already sets the price of admission: "What Will learns to hide has to be visible and undoable", as US-20's silenced list is. A model-backed triage therefore needs its own audit surface, listing what it dropped with a way back, before it is worth building. That is the next story, not a line item.

### The routes measured before any of that ⬜ kept for the record
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

When to pick it up: AppFunctions reaches stable, or Gemini's side opens up, whichever comes first.

**Settled (2026-11): `PrivacySettings` should not grow a second permission surface.** Will already has one, the per-app filter in `SettingsScreen` that decides which apps may create tasks from notifications. When AppFunctions returns, a calling app goes through that same list rather than a parallel allowlist invented for it: two places to say "not this app" is one too many, and the second is the one nobody finds. The caller is already identifiable in the data, because `TaskOrigin.AGENT` records that a task was not the user's and `sourcePackage` records who sent it. What an agent can reach is narrow by construction and stays that way: tasks, never notifications, never the task log, never anything about which apps the user opens.

## Technical Debt and Improvement Tasks

### TD-01: Performance Optimization
- [ ] Optimize Room database queries for large task sets
- [ ] Implement pagination for task feed loading
- [ ] Use WorkManager for background cleanup tasks
- [ ] Optimize AccessibilityService to minimize battery impact
- [ ] Implement efficient diffing algorithms for UI updates
- [ ] Use proper lifecycle observers to prevent memory leaks

### TD-02: Testing and Quality Assurance

**Room migrations are replayed by `./gradlew test`, not only on a phone.** ✅
`WillDatabaseMigrationJvmTest` builds the version 7 `tasks` table in raw SQL,
runs the real migrations through to 10 and opens the database with Room.
Opening is the assertion: Room compares every migrated table against its
entity and throws when they differ, so SQL that drifts from the entity fails
in CI rather than on an upgrade. Robolectric gives it real SQLite on the JVM.

The test was checked against a deliberate break: changing the migrated
`rawTitle` column from `TEXT` to `INTEGER` fails all five of its cases. A
green migration test that cannot fail is worse than none, so this is worth
redoing whenever the migrations change shape.

Robolectric downloads its `android-all` jar with its own HTTP client. Where
that cannot reach the network, `./gradlew test -Probolectric.dependency.dir=DIR`
points it at a directory holding the jars and turns its offline mode on.

- [x] Write unit tests for Task model and repository
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