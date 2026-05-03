# Scope
> Phase: brief | Project: Will | Generated: 2026-04-16

## What we're building

Android launcher MVP — all remaining user stories (US-03 through US-08) after the completed foundation (US-01 launcher, US-02 notification capture).

Target: single APK, minSdk 24, portrait only, dark-only, OLED black.

---

## Screen List (prioritized)

| Priority | Screen / Surface | US | Status | Notes |
|----------|------------------|----|--------|-------|
| P1 | **HomeScreen** — task feed improvements | US-06 | enhance | Swipe actions, priority ordering, task lifecycle states, reactive real-time updates |
| P1 | **TaskActionSheet** — polish | US-04 | enhance | Task lifecycle context (pending / in-progress / done label), confirm-on-delete |
| P2 | **NotificationFilterSettings** | US-08 | new | Per-app toggle list, persisted in Room/SharedPrefs, accessible from HomeScreen |
| P3 | **IntentInputSheet** | US-03 | new | Bottom sheet — typed task creation |
| P4 | **TaskLifecycle indicators** | US-04 | enhance | Visual status on TaskItem (dot or prefix), startedAt / completedAt timestamps |
| P5 | **ProductivityDashboard** | US-05 | new | Focus score, time-on-task chart, context switch count — accessible via gesture or action |
| P6 | **PrivacySettings** | US-07 | new | On-device data explanation, delete-all history, permission rationale |

Out of scope: light theme, landscape, cloud sync, widget, tablet layout.

---

## Component Scope

| Component | Action | Notes |
|-----------|--------|-------|
| `TaskItem` | enhance | Add swipe-to-complete (SwipeToDismiss), lifecycle state indicator, 48dp touch target for tap zone |
| `TaskFeed` | enhance | Priority sections, empty state per-section, real-time update animation (no flash) |
| `HomeTopBar` | enhance | 48dp touch targets on inline Done/Later/Delete, semantic roles |
| `TaskActionSheet` | enhance | Add lifecycle context line, confirm-delete pattern |
| `NotificationFilterSettings` | new | Screen — toggle list of apps that have sent notifications |
| `IntentInputSheet` | new | Bottom sheet — `BasicTextField` for typed task creation |
| `TaskStatusIndicator` | new | Small composable — colored dot or prefix showing pending/active/done |
| `FocusDashboard` | new | Screen — focus score card, time-on-task bar chart (custom Canvas drawing) |
| `PrivacySettingsScreen` | new | Screen — data usage explanation + destructive delete action |
| `AppGrid` / `SearchField` | keep | No changes needed |
| `ActiveTaskOverlayService` | keep | No design changes in MVP |
| `NotificationPermissionPrompt` | keep | No changes needed |

---

## Success Criteria

1. User can create a task manually via IntentInputSheet (typed)
2. Task feed shows live status (pending → in-progress → done) without requiring manual refresh
3. User can configure per-app notification filtering
4. User sees their daily focus score on the dashboard
5. All touch targets ≥ 48dp
6. No accent (#4F6EF7) appears on non-interactive elements
7. Theme compiles and renders correctly on minSdk 24

---

## Dependencies

| Dependency | Type | Notes |
|------------|------|-------|
| `AccessibilityService` | Android API | Required for task lifecycle completion detection (US-04) |
| `Room` | Already installed | Needs `startedAt`, `completedAt`, `contextSwitchCount` fields on TaskEntity |
| `ComposeCharts` or custom Canvas | TBD | Dashboard charts — evaluate before implementing US-05 |

---

## Issue Framing (suggested PRs)

| PR | Scope | User stories |
|----|-------|-------------|
| PR-1 | Feed improvements + task lifecycle states | US-04 (partial), US-06 (partial) |
| PR-2 | Notification filter settings | US-08 |
| PR-3 | Intent input sheet (text input only) | US-03 |
| PR-4 | Full task lifecycle tracking (AccessibilityService + timestamps) | US-04 |
| PR-5 | Productivity dashboard | US-05 |
| PR-6 | Privacy settings screen | US-07 |

Each PR should be independently reviewable, merged to main, tested on device.
