# changelog

## [unreleased]

### added
- pin up to 5 apps to the home screen — long-press an app in the drawer to pin or unpin it; pinned apps live above the task feed for one-tap launch
- visible checkbox to mark tasks as done — no more hidden gestures
- separate "done" screen accessible from top bar — completed tasks live off the main feed
- apps button in top bar — no need to discover the swipe-up gesture
- conversation dedup via database unique index — same conversation never creates duplicate tasks, even after app restart

### fixed
- tapping a task now opens the specific conversation (WhatsApp, Calendar, etc.) instead of the app's home screen — notification key persisted in database
- notification filter toggle now updates the UI reactively
- replace broadcast with SharedFlow for overlay→activity communication
- active task overlay shows the correct tapped task
- overlay actions (done/delete/later) now reach MainActivity

### removed
- swipe-to-done and swipe-to-delete gestures — caused accidental actions
- long-press to action sheet — hidden gesture replaced by visible controls
- system noise notifications (alarms, connectivity, charging, OTA updates) no longer create tasks

## [0.1.0] — 2026-04-15

### added
- basic launcher (app grid, swipe-up gesture, launch apps)
- notification capture (NotificationListenerService → Task → Room)
- task feed (HomeScreen): tap to open source app, long-press for action sheet
- Room database with TaskEntity, TaskDao, WillDatabase
- active task overlay service (TYPE_APPLICATION_OVERLAY)
