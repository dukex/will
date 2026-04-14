# TASKS.md - User Stories and Tasks for Will Project

## Prerequisite Tasks (Must be completed before any User Story)

### PT-01: Project Setup
- [x] Create Android Studio project with minimum SDK 21
- [x] Configure basic app structure with MainActivity
- [x] Set up AndroidManifest.xml with launcher category
- [x] Implement basic UI with Compose or XML layout
- [x] Configure Gradle dependencies for core AndroidX libraries

### PT-02: Core Architecture Foundation
- [ ] Design and implement Task data model
- [ ] Set up Room database for Task persistence
- [ ] Create Repository pattern for data access
- [ ] Implement basic Dependency Injection (Hilt or manual)
- [ ] Create ViewModel for Task management

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
- [ ] Show recent apps when long-pressing home button (system-level API restricted on Android 5+, deferred)

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

### US-03: Active Intent Processing via Voice/Text Input
**As a user, I want to create tasks by speaking or typing commands so I can quickly capture intentions without navigating apps.**

Subtasks:
- [ ] Implement voice input functionality (Speech-to-Text)
- [ ] Implement text input field for manual command entry
- [ ] Create NLP pipeline for intent classification
- [ ] Implement tokenization and Named Entity Recognition (NER)
- [ ] Map recognized intents to appropriate actions (internal/external)
- [ ] For internal actions: create reminder/task in Will
- [ ] For external actions: generate appropriate deep links or intents
- [ ] Display processing feedback during NLP analysis
- [ ] Show suggested actions based on recognized intent
- [ ] Allow editing/correcting recognized commands
- [ ] Learn from user corrections to improve accuracy over time

### US-04: Task Lifecycle Tracking and Completion Detection
**As a user, I want Will to automatically track when I start and complete tasks so I can measure my productivity accurately.**

Subtasks:
- [ ] Implement startedAt timestamp when user engages with a task
- [ ] Create AccessibilityService to monitor app usage
- [ ] Detect when user switches between apps (context switching)
- [ ] Increment contextSwitchCount for each app transition
- [ ] Identify task completion signals in third-party apps (e.g., "Sent", "Success" buttons)
- [ ] Automatically set completedAt when task completion is detected
- [ ] Allow manual task completion marking
- [ ] Handle cases where automatic detection fails
- [ ] Provide visual feedback on task status (pending, in-progress, completed)
- [ ] Implement task expiration/stale task handling

### US-05: Productivity Metrics Dashboard
**As a user, I want to see visualizations of my task completion patterns so I can understand and improve my productivity.**

Subtasks:
- [ ] Calculate Time-on-Task (completedAt - startedAt) for each task
- [ ] Implement GOMS model to estimate "Carga Estranha" (cognitive load saved)
- [ ] Create daily focus score based on task completion metrics
- [ ] Build dashboard using ComposeCharts or similar library
- [ ] Display time-on-task distribution (bar chart, histogram)
- [ ] Show context switch frequency over time
- [ ] Visualize focus score trends (line graph)
- [ ] Provide filtering by date range (day, week, month)
- [ ] Allow exporting metrics data
- [ ] Implement goal setting and progress tracking features

### US-06: Reactive Task Feed with Deep Linking
**As a user, I want a clean, text-focused interface that updates in real-time and takes me directly to the right app functionality.**

Subtasks:
- [ ] Implement StateFlow/SharedFlow for reactive UI updates
- [ ] Create vertical stream of task cards (text-focused, minimal icons)
- [ ] Design task cards to show essential info: title, source app, time
- [ ] Implement priority-based task ordering (urgent, today, scheduled)
- [ ] Add swipe actions for quick task management (complete, snooze, etc.)
- [ ] Implement deep linking to jump directly to relevant app screens
- [ ] Create App Actions integration for common tasks
- [ ] Handle clipboard monitoring for entities (barcodes, contact names)
- [ ] Provide visual feedback when deep linking is attempted
- [ ] Allow customization of feed density and information shown
- [ ] Implement search/filter functionality within the task feed

### US-07: Privacy-First On-Device Processing
**As a user, I want all my task data processed locally on my device so my privacy is protected.**

Subtasks:
- [ ] Ensure all NLP processing happens on-device (no network calls)
- [ ] Store all Task data exclusively in Room database (local only)
- [ ] Prevent any task metadata from leaving the device
- [ ] Implement transparent privacy settings explaining data usage
- [ ] Provide option to delete all task history
- [ ] Allow selective deletion of tasks by time range or app
- [ ] Implement data encryption for sensitive task information
- [ ] Ensure accessibility service data is not logged or transmitted
- [ ] Provide clear explanation of required permissions and why
- [ ] Implement regular local backups option (to device storage only)

### US-08: Notification Filter Settings
**As a user, I want to control which apps can create tasks from notifications so my feed stays relevant.**

Subtasks:
- [ ] Create settings screen accessible from the task feed
- [ ] List all apps that have sent notifications, with enable/disable toggle
- [ ] Persist filter preferences in Room or SharedPreferences
- [ ] Apply filter in WillNotificationListenerService before creating tasks

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
- [ ] Add first-run experience and feature tour
- [ ] Create settings screen for all configurable options
- [ ] Implement proper app shortcuts and widgets
- [ ] Add support for different screen sizes and orientations