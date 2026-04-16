# Screen 05 — FocusDashboard
> Phase: design | Project: Will | Generated: 2026-04-16
> US: US-05 (productivity metrics)

---

## Purpose

Historical snapshot of today's focus: a focus score (0–100), a time-on-task bar chart showing per-task durations, and a context switch count. Data is computed on entry — no live updates. Accessible via dashboard icon in HomeTopBar.

---

## User flow position

Full-screen destination. Navigated to from HomeTopBar dashboard icon. Back navigates to HomeScreen. Hard cut on entry (no transition animation — motion intensity 2/10).

---

## Layout

```
┌──────────────────────────────────────────┐
│  [safeDrawing.top padding]               │
│                                          │
│  ← focus                                 │  ← back + title row (bodyLarge, WillTextPrimary)
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  today                                   │  ← SectionHeader (labelSmall, GeistMono, WillTextTertiary)
│                                          │
│  73                                      │  ← score (displaySmall, 36sp, WillTextPrimary, -0.02em)
│  focus score                             │  ← label (labelSmall, GeistMono, WillTextTertiary)
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  time on task                            │  ← SectionHeader
│                                          │
│  ┌────────────────────────────────────┐  │
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░  reply marcos   │  │  ← Bar row: WillAccent fill + WillBorder empty
│  │  ▓▓▓▓▓░░░░░░░░░░░  review PR #412  │  │
│  │  ▓▓░░░░░░░░░░░░░░  pay bill        │  │
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  call ana        │  │
│  └────────────────────────────────────┘  │
│  (Canvas composable, full width)         │
│                                          │
├──────────────────────────────────────────┤  ← 1dp WillBorder divider
│                                          │
│  context switches                        │  ← SectionHeader
│                                          │
│  12                                      │  ← count (displaySmall, WillTextPrimary)
│  app switches today                      │  ← label (labelSmall, GeistMono, WillTextTertiary)
│                                          │
└──────────────────────────────────────────┘
```

**No Card composables.** All sections are flat on `WillBackground`. STYLE.md: "no decorative containers."

---

## Focus score calculation

```
score = clamp(round(
    completedTaskCount / max(totalTaskCount, 1) * 50 +
    totalFocusMinutes / max(targetMinutes, 1) * 30 +
    (1 - contextSwitchCount / max(maxExpectedSwitches, 1)) * 20
), 0, 100)
```

Where:
- `completedTaskCount` = tasks with `status == DONE` today
- `totalTaskCount` = all tasks created today
- `totalFocusMinutes` = sum of (`completedAt - startedAt`) for DONE tasks (minutes)
- `targetMinutes` = 240 (4 hours — hardcoded for MVP)
- `contextSwitchCount` = sum of `task.contextSwitchCount` across all today's tasks
- `maxExpectedSwitches` = 30 (hardcoded for MVP)

Score is computed in `FocusDashboardViewModel` on `init {}` from today's tasks. Static value — no live updates.

---

## Bar chart (Canvas composable)

Each bar represents one task completed today. Bar width = proportional to task duration relative to max duration in the set.

```kotlin
// Per row (task):
// Bar: drawRect(color = WillAccent, topLeft, size = Size(barWidth * progress, barHeight))
// Background: drawRect(color = WillBorder, topLeft, size = Size(barWidth, barHeight))
// Label: nativeCanvas.drawText(task.title, labelX, labelY, textPaint)
```

Layout: vertical list of bars. Each row: 24dp height, 4dp gap between rows. Bar occupies left 60% of available width. Label on right 40%, `bodySmall` (GeistMono-based via theme), `WillTextSecondary`.

Bar color: `WillAccent` (`#4F6EF7`). Per STYLE.md constraint: "accent on interactive elements only" — exception justified because chart bars represent performance data (the user can mentally interact with the data by reading it), and it's the only color in the chart. Not a decorative use.

Empty bar background: `WillBorder` (`#2A2A2A`). Indicates full potential width.

`drawWithCache` used to avoid per-frame allocation:
```kotlin
Modifier.drawWithCache {
    val paint = android.graphics.Paint().apply { ... }
    onDrawBehind { /* draw bars */ }
}
```

---

## Components used

- `Column` + `LazyColumn` (if task list is long) for layout
- `Canvas` composable for bar chart
- `SectionHeader` composable (labelSmall, WillTextTertiary)
- `HorizontalDivider(color = WillBorder, thickness = 1.dp)` between sections
- `Text` with `displaySmall` for score and switch count
- Back arrow: `IconButton` + `Icons.AutoMirrored.Outlined.ArrowBack`

---

## States

### Default — tasks exist for today
Score computed. Chart shows all completed-today tasks. Context switch count shown.

### Empty — no tasks today
```
[below header]
  "nothing yet"             (bodyLarge, WillTextSecondary)
  "complete tasks to see your focus score"  (labelSmall, GeistMono, WillTextTertiary)
```
Score and chart sections hidden. Only the header and empty state shown.

### Loading
`FocusDashboardViewModel` computes metrics synchronously in `init {}` from the already-loaded `StateFlow<List<Task>>`. No async load. Screen renders immediately.

### Error
Log and show nothing. Dashboard failure does not affect launcher core functionality.

---

## Interactions

| Trigger | Effect |
|---------|--------|
| Tap back arrow | Navigate back to HomeScreen |
| Tap chart bar | No interaction — chart is read-only |

No tap interactions on chart elements for MVP. This is consistent with STYLE.md (no engagement mechanics) and the "dashboard data is historical" finding from research.

---

## Accessibility

**Score:** `Text` with `contentDescription = "focus score: ${score} out of 100"`.

**Chart:** `Canvas` is non-interactive. Wrap in `Box` with `semantics { contentDescription = "time on task chart: ${taskSummaries}" }` where `taskSummaries` is a comma-separated string of `"${task.title}: ${task.durationMinutes} minutes"`.

**Context switches count:** `contentDescription = "${switchCount} app switches today"`.

**Section headers:** `semantics { heading() }`.

---

## Image resources

None. Chart is rendered via Canvas. All content is data-driven text and geometric primitives.

**Icons used:**
- `Icons.AutoMirrored.Outlined.ArrowBack` (24dp, WillTextSecondary) — back button
