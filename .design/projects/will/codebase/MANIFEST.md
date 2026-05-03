# Codebase Manifest — Will
> Phase: review | Project: Will | Generated: 2026-04-16
> Status: partial (conditional pass — 4 accessibility issues outstanding)
> Branch: —
> PR: —

---

## Files Created

| File | Purpose |
|------|---------|
| `app/src/main/res/font/geist_sans_regular.ttf` | Geist Sans 400 |
| `app/src/main/res/font/geist_sans_medium.ttf` | Geist Sans 500 |
| `app/src/main/res/font/geist_mono_regular.ttf` | Geist Mono 400 |
| `app/src/main/java/tech/benedicta/will/task/TaskStatus.kt` | TaskStatus enum |
| `app/src/main/java/tech/benedicta/will/ui/SectionHeader.kt` | Shared labelSmall section header |
| `app/src/main/java/tech/benedicta/will/ui/TaskStatusIndicator.kt` | 8dp animated status dot |
| `app/src/main/java/tech/benedicta/will/ui/IntentInputSheet.kt` | Manual task creation sheet |
| `app/src/main/java/tech/benedicta/will/ui/NotificationFilterSettings.kt` | Per-app notification filter |
| `app/src/main/java/tech/benedicta/will/ui/FocusDashboard.kt` | Focus score + Canvas bar chart |
| `app/src/main/java/tech/benedicta/will/ui/PrivacySettings.kt` | Data transparency + delete history |
| `app/src/main/java/tech/benedicta/will/task/FocusDashboardViewModel.kt` | Focus metrics computation |
| `app/src/main/java/tech/benedicta/will/notification/AppNotificationFilter.kt` | App filter domain model |
| `app/src/main/java/tech/benedicta/will/notification/AppFilterRepository.kt` | Filter repository interface |
| `app/src/main/java/tech/benedicta/will/notification/AppFilterViewModel.kt` | Filter ViewModel |
| `app/src/main/java/tech/benedicta/will/infrastructure/SharedPrefsAppFilterRepository.kt` | SharedPrefs filter persistence |

## Files Modified

| File | Changes |
|------|---------|
| `app/src/main/java/tech/benedicta/will/ui/theme/Theme.kt` | Real Geist fonts, brand color constants, WillShapes, Spacing object |
| `app/src/main/java/tech/benedicta/will/task/TaskViewModel.kt` | startTask, pauseTask, reopenTask, deleteAllTasks |
| `app/src/main/java/tech/benedicta/will/task/TaskRepository.kt` | deleteAllTasks() added |
| `app/src/main/java/tech/benedicta/will/infrastructure/room/TaskDao.kt` | deleteAll() |
| `app/src/main/java/tech/benedicta/will/infrastructure/room/RoomTaskRepository.kt` | deleteAllTasks() |
| `app/src/main/java/tech/benedicta/will/infrastructure/memory/MemoryTaskRepository.kt` | deleteAllTasks() |
| `app/src/main/java/tech/benedicta/will/ui/HomeTopBar.kt` | Date + 3 icon buttons (add/filter/dashboard) |
| `app/src/main/java/tech/benedicta/will/ui/TaskFeed.kt` | Sectioned, SwipeToDismissBox, status indicators |
| `app/src/main/java/tech/benedicta/will/ui/TaskActionSheet.kt` | Lifecycle context, status-based actions, confirm-delete |
| `app/src/main/java/tech/benedicta/will/MainActivity.kt` | Navigation routing, screen state, all callbacks |

## Outstanding Issues (from QA Review)

See `.design/projects/will/review/issues.md` for full detail.

| ID | Severity | Summary |
|----|----------|---------|
| R-01 | critical | SectionHeader contrast fails WCAG AA |
| R-02 | critical | IntentInputSheet placeholder contrast fails WCAG AA |
| R-04 | critical | "history cleared" missing liveRegion (WCAG 4.1.3) |
| R-05 | important | TaskItem missing mergeDescendants + customActions |
| R-06 | minor | bodySmall uses GeistSans instead of GeistMono |
| R-07 | minor | Surface in MainActivity missing tonalElevation = 0.dp |
| R-08 | minor | NotificationPermissionPrompt uses tinted surface |
