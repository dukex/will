# changelog

## [unreleased]

### added
- first-run onboarding — Will now walks you through making it the default launcher, granting the active-task overlay, and (optionally) enabling the notification listener, instead of dropping you into a wall of system dialogs. you can re-run the flow any time from settings.
- distinctive Will icon — a flat geometric "W" stroke on true black replaces the default platform mark. ships with a monochrome variant so Android 13+ themed icons inherit your system tint.
- pin up to 5 apps to the home screen — long-press an app in the drawer to pin or unpin it; pinned apps live above the task feed for one-tap launch
- visible checkbox to mark tasks as done — no more hidden gestures
- separate "done" screen accessible from top bar — completed tasks live off the main feed
- apps button in top bar — no need to discover the swipe-up gesture
- conversation dedup via database unique index — same conversation never creates duplicate tasks, even after app restart
- unread counter on tasks — when more than one notification piles up for the same conversation, the task shows "N new" so you know how many messages are waiting

### changed
- pinned apps and the task feed now share one scroll area — the pinned row no longer holds a fixed slice at the top of the home screen, so the feed reclaims the space when you have messages waiting and pinned apps stay reachable by scrolling up

### fixed
- WhatsApp and other messengers no longer spam the feed — repeated notifications from the same chat collapse into a single task and the tap always opens the most recent message
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
