# Screen 03 — IntentInputSheet
> Phase: design | Project: Will | Generated: 2026-04-16
> US: US-03 (manual task creation — text + voice)

---

## Purpose

Bottom sheet for manual task creation. User types or speaks a task description. NLP pipeline extracts intent and suggests an action. On confirm, task is added to the feed.

---

## User flow position

Secondary surface over HomeScreen. Opens via tap on add trigger (HomeTopBar or a pull-up handle). Dismisses back to HomeScreen after task creation or cancel.

---

## Layout

```
┌──────────────────────────────────────────┐
│  ─────  (handle, #2A2A2A)               │
│                                          │
│  what do you need to do?                 │  ← BasicTextField placeholder (bodyMedium, WillTextTertiary)
│  reply to marcos about deploy_           │  ← typed content (bodyMedium, WillTextPrimary)
│                                          │
│  ──────────────────────────────────── ●  │  ← bottom-only 1dp WillBorder stroke + mic IconButton (right)
│                                          │
│  → open slack · reply to marcos          │  ← NlpStateRow (labelSmall, GeistMono, WillTextSecondary)
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│  add task                                │  ← confirm row (bodyMedium, WillAccent)
├──────────────────────────────────────────┤
│  cancel                                  │  ← cancel row (bodyMedium, WillTextTertiary)
├──────────────────────────────────────────┤
│                                          │
└──────────────────────────────────────────┘
  [safeDrawing.bottom + keyboard height]
```

**Sheet starts half-expanded** (`skipPartiallyExpanded = false`). Keyboard opens on field focus and pushes the sheet up to full expansion. `windowSoftInputMode=adjustResize` on this sheet context.

---

## Components used

- `ModalBottomSheet` (M3, `skipPartiallyExpanded = false`, `shape = WillShapes.large`)
- `BasicTextField` (not `TextField` — no M3 box decoration)
- `IconButton` (mic icon, 48dp touch target)
- `NlpStateRow` composable (inline, text label)
- `HorizontalDivider(color = WillBorder)`
- Background: `WillSurface2` (`#161616`)

---

## BasicTextField styling

```kotlin
BasicTextField(
    value = text,
    onValueChange = onTextChange,
    textStyle = MaterialTheme.typography.bodyMedium.copy(color = WillTextPrimary),
    cursorBrush = SolidColor(WillAccent),  // #4F6EF7 cursor
    decorationBox = { innerTextField ->
        Box {
            if (text.isEmpty()) Text("what do you need to do?", 
                style = bodyMedium.copy(color = WillTextTertiary))
            innerTextField()
        }
    }
)
```

Bottom-only stroke:
```kotlin
Modifier.drawBehind {
    drawLine(WillBorder, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
}
```

---

## NlpStateRow

Text-only row below the input. Three states:

| State | Content | Style |
|-------|---------|-------|
| idle | "" (empty) | — |
| processing | "processing..." | labelSmall, GeistMono, WillTextTertiary |
| suggestion | "→ open slack · reply to marcos" | labelSmall, GeistMono, WillTextSecondary |

`animateContentSize()` on the row so the layout expands smoothly when suggestion appears.

NLP suggestion is informational only — the user adds the task as typed, not as the NLP interpretation. The suggestion contextualizes; it does not override.

---

## Mic button

`IconButton` (48dp built-in touch target).

| Voice state | Icon | Tint |
|-------------|------|------|
| idle | `Icons.Outlined.Mic` | WillTextSecondary |
| listening | `Icons.Outlined.Mic` | WillAccent |
| processing | `Icons.Outlined.Mic` (disabled) | WillTextTertiary |

When listening: `animateColorAsState(WillAccent, tween(100))` on icon tint. No pulse animation — motion intensity is 2/10.

**Tap mic:**
1. Request `RECORD_AUDIO` permission if not granted (system dialog)
2. `SpeechRecognizer.startListening()` via ViewModel
3. On result: populate `BasicTextField` with transcript, trigger NLP pipeline
4. NLP runs synchronously on main thread (regex, < 1ms)

---

## States

### Default — idle, empty field
Sheet half-expanded. Field empty, placeholder visible. Mic idle. NlpStateRow empty.

### Typing in progress
Field shows cursor. NLP pipeline runs on each text change (debounced 300ms). NlpStateRow shows suggestion if any rule matches.

### Voice listening
Mic tint transitions to WillAccent. No animation beyond tint change.

### Processing (after voice result arrives)
Field populated with transcript. NlpStateRow shows "processing..." for the synchronous NLP duration (< 5ms). Then switches to suggestion.

### Voice permission denied
System dialog handles permission. If denied: mic button `flat-press` → opacity-dim. Keyboard-only input still works. No banner, no error message.

### Add task — success
`addTask()` called on ViewModel → Room insert. Sheet dismisses. HomeScreen updates reactively via Flow.

### Add task with empty field
"add task" row is `flat-press` disabled state (opacity 0.3, non-clickable) when field is empty. User cannot add a blank task.

---

## Interactions

| Trigger | Effect |
|---------|--------|
| Sheet opens | Field auto-focused (`FocusRequester.requestFocus()`), keyboard appears |
| Type text | NLP pipeline runs (debounced 300ms), NlpStateRow updates |
| Tap mic | Voice listening starts, tint → WillAccent |
| Voice result | Field populated, NLP runs, NlpStateRow updates |
| Tap "add task" | `flat-press` → `addTask()` → sheet dismisses |
| Tap "cancel" | `flat-press` → sheet dismisses, field cleared |
| Swipe down | Sheet dismisses, field cleared |

---

## Accessibility

**Field:** `contentDescription = "task description"`. Hint text read by TalkBack as placeholder.

**Mic button:**
- idle: `contentDescription = "start voice input"`
- listening: `contentDescription = "listening, tap to stop"`

**NlpStateRow:** `contentDescription = "suggested action: {suggestion text}"` when suggestion visible, `""` when idle.

**Add task row:** `semantics { role = Role.Button; contentDescription = "add task ${text}" }`.

**Cancel row:** `semantics { role = Role.Button; contentDescription = "cancel" }`.

---

## Image resources

None. Icons from Material Symbols:
- `Icons.Outlined.Mic` (mic, 24dp)
- `Icons.Outlined.Add` (add trigger on HomeTopBar, 24dp)
