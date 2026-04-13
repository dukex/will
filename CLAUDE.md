# Claude

## FIRST RULE: Let's discovery the project together.

Always update the CLAUDE.md every new discovery. The CLAUDE.md is a index to documentation at `./docs/` the documentation should be good or to Agents or to Humans.

## Index

| file | description |
|-|-|
| ./docs/01-IDEA.md | The main project idea, here we put the macro idea about the project, always read it to understand what we want to build |
| ./docs/99-TASKS.md | User Stories and Tasks for Will Project - organized by prerequisite tasks, user stories, and technical improvements |

## Current State

| item | status | notes |
|-|-|-|
| PT-01: Project Setup | ✅ done | minSdk 24, Compose, Room, KSP configured |
| PT-02: Core Architecture | ✅ done | Task model (Room entity), TaskDao, RoomTaskRepository, TaskViewModel |
| US-01: Basic Launcher | ✅ done | app grid + launch works; recent apps deferred (API restricted) |
| US-02: Notification Capture | ✅ done | NotificationListenerService → Task → Room; task feed with permission prompt; deferred: PendingIntent fire, notification removal/update, per-app filter |
| US-03+ | ⬜ not started | |

## Key Files

| file | purpose |
|-|-|
| `app/src/main/AndroidManifest.xml` | launcher intent filters, singleTask, clearTaskOnLaunch |
| `app/src/main/java/.../launcher/LauncherApp.kt` | app data model (packageName, name) |
| `app/src/main/java/.../launcher/AppRepository.kt` | interface for app list source |
| `app/src/main/java/.../launcher/SystemAppRepository.kt` | PackageManager impl, sorted by name |
| `app/src/main/java/.../launcher/LauncherViewModel.kt` | StateFlow<List<LauncherApp>>, IO dispatcher |
| `app/src/main/java/.../ui/AppGrid.kt` | adaptive grid composable, icon + name, tap to launch |
| `app/src/main/java/.../MainActivity.kt` | wires LauncherViewModel + TaskViewModel; tabs: Tasks / Apps |
| `app/src/main/java/.../task/Task.kt` | Room entity with createdAt, startedAt, completedAt, contextSwitchCount |
| `app/src/main/java/.../task/TaskDao.kt` | Room DAO — insert, getAll (Flow), update, delete |
| `app/src/main/java/.../task/WillDatabase.kt` | singleton RoomDatabase |
| `app/src/main/java/.../task/TaskViewModel.kt` | StateFlow<List<Task>>, addTask, deleteTask |
| `app/src/main/java/.../notification/NotificationProcessor.kt` | pure logic: notification data → Task (or null) |
| `app/src/main/java/.../notification/WillNotificationListenerService.kt` | intercepts notifications, inserts Tasks via Room |
| `app/src/main/java/.../ui/TaskFeed.kt` | LazyColumn of TaskCards; tap opens source app |
| `app/src/main/java/.../ui/NotificationPermissionPrompt.kt` | banner shown when listener permission not granted |
| `app/src/test/.../launcher/LauncherViewModelTest.kt` | unit tests with FakeAppRepository |
| `app/src/test/.../task/TaskTest.kt` | Task model field tests |
| `app/src/test/.../task/TaskRepositoryTest.kt` | TaskRepository contract tests |
| `app/src/test/.../task/TaskViewModelTest.kt` | TaskViewModel state tests |
| `app/src/test/.../notification/NotificationProcessorTest.kt` | processor mapping + edge case tests |
| `gradle/libs.versions.toml` | dependency catalog |
| `app/build.gradle.kts` | app-level build config |

## Architecture Decisions

- No Hilt — manual `ViewModelProvider.Factory` for DI
- `CoroutineDispatcher` injected into ViewModel for testability
- App icon loaded in composable (not in ViewModel) to keep model Android-framework-free
- `getRecentTasks()` deferred — restricted API on Android 5+
- `Task.pendingIntent` not persisted — Room can't store PendingIntent; tapping task opens source app as fallback
- `WillNotificationListenerService` uses its own `CoroutineScope(Dispatchers.IO + SupervisorJob)` — not tied to Activity lifecycle
- `WillDatabase` is a singleton created in Activity/Service via `WillDatabase.getInstance(context)`

## Development workflow

We work as XP (eXtreme Programming) pair programmers. These rules are **mandatory** and override all other defaults:

1. **[IMPORTANT] XP pair programming** — Claude is the pair, the user drives. All decisions are made together.
2. **[IMPORTANT] Tests first** — Before any implementation, write tests that reflect what the User Story expects. Tests must be red/failing before writing production code.
3. **Edge cases** — Always consider edge cases when writing test scenarios.
4. **INVEST User Stories** — All US must be Independent, Negotiable, Valuable, Estimable, Small, Testable. If a US is not INVEST, break it into smaller parts until it is.
5. **Baby steps** — Only write production code after tests are broken/red. Implement the minimum to make tests pass.
6. **TDD cycle** — Red → Green → Refactor. No exceptions.
7. **[IMPORTANT] E2E / regression tests** — After implementing a full feature or user flow, write an E2E test that exercises the integrated path. Unit tests catch isolated logic errors; E2E tests catch integration bugs that are invisible to unit tests.
8. **Atomic conventional commits** — Commit often, one logical change per commit. Use conventional commit format. `feat:` and `fix:` messages target the final user (they generate the changelog) — write them as user-facing descriptions, promoting the new feature, not technical notes.
9. **[IMPORTANT] Analytics on every feature** — Every user-facing feature must include at least one PostHog event. Server actions use `captureEvent()` from `lib/analytics/server.ts`; client-only events use helpers in `lib/analytics.ts`. See `docs/19-analytics.md` for the full taxonomy and rules. No PR ships a user-facing feature without instrumentation and a test for it.

## Basic Rules

- Avoid add not necessary comments in code, comment with not clear propose is a bad practice
- Always use meaningful variable names, avoid using single letter variables
- Use consistent naming conventions for functions and variables
- Always add a task for yourself to check if the code is working