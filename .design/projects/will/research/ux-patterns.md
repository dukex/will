# UX Patterns — Android Task-Based Launchers
> Phase: research | Project: Will | Generated: 2026-04-16

---

## Feed Patterns

### Vertical task stream (not grid)
Text-dominant feeds outperform icon grids for task-oriented launchers because the user's mental model is "what do I need to do" not "which app do I open." Niagara Launcher proved this at scale: alphabetical text list with zero icons achieves faster app launch than icon grids because pattern recognition in text is faster than spatial memory search.

Pattern: single-column `LazyColumn`, full width, 8dp vertical gap between items. No card shadows. No dividers. Content density communicates urgency better than color.

Source: [Niagara Launcher — Medium review](https://medium.com/@spence7890/android-lets-take-a-quick-look-at-niagara-launcher-7d05adbb0d00)

### Priority sections with visual separation
Linear mobile (task management) and Todoist both use section headers to group tasks: overdue / today / later. The separation is a single line of small-cap text — no card borders, no background change. Collapsing empty sections prevents visual noise when a section has no items.

Pattern: sticky section header (12sp, WillTextTertiary), section items flush below. Hide section if empty — don't show "No overdue tasks."

Source: [Linear Mobile](https://linear.app/mobile), [Todoist Android](https://todoist.com)

### Real-time update with `animateItem()`
When Room emits a new Flow value, the LazyColumn must animate insertions/removals. Without `animateItem()` modifier on each item, additions appear as a flash. The key must be stable (task ID, not list index) for the animation to track identity across recompositions.

Pattern: `items(tasks, key = { it.id }) { task -> TaskItem(task, modifier = Modifier.animateItem()) }`

Source: [Android Compose SwipeToDismiss docs](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss)

---

## Swipe Interaction Patterns

### Bidirectional swipe with distinct semantics
Industry standard (Gmail, Linear, Todoist, iOS Mail) uses directional asymmetry: swipe-right = positive action (done / archive), swipe-left = destructive action (delete / snooze). This maps to left-to-right reading direction where "forward" feels affirming and "away" feels dismissal.

- StartToEnd (right swipe): Done — WillSuccess #4CAF74 background
- EndToStart (left swipe): Delete — WillDestructive #E05252 background

Threshold: 40% of item width. Below threshold, item springs back. Above threshold, `confirmValueChange` fires.

Pattern: `rememberSwipeToDismissBoxState(confirmValueChange = { ... })` with `positionalThreshold = { totalDistance -> totalDistance * 0.4f }`

Source: [Android SwipeToDismissBox docs](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss), [droidcon SwipeTo article](https://www.droidcon.com/2024/11/25/swipeto-explore-different-implementations-in-jetpack-compose/)

### Progress-based color interpolation
Background should transition from neutral to action-color as swipe progresses — not snap to color at threshold. Use `swipeToDismissBoxState.progress` with `lerp(Color.Transparent, actionColor, progress)` in `backgroundContent`. This gives visual feedback without committing the action.

No undo snackbar for MVP — deletion is permanent. This is a deliberate Will design choice: no recovery flow adds weight to the swipe, making it intentional.

### Long-press for overlay activation
Long-press on a TaskItem opens the `ActiveTaskOverlayService`. This is already implemented. The overlay is the secondary action surface — swipe actions (done/delete) are the primary quick actions. This hierarchy is correct: quick + destructive via swipe, nuanced control via overlay.

---

## Bottom Sheet Patterns

### Input sheet as a drawer, not a dialog
IntentInputSheet is triggered by a gesture or button from the home screen. It should feel like pulling up a drawer from the floor of the interface, not a dialog that blocks the background.

Pattern: `ModalBottomSheet` with `skipPartiallyExpanded = false` so the sheet starts at a half-expanded state showing just the text field. Full expansion on keyboard open.

Source: [Android Bottom Sheets docs](https://developer.android.com/develop/ui/compose/components/bottom-sheets)

### NLP state feedback inside the sheet
After user submits text, the sheet should not dismiss immediately. It transitions through states inline:
1. `idle` — BasicTextField focused
2. `processing` — field disabled, animated label "processing..." in Geist Mono below field
3. `suggestion` — action chip(s) appear below field showing mapped intent

This matches the pattern from Google Assistant's inline suggestion pattern: show suggestions in-context, don't navigate away.

### Sheet dismiss behavior
`onDismissRequest` must set the boolean state to false. Use `sheetState.hide().invokeOnCompletion { if (!sheetState.isVisible) showSheet = false }` to avoid the sheet being removed from composition mid-animation.

---

## Settings Screen Patterns

### Toggle list (NotificationFilterSettings)
The standard Android settings toggle pattern: full-width row, app name + package on left, `Switch` on right. No section headers needed when list is flat. Sort by: apps with notifications first, then alphabetical.

Source: Android System Settings (any Android device), [Material3 Switch docs](https://developer.android.com/develop/ui/compose/components/switch)

### Accessible from HomeScreen via single gesture
Settings must not require navigating through multiple taps. NotificationFilterSettings should be accessible via a long-press on the top bar or a dedicated icon in the top bar. No hamburger menu — this is a launcher.

### Destructive settings (PrivacySettings)
Delete-all-history action must use a two-step confirm pattern: first tap shows a confirmation dialog or inline warning; second tap executes. This is standard in iOS Mail, Signal, and Android system storage settings. Never immediate destructive action on first tap.

---

## Empty State Patterns

### Distinguish "not yet" from "all done"
Two semantically different empty states must have different copy and visual treatment:
- **First launch / no tasks yet**: contextual prompt to create first task or wait for notifications
- **All tasks cleared**: affirming — work is complete for now

Both states should be minimal text only — no illustrations, no mascots, no animations. The feed emptiness itself communicates the cleared state.

Source: [NN/G Empty States article](https://www.nngroup.com/articles/empty-state-interface-design/), [Pencil & Paper empty states guide](https://www.pencilandpaper.io/articles/empty-states)

### Per-section empty states: hide, don't explain
If the "Overdue" section has no items, remove the section header entirely. Don't show "No overdue tasks" — that's noise. Show nothing. The absence communicates clearly enough.

---

## Dashboard Patterns

### Focus score as a single number + supporting bar chart
Productivity dashboards that show one primary metric (score) and one supporting visualization (time-on-task bars) outperform dashboards that show many metrics simultaneously. The primary metric answers "how did I do today?" The chart answers "when was I focused?"

Pattern: Large number (score, WillTextPrimary, large type), subheading label (WillTextSecondary, caption), bar chart below showing hourly or per-task time distribution.

Source: [Building custom charts in Compose — ProAndroidDev](https://proandroiddev.com/building-your-first-custom-chart-in-android-with-jetpack-compose-a890fb60878b)

### No real-time updates on dashboard
Dashboard data is historical. It does not need to update while visible. Compute metrics on entry, display static snapshot. This avoids unnecessary Room queries and recompositions.
