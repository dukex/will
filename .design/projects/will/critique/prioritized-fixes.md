# Prioritized Fixes — Will
> Phase: critique | Project: Will | Date: 2026-04-16
> See also: [critique.md](./critique.md) | [accessibility-fixes.md](./accessibility-fixes.md)

---

## Critical (Must Fix Before Build)

### C-01 — IntentInputSheet has no visible entry point on HomeScreen

**Screen:** [HomeScreen](../design/screen-01-home-screen.md)
**Problem:** The HomeScreen layout and interactions table reference "Tap + or bottom edge pull" as the trigger for IntentInputSheet, but neither a `+` button nor a pull handle exists in the wireframe or component plan. A user who relies only on the visible UI has no way to discover manual task creation.
**Fix:** Add an `IconButton(Icons.Outlined.Add, contentDescription = "add task")` to the HomeTopBar right cluster (alongside filter and chart icons). Update the HomeTopBar layout spec and component-plan.md. Three icons total: add · filter · chart.
**Why critical:** This is a functional gap — an entire US (US-03) is inaccessible from the default UI path. It also breaks WCAG 2.4.4 (link purpose) since the gesture path is not surfaced.

---

### C-02 — Swipe-to-delete has no undo mechanism

**Screen:** [HomeScreen](../design/screen-01-home-screen.md)
**Problem:** SwipeToDismissBox left swipe executes `deleteTask()` immediately after 40% threshold. There is no undo, no snackbar, no recovery path. Accidental deletion of an IN_PROGRESS task is a permanent data loss. This is a Heuristic 3 (user control and freedom) failure.
**Fix:** On swipe-delete threshold: call `deleteTask()` but immediately show a `Snackbar` (M3, `WillSurface2` background, `WillTextPrimary` text, `WillAccent` action label) with "undo" action. Hold deleted task in a ViewModel-level undo buffer for 5 seconds. On "undo" tap: restore to Room. On timeout: no action needed (already deleted). The Snackbar must use `SnackbarHostState` in the HomeScreen scaffold.
**Content:** Snackbar copy: `"task deleted"` with action label `"undo"` — lowercase, matching STYLE.md voice.
**Note:** Swipe-to-complete (done) does not need undo — it's reversible via "reopen" in TaskActionSheet.

---

## Important (High Priority)

### I-02 — FocusDashboard error state leaves blank screen

**Screen:** [FocusDashboard](../design/screen-05-focus-dashboard.md)
**Problem:** The design specifies "Log and show nothing" for computation errors. A blank screen below the header with no message is ambiguous — the user cannot distinguish "loading" from "error" from "no data."
**Fix:** If ViewModel initialization fails: show a two-line state below the header: `"data unavailable"` (bodyLarge, WillTextSecondary) + `"check back after completing some tasks"` (labelSmall, GeistMono, WillTextTertiary). This matches the existing empty state pattern used across other screens.
**Why important:** The dashboard is a new screen — users will encounter it for the first time. A blank screen on first visit creates permanent distrust.

---

### I-03 — HomeScreen empty state "04:12 focused today" requires cross-screen data

**Screen:** [HomeScreen](../design/screen-01-home-screen.md)
**Problem:** The "all tasks cleared" empty state shows `"04:12 focused today"` — but this data lives in `FocusDashboardViewModel`, not `TaskViewModel`. The HomeScreen must either duplicate this computation in `TaskViewModel` or depend on `FocusDashboardViewModel`.
**Fix:** Add `totalFocusTime` as a derived value in `TaskViewModel` (it has access to all tasks). Expose it as a `String` formatted to `"HH:MM"` via a `derivedStateOf` on the existing `StateFlow<List<Task>>`. Do not instantiate `FocusDashboardViewModel` from the HomeScreen.
**Why important:** This is a data architecture decision that needs resolution before build starts. If left ambiguous, the build phase will make an ad-hoc choice that may conflict with the ViewModel separation.

---

### I-05 — NotificationFilterSettings has no search / filter for long app lists

