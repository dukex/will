# Micro-interactions
> Phase: design | Project: Will | Generated: 2026-04-16

---

## Effects vocabulary (from STYLE.md)

All interactions use only three effects:
- `flat-press` — opacity drops to 0.6–0.7, no scale, no translate
- `border-brighten` — border transitions from `#2A2A2A` to `#404040`
- `opacity-dim` — secondary action dims to indicate non-primary state

Transition timing: `100ms ease-out` on all state changes. Responsive, not animated.

---

## Interaction table

| Trigger | Element | Effect | Duration | Compose implementation |
|---------|---------|--------|----------|------------------------|
| Press down on TaskItem | Entire row | `flat-press` — opacity 0.65 | 100ms ease-out | `Modifier.clickable` with `indication = rememberRipple(false)` + `animateColorAsState` on background |
| Release / lift on TaskItem | Entire row | Restore opacity 1.0 | 100ms ease-out | State reset |
| Swipe right on TaskItem (0–40%) | Row background | Lerp transparent → `WillSuccess` with `state.progress` | Continuous | `SwipeToDismissBox` background content |
| Swipe left on TaskItem (0–40%) | Row background | Lerp transparent → `WillDestructive` with `state.progress` | Continuous | `SwipeToDismissBox` background content |
| Swipe exceeds 40% threshold | Row | Animate dismiss (M3 default) | System-defined | `confirmValueChange` returns true/false |
| Task inserted into feed | TaskItem | Slide in from top (implicit) | M3 `animateItem()` | `Modifier.animateItem()` on each item |
| Task removed from feed | TaskItem | Fade + shrink (implicit) | M3 `animateItem()` | `Modifier.animateItem()` on each item |
| Press down on HomeTopBar icon | Icon | `flat-press` — opacity 0.6 | 100ms ease-out | `IconButton` default ripple (no-bounds) |
| Press down on action button (TaskActionSheet) | Button | `flat-press` — opacity 0.65 | 100ms ease-out | `Modifier.clickable` |
| Delete first tap → confirm state | Delete button text | `opacity-dim` — "delete?" label replaces "delete", `WillDestructive` | Instant | State toggle |
| Switch toggle (FilterSettings) | Switch track | `uncheckedTrackColor` → `checkedTrackColor` (`WillAccent`) | System-defined | M3 Switch built-in |
| Screen enter (FilterSettings, Dashboard) | Whole screen | No animation — hard cut | Instant | `ActivityOptions.makeCustomAnimation` with no anim |
| Sheet open (IntentInputSheet) | Bottom sheet | Slide up from bottom | System M3 ModalBottomSheet | Default |
| Sheet dismiss | Bottom sheet | Slide down | System M3 ModalBottomSheet | Default |
| TaskStatusIndicator state change | Dot color | `WillTextTertiary` → `WillAccent` → `WillSuccess` | 100ms ease-out | `animateColorAsState` on dot `Color` |

---

## What is not used

Per STYLE.md constraints (motion intensity 2/10):
- No entrance animations on home screen elements
- No scroll-driven animations
- No parallax
- No typewriter effects
- No ambient pulse or glow
- No spring physics beyond what M3 provides by default in sheets
- No stagger animations on list items beyond `animateItem()`
