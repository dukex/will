# Gap Analysis
> Phase: brief | Project: Will | Generated: 2026-04-16

## Summary

Brand system is complete. Codebase has the foundation (US-01, US-02). The gaps below are components and patterns in the brand system / scope that have no codebase equivalent yet.

---

## Missing Components

| Component | Brand ref | Priority | Notes |
|-----------|-----------|----------|-------|
| `IntentInputSheet` | will.yml patterns.input | P3 | Bottom sheet for typed task creation |
| `TaskStatusIndicator` | will.yml tokens.color.success/accent/tertiary | P1 | Dot composable — pending / in-progress / done |
| `NotificationFilterSettings` | will.yml patterns.layout | P2 | Full-screen toggle list |
| `FocusDashboard` | will.yml tokens.color.accent | P5 | Canvas-based chart screen |
| `PrivacySettingsScreen` | will.yml voice (dry, transparent) | P6 | Data explanation + delete-all |
| `SwipeToDismissBox` integration on `TaskItem` | will.yml tokens.color.success/destructive | P1 | M3 primitive, not yet wired in |

---

## Missing Token Usages

| Gap | Location | Fix |
|-----|----------|-----|
| `WillSuccess` (`#4CAF74`) | Nowhere in codebase | Use for swipe-to-complete action bg + done state indicator |
| `Spacing.touchTarget` (48dp) | HomeTopBar inline actions, TaskItem | Apply `Modifier.heightIn(min = Spacing.touchTarget)` |
| `Spacing.screenHorizontal` (24dp) | Hardcoded as `24.dp` in multiple files | Migrate to `Spacing.screenHorizontal` — not blocking, track as TD |
| `WillTextTertiary` (`#505050`) | Not used | Use for pending state + lifecycle timestamp metadata |

---

## Missing Data Model Fields

Brand scope requires lifecycle tracking (US-04) — TaskEntity needs fields that don't exist yet:

| Field | Type | US | Notes |
|-------|------|-----|-------|
| `startedAt` | `Long?` | US-04 | Timestamp when user first engaged with task |
| `completedAt` | `Long?` | US-04 | Timestamp of completion (auto or manual) |
| `contextSwitchCount` | `Int` | US-04 | App transitions before task completion |
| `status` | `enum TaskStatus` | US-04 | PENDING / IN_PROGRESS / DONE / DELETED |

These are domain model changes — also require `TaskDao` migration + `TaskViewModel` updates.

---

## M3 Accent Leak Risks (not yet audited in codebase)

Components added in MVP that need explicit color overrides:

| Component | Default M3 behavior | Required override |
|-----------|--------------------|--------------------|
| `Switch` (FilterSettings) | thumb = primary (WillAccent) | `checkedThumbColor = WillTextPrimary` |
| `SwipeToDismissBox` background | custom — no leak | Use `WillSuccess` / `WillDestructive` explicitly |
| `CircularProgressIndicator` (if used) | track = primary | Override to `WillTextTertiary` |
| `LinearProgressIndicator` (if used) | track = primary | Override to `WillTextTertiary` |

---

## No Gaps (already covered)

| Area | Status |
|------|--------|
| Color scheme (M3 tokens) | ✅ Applied in Theme.kt |
| Typography scale | ✅ Applied in Theme.kt |
| Shapes | ✅ Applied in Theme.kt — 0dp cards, 8dp sheets |
| Spacing constants | ✅ `Spacing` object in Theme.kt |
| Bottom sheet behavior | ✅ ModalBottomSheet uses `Shapes.large` (8dp) automatically |
| HorizontalDivider color | ✅ Uses `outline` token (#2A2A2A) |