**Screen:** [NotificationFilterSettings](../design/screen-04-notification-filter-settings.md)
**Problem:** The design shows a flat alphabetical list. A heavy Android user (Lucas's persona) may have 50+ apps that have sent notifications. Scrolling through 50 rows to find and toggle an app creates unnecessary friction.
**Fix:** Add a `BasicTextField` search input at the top of the list (same styling as IntentInputSheet input: bottom-only border, Geist Sans 14sp, `WillTextTertiary` placeholder `"filter apps"`). Wire to a `filterQuery` StateFlow in `FilterSettingsViewModel`. The search filters by app name prefix (case-insensitive). This is a `BasicTextField` pattern already designed for Screen 03.
**Why important:** This is the same efficiency gap as Heuristic 7. Without search, the settings screen becomes unusable at scale.

---

## Polish (If Time Allows)

### P-01 — Unlabeled HomeTopBar icons

**Screen:** [HomeScreen](../design/screen-01-home-screen.md)
**Problem:** The filter and chart icons are unlabeled. First-time users must tap to discover their destinations. This is a Heuristic 6 (recognition over recall) cosmetic issue for the target persona, not a functional gap.
**Fix:** No change recommended unless user testing reveals discovery issues. The icons (`FilterList`, `BarChart`) are conventional enough for the developer persona. If labels are added in a future iteration, use `labelSmall` (GeistMono, WillTextTertiary) below each icon, hidden after first visit.

---

### P-02 — FocusDashboard copy deviates slightly from STYLE.md voice

**Screen:** [FocusDashboard](../design/screen-05-focus-dashboard.md)
**Problem:** "complete tasks to see your focus score" is mildly instructional — it tells the user what to do. STYLE.md voice is observational, not directive.
**Fix:** Replace with `"no completed tasks today"` (bodyLarge, WillTextSecondary). Remove the instruction. The user knows what tasks are.

---

### P-03 — Bar chart accent justification is a brand stretch

**Screen:** [FocusDashboard](../design/screen-05-focus-dashboard.md)
**Problem:** STYLE.md requires accent on interactive elements only. Chart bars are not interactive. The justification ("the user can mentally interact") is the weakest reasoning in the design.
**Fix:** Use `WillTextSecondary` (`#888888`) for bar fill instead of `WillAccent`. The chart reads cleanly in secondary gray — it is data, not interaction. Reserve accent for the score number itself if a future iteration adds a tap-to-detail pattern. **Only apply this if the brand contract audit is ever reassessed — the current design made a deliberate bet and it is not worth re-opening for MVP.**

---

### P-04 — IntentInputSheet keyboard dismissal requires two steps

**Screen:** [IntentInputSheet](../design/screen-03-intent-input-sheet.md)
**Problem:** On sheet open, keyboard auto-focuses the field. To dismiss the sheet without adding a task, the user must: (1) dismiss keyboard, (2) swipe sheet down. A cancel tap exits cleanly (one step) but the gesture path is two steps.
**Fix:** In `ModalBottomSheet`'s `onDismissRequest`, ensure the keyboard is dismissed simultaneously via `LocalSoftwareKeyboardController.current?.hide()`. This makes swipe-down a single-step dismiss.

---

### P-05 — Screen 06 "history cleared" delay uses hardcoded 1500ms

**Screen:** [PrivacySettings](../design/screen-06-privacy-settings.md)
**Problem:** `delay(1500)` in `LaunchedEffect` is specified inline. If the user navigates back before the 1.5 second window ends, the LaunchedEffect may attempt a state update on a disposed composable.
**Fix:** Wrap the delay in a `isActive` check or use `rememberCoroutineScope().launch` with a cancellation-safe approach. This is an implementation detail — flag for the build phase review.

---

## Style-Level Issues (require `/gsp-brand-refine`)

No STYLE.md source constraint violations were found. The one stretched justification (bar chart accent) does not require a STYLE.md update — it is a screen-level interpretation, not a pattern-level change.

If `/gsp-brand-refine` is run post-critique, consider adding:
- An explicit carve-out in STYLE.md for "data visualization fills" — permitted to use `WillTextSecondary` or a declared chart-specific token.
- A note in the `Always:` list clarifying that the accent on Switch track is correct (the switch IS interactive) — this is already implied but explicit confirmation prevents future ambiguity.
