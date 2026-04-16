# Conventions
> Design System Analysis | Generated: 2026-04-16

## Naming Patterns

| Convention | Value |
|------------|-------|
| Components | PascalCase filename matches exported composable name — `TaskFeed.kt` → `fun TaskFeed(...)` |
| Private subcomponents | PascalCase, declared in same file as parent — `private fun TaskItem(...)` |
| Files | PascalCase for composables; camelCase for non-UI Kotlin (e.g. `formatTime`) |
| Utilities | camelCase functions, no dedicated utils directory |
| Resource files | `snake_case.xml` (Android convention) |
| Packages | lowercase dot-separated — `tech.benedicta.will.ui` |

## Export Style

| Pattern | Usage |
|---------|-------|
| Default exports | Not used (Kotlin doesn't have default exports) |
| Named exports | All composables and classes are named — direct imports |
| Barrel files | Not used — no `index.kt` or re-export files |

## Styling Approach

| Aspect | Value |
|--------|-------|
| Primary method | Compose `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*` for semantic tokens; direct `Color(0xFF...)` literals for non-theme values |
| Class merging | Not applicable — Compose uses `Modifier` chaining |
| Component styling | `modifier: Modifier = Modifier` parameter pattern, augmented with `.padding()`, `.fillMaxWidth()`, etc. |
| Responsive approach | `WindowInsets.safeDrawing.asPaddingValues()` for edge-to-edge layout — no breakpoints (portrait-only launcher) |

## Import Aliases

| Alias | Maps to |
|-------|---------|
| None | Direct package imports only — no `@/` or `~/` aliases |

## File Organization

**Pattern:** Feature-based

```
tech.benedicta.will/
├── ui/               — All Compose UI components + Theme
│   └── theme/        — WillTheme, ColorScheme
├── task/             — Domain model: Task, TaskRepository (interface), TaskViewModel
├── notification/     — NotificationProcessor, NotificationActionStore, WillNotificationListenerService
├── launcher/         — LauncherApp, AppRepository (interface), SystemAppRepository, LauncherViewModel
├── infrastructure/
│   ├── room/         — TaskEntity, TaskDao, WillDatabase, RoomTaskRepository
│   └── memory/       — MemoryTaskRepository (test double)
└── overlay/          — ActiveTaskOverlayService
```

Domain interfaces live with domain classes. Infrastructure implementations live under `infrastructure/`. This separation is intentional — domain classes have zero Android/Room imports.

## Where to Add

| Type | Location | Naming | Example |
|------|----------|--------|---------|
| Composable component | `ui/` | `PascalCase.kt` | `TaskCard.kt` |
| Theme token | `ui/theme/Theme.kt` | `Color(0xFF...)` constant | `val WillAccent = Color(0xFF4F6EF7)` |
| Font resource | `res/font/` | `snake_case.ttf` + `.xml` | `geist_sans_regular.ttf` |
| Domain model/interface | `{feature}/` | `PascalCase.kt` | `task/TaskFilter.kt` |
| ViewModel | `{feature}/` | `{Feature}ViewModel.kt` | `task/TaskViewModel.kt` |
| Room entity/DAO | `infrastructure/room/` | `{Name}Entity.kt`, `{Name}Dao.kt` | `infrastructure/room/TaskDao.kt` |
| Test double | `infrastructure/memory/` | `Memory{Name}Repository.kt` | `MemoryTaskRepository.kt` |
