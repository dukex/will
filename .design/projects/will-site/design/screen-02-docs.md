# Screen 02 — Docs
> Phase: design | Project: Will Site | Generated: 2026-04-16

---

## Purpose

Documentation section. Explains how Will works, its architecture, and provides a changelog.
Target reader: developers who want to build or contribute.

---

## Structure

```
/docs/
├── index.md             → "what is Will"
├── getting-started.md   → build from source, install on device
├── architecture.md      → domain model, notification pipeline, overlay service
└── changelog.md         → version history
```

---

## Layout (VitePress doc layout)

VitePress default doc layout with sidebar. Theme overrides match Will brand.

### Sidebar

```
docs
├── introduction
├── getting started
├── architecture
└── changelog
```

### Content area

Standard markdown content. VitePress renders it.

Brand overrides applied via CSS:
- `--vp-c-bg`: `#000000`
- `--vp-c-bg-soft`: `#0D0D0D`
- `--vp-c-bg-mute`: `#161616`
- `--vp-c-text-1`: `#F0F0F0`
- `--vp-c-text-2`: `#888888`
- `--vp-c-brand-1`: `#4F6EF7`
- `--vp-c-border`: `#2A2A2A`
- `--vp-font-family-base`: `'Geist Sans', Inter, sans-serif`
- `--vp-font-family-mono`: `'Geist Mono', monospace`

---

## Content

### docs/index.md — "introduction"

```markdown
# Will

Will is an Android launcher that treats your phone as a work surface, not an attention sink.

## What it does

- Captures notifications and turns them into a task list
- Provides a minimal app grid (swipe up from the home screen)
- Includes an overlay for active task management
- Has no streaks, no badges, no engagement mechanics

## Status

Will is under active development. US-01 (launcher) and US-02 (notification capture) are complete.
US-03 through US-08 are in progress.
```

### docs/getting-started.md

```markdown
# Getting started

## Prerequisites

- Android Studio Meerkat (2024.3) or later
- Android SDK 36
- Device or emulator running Android 7.0+ (minSdk 24)

## Build from source

\`\`\`bash
git clone https://github.com/dukex/will.git
cd will
./gradlew assembleDebug
./gradlew installDebug
\`\`\`

## Set as default launcher

1. Install the APK
2. Press home button
3. Select "Will" from the launcher picker
4. Grant notification access in Settings → Apps → Special app access

## Permissions

| Permission | Required for |
|-----------|-------------|
| BIND_NOTIFICATION_LISTENER_SERVICE | Capturing notifications as tasks |
| SYSTEM_ALERT_WINDOW | Active task overlay |
| RECEIVE_BOOT_COMPLETED | Restore overlay on reboot |
```

### docs/architecture.md

```markdown
# Architecture

Will follows a clean domain/infrastructure separation.

## Domain model

\`Task\` is a pure Kotlin data class — zero Android or Room dependencies.

\`\`\`kotlin
data class Task(
    val id: Long = 0,
    val title: String,
    val source: String,
    val createdAt: Long = System.currentTimeMillis(),
    val notificationKey: String? = null,
    val pendingIntent: PendingIntent? = null
)
\`\`\`

## Layers

| Layer | Package | Responsibility |
|-------|---------|---------------|
| Domain | \`task/\` | Task model, TaskRepository interface, TaskViewModel |
| Infrastructure | \`infrastructure/room/\` | Room entity, DAO, database singleton |
| Notification | \`notification/\` | Notification listener, processor, action store |
| UI | \`ui/\` | Composable screens and components |
| Overlay | \`overlay/\` | System alert window overlay service |
| Launcher | \`launcher/\` | App list repository and ViewModel |

## Notification pipeline

\`\`\`
Notification posted
  → WillNotificationListenerService.onNotificationPosted()
  → NotificationProcessor.process() — maps notification to Task (or null)
  → TaskDao.insert() — persists to Room
  → TaskFeed recomposes — StateFlow emits new list
\`\`\`

## Overlay communication

The overlay service communicates back to MainActivity via local broadcast:
- \`ACTION_DONE\` — mark task done
- \`ACTION_LATER\` — dismiss overlay, keep task
- \`ACTION_DELETE\` — delete task

## No Hilt

Dependency injection is manual via \`ViewModelProvider.Factory\`. Each ViewModel receives a \`CoroutineDispatcher\` for testability.
```

### docs/changelog.md

```markdown
# Changelog

All notable changes to Will are documented here.

## [unreleased]

### Added
- Notification filter toggle now updates the UI reactively
- Overlay actions (done/delete/later) now reach MainActivity
- Active task overlay shows the correct tapped task

### Fixed
- Replace broadcast with SharedFlow for overlay→activity communication
- Notification filter toggle now updates the UI reactively

## [0.2.0] — 2026-04-16

### Added
- Per-app notification filter (US-08 partial)
- Active task overlay with done/later/delete actions
- Regression tests for overlay action handling

## [0.1.0] — 2026-04-15

### Added
- Basic launcher (US-01): app grid, swipe-up gesture, launch apps
- Notification capture (US-02): NotificationListenerService → Task → Room
- Task feed (HomeScreen): tap to open source app, long-press for action sheet
- Room database with TaskEntity, TaskDao, WillDatabase
- NotificationProcessor, NotificationActionStore, NotificationKeyTracker
```

---

## Accessibility

- VitePress built-in keyboard navigation (sidebar, content)
- Focus ring: `outline: 2px solid #4F6EF7`
- Skip to content link
- Code blocks: proper `<code>` semantics
