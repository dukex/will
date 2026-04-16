# Responsive
> Phase: design | Project: Will | Generated: 2026-04-16

---

## Platform constraint

Will is portrait-only Android (`screenOrientation=portrait`). There is no landscape layout, no tablet layout in scope for MVP.

The responsive surface is one: a portrait Android phone screen, minSdk 24, varying in width from ~360dp (compact) to ~412dp (large Pixel/Samsung).

---

## Width adaptations

| Element | 360dp (compact) | 393–412dp (standard/large) | Notes |
|---------|-----------------|---------------------------|-------|
| Screen horizontal padding | 24dp | 24dp | Fixed per brand spec — no fluid scaling |
| TaskItem padding | 12dp vertical, 16dp horizontal | 12dp vertical, 16dp horizontal | Fixed |
| TaskItem title | 15sp, wraps at 2 lines max | 15sp, wraps at 2 lines max | Ellipsis on line 3 |
| HomeTopBar | Space-between: date/time left, icons right | Same | No reflow |
| FocusDashboard score | `displaySmall` (36sp) | `displaySmall` (36sp) | No scaling |
| Bar chart width | Full width minus 48dp total padding | Full width minus 48dp | Canvas fills available width |
| NotificationFilterSettings row | Full width | Full width | Row stretches |

---

## Text scaling (Dynamic Type)

WCAG 2.2 AA requires that text is legible at 200% system text size without loss of content.

| Scale | Behavior |
|-------|----------|
| Default (100%) | All sizes as specified in WillTypography |
| 130% | Layout holds — most rows are tall enough to absorb 1-line text growth |
| 200% | Task titles wrap to 2 lines; sheet labels wrap; sheet height grows via `wrapContentHeight`. No content truncation except TaskItem title (2 lines max) |

Compose `sp` units respond to system text scale automatically. No manual `fontScale` overrides.

---

## Safe areas and insets

All screens use `WindowCompat.setDecorFitsSystemWindows(window, false)` + `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`.

| Area | Handling |
|------|----------|
| Status bar | Content draws behind; first content element padded by `safeDrawing.top` |
| Navigation bar (3-button or gesture) | `safeDrawing.bottom` applied to screen root padding |
| Keyboard | `windowSoftInputMode=adjustResize` on settings screens; `adjustPan` (or nothing) on HomeScreen |
| Cutouts / notches | `safeDrawing` covers display cutouts |

---

## Tablet / landscape

Out of scope for MVP. No design decisions needed. The app will not rotate or adapt to wide screen sizes.

---

## No tablet notes

When Will is eventually distributed on tablets, the primary adaptation would be:
- TaskFeed constrained to max 600dp centered column
- AppGrid in a multi-column grid (3–4 per row vs 4)

These are not designed in this phase.
