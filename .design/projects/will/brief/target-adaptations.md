# Target Adaptations
> Phase: brief | Project: Will | Generated: 2026-04-16

## Platform

Android + Jetpack Compose + Material3. All design maps to Compose primitives.
Theme applied via `WillTheme` in `ui/theme/Theme.kt`.

---

## Token overrides (project-specific)

No token overrides — Will brand tokens already cover the full project.
All color, typography, shape, and spacing decisions come from:
→ `.design/branding/will/patterns/components/token-mapping.md`

One addition needed: **task lifecycle state colors** mapped to existing brand tokens.

| State | Token | Color |
|-------|-------|-------|
| pending | `WillTextTertiary` | `#505050` |
| in-progress | `WillAccent` | `#4F6EF7` |
| done | `WillSuccess` | `#4CAF74` |
| deleted | `WillDestructive` | `#E05252` |

These are semantic reads from existing tokens — no new color values needed.

---

## Component adaptations

### TaskItem — swipe + lifecycle state

- Wrap with `SwipeToDismissBox` (M3) — trailing action = "Done" (WillSuccess), leading = "Delete" (WillDestructive)
- Add `TaskStatusIndicator` — 8dp dot before task title. Color = state token above.
- Tap zone must cover full row — 48dp minimum via `Modifier.heightIn(min = Spacing.touchTarget)`
- → Brand ref: `.design/branding/will/patterns/will.yml` (density: 8, touchTarget: 48dp)

### HomeTopBar — inline action touch targets

- Done / Later / Delete are currently plain `Text(...).clickable(...)` — no guaranteed 48dp
- Wrap each in `Box(modifier = Modifier.size(Spacing.touchTarget), contentAlignment = Center)` containing the `Text`
- Semantic roles: add `Modifier.semantics { role = Role.Button; contentDescription = "..." }`
- → Brand ref: `will.yml` constraints: "touch targets ≥ 48dp"

### IntentInputSheet — new bottom sheet

- Use `ModalBottomSheet` — `WillShapes.large` (8dp) applied automatically via theme
- `BasicTextField` with `WillTextPrimary` cursor, `WillBorder` bottom-only stroke (not box border)
- → Brand ref: `.design/branding/will/patterns/will.yml` (patterns.input, patterns.bottom-sheet)

### FocusDashboard — new screen

- No M3 chart primitive. Use `Canvas` composable for bar chart.
- Focus score: large `displaySmall` number in `WillTextPrimary`, label in `labelSmall` GeistMono
- Chart bars: `WillAccent` fill (interactive data = accent is correct use), `WillBorder` for empty slots
- Background: `WillBackground` (no card container around chart)
- **⚠ Override needed:** `CircularProgressIndicator` if used for score loading → override `color` to `WillTextTertiary` (not `WillAccent`)
- → Brand ref: will.yml constraint: "accent on interactive elements only"

### NotificationFilterSettings — new screen

- Full-screen list (no card containers) — matches launcher UX constraints
- Toggle row: `Switch` — **⚠ Override needed:** M3 Switch applies `primary` (WillAccent) to thumb by default. Override `colors = SwitchDefaults.colors(checkedThumbColor = WillTextPrimary, checkedTrackColor = WillAccent, uncheckedTrackColor = WillBorder)` — thumb is white, track is accent. This is correct: the toggle IS interactive.
- App name in `bodyMedium`, package name in `labelSmall` GeistMono
- Section header "Notifications" in `titleSmall` (FontWeight.Medium)

### TaskActionSheet — lifecycle context

- Add a `labelSmall` GeistMono line below task title: `"created 14:23 · 2m ago"` in `WillTextTertiary`
- Delete action: change from direct callback to confirm pattern — first tap shows "Confirm delete?" in `WillDestructive`, second tap executes
- → Brand ref: will.yml (voice: "dry, uncompromising") — no softening language, just clear state

---

## Implementation target mapping

| Design component | Compose primitive | Notes |
|-----------------|-------------------|-------|
| Task card | `Row` in `LazyColumn` | Custom layout, no Card composable |
| Bottom sheets | `ModalBottomSheet` | Theme applies 8dp corners automatically |
| Swipe actions | `SwipeToDismissBox` (M3) | Threshold 40% width |
| Text input | `BasicTextField` | Not `TextField` — avoids M3 box decoration |
| Charts | `Canvas` | No library in MVP — custom draw |
| Toggle | `Switch` | Override colors (see above) |
| Icon buttons | `IconButton` | Guarantees 48dp touch target |
| Section headers | `Text(style = titleSmall)` | FontWeight.Medium via theme |
| Metadata / timestamps | `Text(style = labelSmall)` | GeistMono via theme |
| Dividers | `HorizontalDivider(color = WillBorder)` | 1dp, #2A2A2A |
