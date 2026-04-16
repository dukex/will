# Component Plan
> Phase: design | Project: Will | Generated: 2026-04-16
> Target: code | Codebase: existing Android + Jetpack Compose + Material3

---

## Reuse (no changes needed)

| Component | File | Notes |
|-----------|------|-------|
| `AppGrid` | `ui/AppGrid.kt` | Keep as-is |
| `SearchField` (in AppGrid) | `ui/AppGrid.kt` | Keep as-is |
| `NotificationPermissionPrompt` | `ui/NotificationPermissionPrompt.kt` | Keep as-is |
| `ActiveTaskOverlayService` | `overlay/ActiveTaskOverlayService.kt` | No design changes in MVP |
| `ThemePreference` | `ui/ThemePreference.kt` | Keep as-is |
| `WillTheme` | `ui/theme/Theme.kt` | Token additions only (see Refactor) |

---

## Refactor (enhance existing)

| Component | File | Changes |
|-----------|------|---------|
| `TaskItem` (inside `TaskFeed.kt`) | `ui/TaskFeed.kt` | Wrap with `SwipeToDismissBox`; add `TaskStatusIndicator`; add `animateItem()` modifier; ensure `heightIn(min = 48.dp)` |
| `TaskFeed` | `ui/TaskFeed.kt` | Add priority sections (active/pending/done); section headers; hide empty sections; `key = { it.id }` on items |
| `TaskActionSheet` | `ui/TaskActionSheet.kt` | Add lifecycle context line (timestamp + state label); add confirm-delete pattern |
| `HomeTopBar` | `ui/HomeTopBar.kt` | Wrap Done/Later/Delete in 48dp `Box`; add filter icon → NotificationFilterSettings; add chart icon → FocusDashboard; add semantics `role = Role.Button` |
| `WillTheme` | `ui/theme/Theme.kt` | Add `WillSuccess`, `WillTextTertiary` token usages; ensure `WillSpacing.touchTarget` constant exists |

---

## New — shared (project-wide)

| Component | File to create | Purpose |
|-----------|---------------|---------|
| `TaskStatusIndicator` | `ui/TaskStatusIndicator.kt` | 8dp colored dot composable; maps `TaskStatus` → color token |
| `SectionHeader` | (inline or `ui/SectionHeader.kt`) | `Text(style = labelSmall, color = WillTextTertiary)` section separator |

---

## New — local (single-screen use)

| Component | File to create | Screen |
|-----------|---------------|--------|
| `NotificationFilterSettings` composable | `ui/NotificationFilterSettings.kt` | Filter settings screen (US-08) |
| `AppToggleRow` | (inline in NotificationFilterSettings) | Per-app toggle row composable |
| `IntentInputSheet` composable | `ui/IntentInputSheet.kt` | Text + voice task creation sheet (US-03) |
| `NlpStateRow` | (inline in IntentInputSheet) | Idle/processing/suggestion label row |
| `FocusDashboard` composable / screen | `ui/FocusDashboard.kt` | Focus score + bar chart screen (US-05) |
| `TaskTimeChart` | (inline in FocusDashboard) | Canvas bar chart composable |
| `PrivacySettingsScreen` composable | `ui/PrivacySettings.kt` | Data explanation + delete-all (US-07) |

---

## New — ViewModels and domain

| Class | File to create | Purpose |
|-------|---------------|---------|
| `IntentInputViewModel` | `task/IntentInputViewModel.kt` | Voice state, NLP state, addTask delegation |
| `NlpPipeline` | `task/NlpPipeline.kt` | Regex intent extraction from transcript |
| `FilterSettingsViewModel` | `notification/FilterSettingsViewModel.kt` | App list + per-app enabled state (Room or SharedPrefs) |
| `FocusDashboardViewModel` | `task/FocusDashboardViewModel.kt` | Compute focus score, time-on-task, context switch count |
| `AppNotificationFilter` | `notification/AppNotificationFilter.kt` | Domain model: packageName + isEnabled |

---

## Room migrations required

| Change | Migration |
|--------|-----------|
| Add `status` TEXT column to `tasks` | `ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'` |
| Add `startedAt` INTEGER nullable | `ALTER TABLE tasks ADD COLUMN startedAt INTEGER` |
| Add `completedAt` INTEGER nullable | `ALTER TABLE tasks ADD COLUMN completedAt INTEGER` |
| Add `contextSwitchCount` INTEGER | `ALTER TABLE tasks ADD COLUMN contextSwitchCount INTEGER NOT NULL DEFAULT 0` |
| New `app_filters` table | `CREATE TABLE app_filters (packageName TEXT PRIMARY KEY, isEnabled INTEGER NOT NULL DEFAULT 1)` |
