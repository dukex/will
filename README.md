# Will

> A task-centric Android launcher. Notifications, calendar entries, and typed
> intents are turned into **Tasks** in a single reactive feed — no icon grid as
> the front door, no badges, no infinite scroll. The phone serves your intent,
> not the other way around.

**Status:** early/alpha. The launcher boots, captures notifications, pins apps,
and tracks task lifecycle. Accessibility-driven auto-completion and the full
focus dashboard are still in progress — see
[`docs/99-TASKS.md`](docs/99-TASKS.md).

## Why

Most launchers are app launchers. Will is an *intent* launcher. Every passive
input (a notification, a calendar event) and every typed intent becomes a
`Task` in a Room database. The home screen is that feed. Tap a task to fire
its underlying `PendingIntent` — no app drawer detour. Lifecycle fields
(`startedAt`, `completedAt`, `contextSwitchCount`) feed a focus dashboard so
you can measure how the device actually spends your attention.

The full design rationale lives in [`docs/01-IDEA.md`](docs/01-IDEA.md).

## Features today

- **Launcher home** — task feed first, app grid behind a swipe up.
- **Pinned apps** — long-press in the drawer to pin up to 5 apps above the
  feed.
- **Notification capture** — `NotificationListenerService` turns notifications
  into tasks, preserves the original `PendingIntent`, dedups conversations,
  and clears the task when the notification is dismissed.
- **Manual tasks** — text input sheet creates tasks directly.
- **Lifecycle tracking** — manual completion, time on task, context switches.
- **Focus dashboard** — today's score, time-on-task chart, switch count.
- **Per-app notification filter** — settings screen with allow/deny per app.
- **Active task overlay** — draggable bubble with done/later/delete actions.
- **OLED true-black theme** — `#000000`, no shadows, no gradients.

## Privacy

Will operates under **least privilege**:

- All data stays **on device**. There is no backend, no analytics, no
  telemetry.
- The `NotificationListenerService` and (planned) `AccessibilityService` are
  used **only** to populate the task feed and detect completion.
- The user can wipe all stored tasks from the privacy settings screen at any
  time.

If this changes in the future, it will be called out in
[`SECURITY.md`](SECURITY.md) and the changelog before any release.

## Build

Requirements: Android Studio (Ladybug or newer), JDK 17, Android SDK with
platform 36.

```bash
# Debug APK
./gradlew assembleDebug

# Install on a connected device
./gradlew installDebug

# Unit tests
./gradlew test

# Lint
./gradlew lint

# Type check (no APK)
./gradlew compileDebugKotlin
```

After install, grant the launcher these permissions when prompted:

- **Notification access** — Settings → Apps → Special access → Notification
  access → Will.
- **Display over other apps** — required for the active-task overlay bubble.
- **Set as default home app** — Settings → Apps → Default apps → Home app.

## Architecture

Domain (`task/`) is plain Kotlin with zero Android dependencies. Persistence
lives in `infrastructure/room/` with mappers between domain and Room
entities. ViewModels expose `StateFlow`s; Compose subscribes. Manual DI via
`ViewModelProvider.Factory` — no Hilt.

A more detailed file-by-file map is in [`CLAUDE.md`](CLAUDE.md).

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md). The short version: tests first
(red → green → refactor), atomic conventional commits, and follow the
launcher UX constraints — no top app bars, no bottom nav, the task feed *is*
the home screen.

## Security

Found a vulnerability or a privacy concern? Please follow
[`SECURITY.md`](SECURITY.md) — do **not** open a public issue.

## License

[MIT](LICENSE).
