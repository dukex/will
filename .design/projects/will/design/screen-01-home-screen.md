# Screen 01 — HomeScreen
> Phase: design | Project: Will | Generated: 2026-04-16
> US: US-04 (task lifecycle), US-06 (feed improvements)

---

## Purpose

The launcher's home screen. Primary and only persistent screen. Displays the task feed with live lifecycle state, and provides entry points to all other surfaces. No navigation bar, no tabs — the feed is the phone.

---

## User flow position

Entry point. All other screens branch from here. Returns here on back press and home button.

---

## Layout

```
┌──────────────────────────────────────────┐
│  [WindowInsets.safeDrawing.top padding]  │
│                                          │
│  Thu, Apr 16       [+ add] [≡ filter] [⊟]│  ← HomeTopBar (48dp rows)
│                                          │
├──────────────────────────────────────────┤  ← 1dp #2A2A2A divider
│                                          │
│  [active section — only if tasks exist]  │
│  ● reply to marcos about deploy          │
│    slack · started 14:32                 │
├──────────────────────────────────────────┤  ← 1dp divider
│  ● review pull request #412              │
│    github · started 14:28                │
├──────────────────────────────────────────┤  ← 1dp divider
│                                          │
│  pending                                 │  ← SectionHeader (labelSmall, WillTextTertiary)
├──────────────────────────────────────────┤
│  ○ pay electricity bill                  │
│    whatsapp · 09:14                      │
├──────────────────────────────────────────┤
│  ○ schedule dentist                      │
│    manual · 08:51                        │
├──────────────────────────────────────────┤
│                                          │
│  done today                              │  ← SectionHeader
├──────────────────────────────────────────┤
│  ✓ call ana back                         │
│    phone · completed 13:45               │
│                                          │
└──────────────────────────────────────────┘
  ↑ LazyColumn fills remaining height
```

**Screen padding:** 0dp sides — task cards extend edge to edge. Cards provide internal `16dp` horizontal padding.

**HomeTopBar:** 24dp horizontal padding, space-between layout. Left: date string (bodyMedium, WillTextSecondary). Right: **three** IconButtons (add · filter · dashboard), each 48dp touch target.
- Add: `Icons.Outlined.Add`, `contentDescription = "add task"` → opens `IntentInputSheet`
- Filter: `Icons.Outlined.FilterList` → `NotificationFilterSettings`
- Dashboard: `Icons.Outlined.BarChart` → `FocusDashboard`

---

## Components used

- `HomeTopBar` (refactored — see component-plan.md)
- `TaskFeed` (refactored — sectioned, with `animateItem()`)
- `TaskItem` (refactored — SwipeToDismissBox + TaskStatusIndicator)
- `TaskStatusIndicator` (new — 8dp dot)
- `SectionHeader` (new — labelSmall text)
- `HorizontalDivider(color = WillBorder, thickness = 1.dp)` between items
- `NotificationPermissionPrompt` (existing — conditional banner at bottom)
- `SnackbarHost` + `SnackbarHostState` — delete undo only. Snackbar: background `WillSurface2`, text `"task deleted"` (bodyMedium, WillTextPrimary), action `"undo"` (labelMedium, WillAccent). **⚠ Accent on "undo" is correct — it is an interactive action.** Hold deleted task in `TaskViewModel.pendingDelete` for 5s; restore on undo tap.

---

## TaskItem anatomy

```
[ ● ] [task title — bodyLarge, WillTextPrimary, -0.01em]
       [source · timestamp — labelSmall, GeistMono, WillTextSecondary]
```

- Status dot: 8dp circle composable, color from `TaskStatus` → token map
- Touch target: `Modifier.heightIn(min = 48.dp)` on the row
- Padding: `12dp` vertical, `16dp` horizontal inside card
- Tap: fire `PendingIntent` → source app
- Long-press: open `TaskActionSheet`
- Swipe right (StartToEnd): Done action
- Swipe left (EndToStart): Delete action

**Swipe background (StartToEnd — Done):**
```
Background: lerp(Color.Transparent, WillSuccess, state.progress)
Icon: checkmark (Material Symbols, 24dp, WillTextPrimary), right-aligned, 16dp from right edge
Label: "done" (labelSmall, GeistMono, WillTextPrimary), left of icon
```

