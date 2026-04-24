# changelog

## [unreleased]

### fixed
- notification filter toggle now updates the UI reactively
- replace broadcast with SharedFlow for overlay→activity communication
- active task overlay shows the correct tapped task
- overlay actions (done/delete/later) now reach MainActivity

## [0.1.0] — 2026-04-15

### added
- basic launcher (app grid, swipe-up gesture, launch apps)
- notification capture (NotificationListenerService → Task → Room)
- task feed (HomeScreen): tap to open source app, long-press for action sheet
- Room database with TaskEntity, TaskDao, WillDatabase
- active task overlay service (TYPE_APPLICATION_OVERLAY)
