# File References
> Phase: brief | Project: Will | Generated: 2026-04-16

## Theme & Tokens

| File | Purpose |
|------|---------|
| `app/src/main/java/tech/benedicta/will/ui/theme/Theme.kt` | `WillDarkColors`, `WillTypography`, `WillShapes`, `Spacing`, `WillTheme()`, brand color constants |

---

## UI Components

| File | Components |
|------|-----------|
| `app/src/main/java/tech/benedicta/will/ui/TaskFeed.kt` | `TaskFeed` (public), `TaskItem` (private) |
| `app/src/main/java/tech/benedicta/will/ui/TaskActionSheet.kt` | `TaskActionSheet` (public), `SheetAction` (private) |
| `app/src/main/java/tech/benedicta/will/ui/HomeTopBar.kt` | `HomeTopBar` (public), `formatTime()`, `formatDate()`, `batteryPercentage()` |
| `app/src/main/java/tech/benedicta/will/ui/AppGrid.kt` | `AppGrid` (public), `SearchField` (private), `AppItem` (private) |
| `app/src/main/java/tech/benedicta/will/ui/NotificationPermissionPrompt.kt` | `NotificationPermissionPrompt` (public) |

---

## Domain & Infrastructure

| File | Purpose |
|------|---------|
| `app/src/main/java/tech/benedicta/will/task/Task.kt` | Domain model — add `status`, `startedAt`, `completedAt`, `contextSwitchCount` fields for US-04 |
| `app/src/main/java/tech/benedicta/will/task/TaskViewModel.kt` | `StateFlow<List<Task>>`, `addTask`, `deleteTask`, `markActive` — extend for lifecycle state |
| `app/src/main/java/tech/benedicta/will/infrastructure/room/TaskEntity.kt` | Room entity — needs migration for new lifecycle fields |
| `app/src/main/java/tech/benedicta/will/infrastructure/room/TaskDao.kt` | DAO — needs queries for status filtering |

---

## Overlay

| File | Purpose |
|------|---------|
| `app/src/main/java/tech/benedicta/will/overlay/ActiveTaskOverlayService.kt` | Floating bubble + expanded panel — no changes in MVP |

---

## Entry Points

| File | Purpose |
|------|---------|
| `app/src/main/java/tech/benedicta/will/MainActivity.kt` | Wires ViewModels, broadcasts; add routing to new screens |
| `app/src/main/AndroidManifest.xml` | Add new service declarations for AccessibilityService (US-04), SpeechRecognizer permissions |
