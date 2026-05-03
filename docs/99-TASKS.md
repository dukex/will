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

### US-03: Active Intent Processing via Text Input
**As a user, I want to create tasks by typing commands so I can quickly capture intentions without navigating apps.**

Subtasks:
- [x] Implement text input field for manual command entry (`IntentInputSheet`)
- [x] For internal actions: create reminder/task in Will
- [ ] Display processing feedback during NLP analysis (idle/processing/suggestion states in sheet)
- [ ] Create NLP pipeline for intent classification
- [ ] Implement tokenization and Named Entity Recognition (NER)
- [ ] Map recognized intents to appropriate actions (internal/external)
- [ ] For external actions: generate appropriate deep links or intents
- [ ] Show suggested actions based on recognized intent
- [ ] Allow editing/correcting recognized commands
- [ ] ~~Voice / Speech-to-Text input~~ → out of scope, will not be built
- [ ] ~~Learn from user corrections~~ → out of MVP per `.design/projects/will/brief/scope.md`

### US-04: Task Lifecycle Tracking and Completion Detection
**As a user, I want Will to automatically track when I start and complete tasks so I can measure my productivity accurately.**

Subtasks:
- [x] Add `startedAt`, `completedAt`, `contextSwitchCount`, `status` fields on Task/TaskEntity
- [x] Provide visual feedback on task status (`TaskStatusIndicator` — pending / in-progress / done)
- [x] Allow manual task completion marking (checkbox in TaskFeed; overlay actions)
- [x] Show lifecycle context in `TaskActionSheet` (state-aware copy and actions)
- [ ] Implement startedAt timestamp when user engages with a task (currently only via overlay activation — not via app launch detection)
- [ ] Create AccessibilityService to monitor app usage
- [ ] Detect when user switches between apps (context switching)
- [ ] Increment contextSwitchCount for each app transition
- [ ] Identify task completion signals in third-party apps (e.g., "Sent", "Success" buttons)
- [ ] Automatically set completedAt when task completion is detected
- [ ] Handle cases where automatic detection fails
- [ ] Implement task expiration/stale task handling

### US-05: Productivity Metrics Dashboard
**As a user, I want to see visualizations of my task completion patterns so I can understand and improve my productivity.**

Subtasks:
- [x] Calculate Time-on-Task (completedAt − startedAt) for each task (`FocusDashboardViewModel`)
- [x] Create daily focus score based on task completion metrics
- [x] Build dashboard using custom Canvas (no external chart lib)
- [x] Display time-on-task distribution (bar chart, normalized per-task)
- [x] Show context switch frequency (today's total)
- [ ] Implement GOMS model to estimate "Carga Estranha" (cognitive load saved)
- [ ] Visualize focus score trends across days (line graph)
- [ ] Provide filtering by date range (week, month) — currently only today
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
- [ ] Ensure all NLP processing happens on-device (no network calls) → enforce when US-03 NLP lands
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