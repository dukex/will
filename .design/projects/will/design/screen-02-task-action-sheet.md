# Screen 02 — TaskActionSheet
> Phase: design | Project: Will | Generated: 2026-04-16
> US: US-04 (task lifecycle context, confirm-delete)

---

## Purpose

Bottom sheet for task lifecycle actions. Replaces the existing `TaskActionSheet.kt` with lifecycle context (state label + timestamps) and a confirm-delete pattern. Opens via long-press on TaskItem.

---

## User flow position

Secondary surface. Opens over HomeScreen (scrim). Dismisses back to HomeScreen. No navigation destination.

---

## Layout

```
┌──────────────────────────────────────────┐
│  ─────  (handle indicator, #2A2A2A)      │
│                                          │
│  reply to marcos about deploy            │  ← task title (bodyLarge, WillTextPrimary)
│  in progress · started 14:32 · 2m ago   │  ← lifecycle line (labelSmall, GeistMono, WillTextTertiary)
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│  mark done                               │  ← action row (bodyMedium, WillTextPrimary)
├──────────────────────────────────────────┤
│  pause                                   │  ← action row (bodyMedium, WillTextPrimary)
│                                          │  (or "start" if PENDING)
├──────────────────────────────────────────┤
│  delete                                  │  ← action row (bodyMedium, WillDestructive)
├──────────────────────────────────────────┤
│                                          │
└──────────────────────────────────────────┘
  [safeDrawing.bottom padding]
```

**Confirm-delete state** (after first tap on delete):
```
├──────────────────────────────────────────┤
│  confirm delete?        [cancel]         │  ← WillDestructive text, cancel in WillTextSecondary
├──────────────────────────────────────────┤
```

---

## Components used

- `ModalBottomSheet` (M3 — shape = `RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)` from WillShapes.large)
- `HorizontalDivider(color = WillBorder, thickness = 1.dp)` between rows
- Action rows: `Row` with `Modifier.fillMaxWidth().clickable(...).padding(horizontal = 24.dp, vertical = 14.dp)`
- Background: `WillSurface2` (`#161616`)
- Top border: `1dp WillBorder` along the top edge

---

## Lifecycle context line

Format depends on task status:

| Status | Line format |
|--------|-------------|
| PENDING | `pending · created {time} · {relative}` |
| IN_PROGRESS | `in progress · started {time} · {duration}` |
| DONE | `done · completed {time} · {duration}` |

All values in Geist Mono (labelSmall, WillTextTertiary). Separator is `·` (middle dot). Relative time: `{N}m ago` or `{N}h ago`.

---

## Action list by status

| Status | Actions shown |
|--------|---------------|
| PENDING | "start", "mark done", "delete" |
| IN_PROGRESS | "mark done", "pause", "delete" |
| DONE | "reopen", "delete" |

"start" → sets status to IN_PROGRESS, sets `startedAt`  
"pause" → sets status to PENDING, preserves `startedAt`  
"mark done" → sets status to DONE, sets `completedAt`  
"reopen" → sets status to PENDING, clears `completedAt`  
"delete" → confirm-delete pattern before execution

---

## States

### Default
Sheet renders with task data loaded from the `Task` passed on long-press. No async loading required — task data is already in memory in the ViewModel.

### Confirm-delete
First tap on "delete" changes the row from:
```
delete                     (WillDestructive)
```
to:
```
confirm delete?   [cancel] (WillDestructive + WillTextSecondary)
```
Second tap executes `deleteTask()` and dismisses the sheet.  
Tap on cancel: restores original delete row (no animation, instant state reset).

Timeout: no auto-reset. The confirm state persists until dismissed or cancelled.

### Loading
Not applicable — all data is synchronous and in-memory.

### Error
If `deleteTask()` throws: sheet stays open, action row restores, no user-visible error message. Error is logged. (Room delete failures are essentially impossible in practice.)

---

## Interactions

| Trigger | Effect |
|---------|--------|
| Long-press TaskItem on HomeScreen | Sheet slides up (M3 default animation) |
| Tap action row | `flat-press` opacity 0.65, 100ms → executes action → sheet dismisses |
| Tap "delete" (first tap) | Instant state change to confirm-delete view |
| Tap "confirm delete?" | `flat-press` → `deleteTask()` → sheet dismisses |
| Tap "cancel" | Instant restore to original delete row |
| Tap scrim | Sheet dismisses (M3 `onDismissRequest`) |
| Swipe down | Sheet dismisses |

---

## Accessibility

**Sheet focus:** `FocusRequester` on the task title text when sheet opens — TalkBack focus lands on title first.

**Action rows:** each row has `Modifier.semantics { role = Role.Button; contentDescription = "${action} ${task.title}" }`.

**Lifecycle line:** read as-is. No additional semantic override needed — content is human-readable.

**Confirm-delete state:** When confirm state is active, the "confirm delete?" row gets `contentDescription = "confirm delete ${task.title}"`. Cancel button gets `contentDescription = "cancel delete"`.

**Image resources:** None. All text and icons are system-provided (`Icons.Outlined.Check`, etc. only if icon prefixes are used on rows — optional).
