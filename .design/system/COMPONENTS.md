# Components
> Design System Analysis | Generated: 2026-04-16

## UI Kit Detection

**UI Kit:** Material3 (Compose)
**Source:** `androidx.compose.material3` in `app/build.gradle.kts` — `ModalBottomSheet`, `HorizontalDivider`, `Surface`, `Button`, `Text`, `MaterialTheme`

## Existing Components

| Component | Path | Props / Variants | Reusable? | Notes |
|-----------|------|------------------|-----------|-------|
| `TaskFeed` | `ui/TaskFeed.kt` | `tasks`, `onTap`, `modifier` | Yes | `LazyColumn` with `TaskItem` subcomponent; handles empty state inline |
| `TaskItem` | `ui/TaskFeed.kt` | `task`, `onTap` (private) | No | Shows title, body text, timestamp, app name. Uses `FontWeight.SemiBold` — misaligns with brand weight policy |
| `TaskActionSheet` | `ui/TaskActionSheet.kt` | `task`, `onDone`, `onLater`, `onDelete`, `onDismiss` | Yes | `ModalBottomSheet` — done/later/delete; `SheetAction` subcomponent |
| `SheetAction` | `ui/TaskActionSheet.kt` | `label`, `color`, `onClick` (private) | No | Clickable row with `bodyLarge` text; color passed as parameter |
| `HomeTopBar` | `ui/HomeTopBar.kt` | `activeTask`, `onTaskDone`, `onTaskLater`, `onTaskDelete`, `modifier` | Partially | Time/date/battery display + active task inline actions. Heavy — BroadcastReceiver for battery, timer coroutine |
| `AppGrid` | `ui/AppGrid.kt` | `apps`, `query`, `onQueryChange`, `modifier` | Yes | `LazyColumn` with `SearchField` + `AppItem` subcomponents |
| `SearchField` | `ui/AppGrid.kt` | `query`, `onQueryChange`, `modifier` (private) | No | `BasicTextField` with custom placeholder overlay |
| `AppItem` | `ui/AppGrid.kt` | `app`, `onLaunch` (private) | No | Single text clickable; launches app via PackageManager |
| `NotificationPermissionPrompt` | `ui/NotificationPermissionPrompt.kt` | `onGrantPermission`, `modifier` | Yes | `Surface` banner with `Button`. Uses `errorContainer` color scheme slot |
| `ActiveTaskOverlayService` | `overlay/ActiveTaskOverlayService.kt` | Service — not a Composable | No | `TYPE_APPLICATION_OVERLAY` window; draggable bubble → expanded panel with done/later/delete |

## Where to Add

| Type | Location | Pattern |
|------|----------|---------|
| UI component | `app/src/main/java/tech/benedicta/will/ui/` | PascalCase filename, functional `@Composable`, private subcomponents in same file |
| Domain class | `app/src/main/java/tech/benedicta/will/{feature}/` | Feature directory, pure Kotlin (no Android/Room deps in domain classes) |
| ViewModel | `app/src/main/java/tech/benedicta/will/{feature}/` | Feature directory, `ViewModel` subclass, injected `CoroutineDispatcher` |
| Infrastructure | `app/src/main/java/tech/benedicta/will/infrastructure/{store}/` | Implementation of domain interfaces |
| Theme token | `app/src/main/java/tech/benedicta/will/ui/theme/Theme.kt` | Add `Color` constants, extend `darkColorScheme()` / `lightColorScheme()` |
| Font | `app/src/main/res/font/` | `snake_case.ttf` + font family XML |
