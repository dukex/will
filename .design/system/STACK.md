# Stack
> App: app/ | Repo type: single
> Design System Analysis | Generated: 2026-04-16

## Classification

**Codebase type:** `existing`
**Rationale:** Real custom application logic — domain model (Task), notification capture pipeline, overlay service, ViewModels with injected dispatchers, and 5 production UI components. Not a scaffold.

## Tech Stack

| Layer | Value |
|-------|-------|
| Framework | Android (minSdk 24, targetSdk 36) + Jetpack Compose |
| Language | Kotlin |
| Styling | Compose Material3 theming (`WillTheme` → `MaterialTheme`) |
| UI Kit | Material3 (`ModalBottomSheet`, `HorizontalDivider`, `Surface`, `Button`) |
| Package Manager | Gradle (libs.versions.toml) |
| Build Tool | AGP + Kotlin Compose plugin + KSP |

## Architecture Patterns

| Pattern | Value |
|---------|-------|
| Component style | Functional `@Composable` — file-per-component, private subcomponents in same file |
| State management | `StateFlow` + `ViewModel` — no external state library, manual `ViewModelProvider.Factory` |
| Data fetching | Room Flow → ViewModel → UI via `collectAsStateWithLifecycle` |
| Routing | No navigation library — `MainActivity` controls two "screens" (task feed / app drawer) via a `pagerState` / `nestedScroll` gesture |
| File organization | Feature-based: `ui/`, `task/`, `notification/`, `launcher/`, `infrastructure/`, `overlay/` |

## Key Paths

| Path | Purpose |
|------|---------|
| Components | `app/src/main/java/tech/benedicta/will/ui/` |
| Theme | `app/src/main/java/tech/benedicta/will/ui/theme/Theme.kt` |
| Overlay | `app/src/main/java/tech/benedicta/will/overlay/ActiveTaskOverlayService.kt` |
| Domain | `app/src/main/java/tech/benedicta/will/task/` |
| Notification | `app/src/main/java/tech/benedicta/will/notification/` |
| Res values | `app/src/main/res/values/` (themes.xml, colors.xml — vestigial XML from project init, not used at runtime) |
| Fonts | `app/src/main/res/font/` — **does not exist yet** (Geist fonts not bundled) |
| Config | `app/build.gradle.kts`, `gradle/libs.versions.toml` |
