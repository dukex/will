# Screen 06 — PrivacySettings
> Phase: design | Project: Will | Generated: 2026-04-16
> US: US-07 (data transparency, delete history)

---

## Purpose

Explains what data Will stores, where it stays (on-device only), and gives the user a single destructive action to delete all task history. Accessible via "data & privacy" row in NotificationFilterSettings.

---

## User flow position

Second-level full-screen destination. Navigated to from NotificationFilterSettings → "data & privacy" row. Back navigates to NotificationFilterSettings.

---

## Layout

```
┌──────────────────────────────────────────┐
│  [safeDrawing.top padding]               │
│                                          │
│  ← data & privacy                        │  ← back + title (bodyLarge, WillTextPrimary)
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  what will stores                        │  ← SectionHeader (labelSmall, GeistMono, WillTextTertiary)
│                                          │
│  task title and source app               │  ← list item (bodyMedium, WillTextSecondary)
│  created, started, and completed times   │  ← list item
│  app switch count per task               │  ← list item
│  notification filter preferences         │  ← list item
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  where it stays                          │  ← SectionHeader
│                                          │
│  on this device only. no accounts,       │  ← paragraph (bodyMedium, WillTextSecondary)
│  no cloud sync, no analytics, no         │
│  third-party data sharing.               │
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  permissions used                        │  ← SectionHeader
│                                          │
│  notification listener                   │  ← permission row (bodyMedium, WillTextSecondary)
│  to capture notifications as tasks       │  ← rationale (labelSmall, GeistMono, WillTextTertiary)
├──────────────────────────────────────────┤
│  accessibility service                   │
│  to detect when tasks are completed      │
├──────────────────────────────────────────┤
│  microphone (optional)                   │
│  for voice task input                    │
├──────────────────────────────────────────┤
│  draw over other apps                    │
│  for the active task overlay             │
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  delete all task history                 │  ← destructive action row (bodyMedium, WillDestructive)
│                                          │
└──────────────────────────────────────────┘
```

---

## Confirm-delete pattern

First tap on "delete all task history":
```
delete all task history     →    confirm? this cannot be undone    [cancel]
```

The row expands inline:
- First state: `"delete all task history"` (WillDestructive)
- Confirm state: `"confirm? this cannot be undone"` (WillDestructive, bodySmall) + `"cancel"` (WillTextTertiary, bodySmall) inline to the right

Second tap on confirm: executes `deleteAllTasks()` + `clearFilterPreferences()`. Screen remains open. Shows:
```
history cleared
```
(bodyMedium, WillTextSecondary) in place of the delete row for 1.5s, then row resets to original state.

No navigation away. No dialog. Inline state change — consistent with STYLE.md voice: dry, uncompromising.

---

## Components used

- `Column` with `LazyColumn` for scrollable content
- `SectionHeader` composable
- `HorizontalDivider(color = WillBorder, thickness = 1.dp)` between sections
- `Text` rows (bodyMedium, WillTextSecondary) for list items
- `Text` paragraphs (bodyMedium, WillTextSecondary) for explanations
- Destructive row: `Row.clickable` with `Text(color = WillDestructive)`
- Back arrow: `IconButton` + `Icons.AutoMirrored.Outlined.ArrowBack`

---

## Copy

All copy follows STYLE.md voice: precise, dry, peer-to-peer. No `"your"`, no marketing language, no comfort-speak.

| Section | Copy |
|---------|------|
| what will stores | List of data types, noun only — no "we collect" phrasing |
| where it stays | Factual statement. No brand promise language. |
| delete action | "delete all task history" — not "clear my data" or "reset" |
| confirm state | "confirm? this cannot be undone" — direct, no softening |
| after delete | "history cleared" — past tense, done |

---

## States

### Default
Static content. No live data loaded. All explanations are hardcoded strings.

### Confirm-delete active
Row expands to confirmation inline state. No animation — instant state change.

### After deletion
"history cleared" shown for 1.5s, then reset. Uses `LaunchedEffect(Unit)` + `delay(1500)` + state reset.

### Error on deletion
Room delete failure: log, show nothing. Delete action row resets to default state silently.

---

## Interactions

| Trigger | Effect |
|---------|--------|
| Tap back arrow | Navigate back to NotificationFilterSettings |
| Tap "delete all task history" | `flat-press` → confirm state renders inline |
| Tap "confirm?" | `flat-press` → `deleteAllTasks()` → "history cleared" for 1.5s |
| Tap "cancel" | `flat-press` → restore original delete row |

---

## Accessibility

**Delete row:** `semantics { role = Role.Button; contentDescription = "delete all task history" }`.

**Confirm state:**
- Confirm text: `contentDescription = "confirm delete all task history, this cannot be undone"`
- Cancel button: `contentDescription = "cancel delete"`

**Announcement after deletion:** `View.announceForAccessibility("history cleared")` (or equivalent Compose semantics live region).

**Section headers:** `semantics { heading() }`.

**Permission rows:** Each `Modifier.semantics(mergeDescendants = true)` so permission name + rationale are read as one unit.

---

## Image resources

None. All text-based content. No icons beyond back arrow.

**Icons used:**
- `Icons.AutoMirrored.Outlined.ArrowBack` (24dp, WillTextSecondary)