**Swipe background (EndToStart — Delete):**
```
Background: lerp(Color.Transparent, WillDestructive, state.progress)
Icon: trash (Material Symbols, 24dp, WillTextPrimary), left-aligned, 16dp from left edge
Label: "delete" (labelSmall, GeistMono, WillTextPrimary), right of icon
```

---

## TaskStatusIndicator colors

| Status | Color token | Hex |
|--------|-------------|-----|
| PENDING | WillTextTertiary | #505050 |
| IN_PROGRESS | WillAccent | #4F6EF7 |
| DONE | WillSuccess | #4CAF74 |
| DELETED | WillDestructive | #E05252 |

Color transition: `animateColorAsState(targetValue = statusColor, animationSpec = tween(100))`.

---

## States

### Default — tasks in all sections
Sections render dynamically. Active section appears only if at least one `IN_PROGRESS` task exists. Pending section appears only if pending tasks exist. Done section shows today's completed tasks only.

### Empty — no tasks at all (first launch)
```
Full screen, centered vertically:
  "no tasks yet"           (bodyLarge, WillTextSecondary)
  "notifications will appear here"  (labelSmall, GeistMono, WillTextTertiary)
```
No illustration, no CTA button — the absence is the state.

### Empty — all tasks cleared today
```
Full screen, centered vertically:
  "clear"                  (bodyLarge, WillTextSecondary)
  "04:12 focused today"    (labelSmall, GeistMono, WillTextTertiary)
```

### Loading — initial Room query
HomeScreen mounts before Room emits first value. Show nothing (empty list) — `StateFlow` initial value is `emptyList()`. No skeleton, no spinner. The screen appears instantly empty then fills. The 0-flash interval is under 100ms on Room queries from the main thread.

### Error — Room error or permission denied
If notification permission is not granted: `NotificationPermissionPrompt` banner at the bottom of the screen (existing component). No full-screen error state for permission.

If Room throws (should not happen in practice): log to Logcat, show nothing. Do not surface internal errors to the user.

---

## Interactions

| Trigger | Effect |
|---------|--------|
| Tap TaskItem | `flat-press` (opacity 0.65, 100ms) → fire PendingIntent |
| Long-press TaskItem | `TaskActionSheet` opens |
| Swipe right on TaskItem | Lerp background WillSuccess; on 40% → `markDone()` |
| Swipe left on TaskItem | Lerp background WillDestructive; on 40% → `deleteTask()` + show Snackbar "task deleted" / action "undo" (5s). On undo tap → restore task to Room. |
| Task inserted | `animateItem()` slide from top |
| Task removed | `animateItem()` fade + shrink |
| Tap add icon | Open `IntentInputSheet` (US-03 entry point) |
| Tap filter icon | Navigate to NotificationFilterSettings |
| Tap chart icon | Navigate to FocusDashboard |
| Swipe up | AppGrid opens |

---

## Accessibility

**TalkBack traversal order:** HomeTopBar → active section header → active tasks (top to bottom) → pending section header → pending tasks → done section header → done tasks → permission prompt (if visible).

**Per TaskItem:**
```kotlin
Modifier.semantics(mergeDescendants = true) {
    contentDescription = "${task.title}. ${task.status.label}. from ${task.source}. ${task.createdAtLabel}"
    customActions = listOf(
        CustomAccessibilityAction("mark done") { markDone(task); true },
        CustomAccessibilityAction("delete") { deleteTask(task); true }
    )
}
```

**TaskStatusIndicator dot:**
```kotlin
Modifier.semantics { invisibleToUser() }
```

**Section headers:** `Modifier.semantics { heading() }` on each section header `Text`.

**HomeTopBar icons:** `contentDescription = "notification filter"` and `contentDescription = "focus dashboard"`.

---

## Image resources

No images. STYLE.md: "the UI is the imagery." Section headers are text. Empty states are text. No icons beyond system Material Symbols.

**Icons used:**
- Add icon: `Icons.Outlined.Add` (24dp, WillTextSecondary) — entry point for IntentInputSheet
- Filter icon: `Icons.Outlined.FilterList` (24dp, WillTextSecondary)
- Dashboard icon: `Icons.Outlined.BarChart` (24dp, WillTextSecondary)
- Done indicator: `Icons.Outlined.Check` (24dp, WillTextPrimary) in swipe background
- Delete indicator: `Icons.Outlined.Delete` (24dp, WillTextPrimary) in swipe background
