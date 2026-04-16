# Concerns
> Design System Analysis | Generated: 2026-04-16

## Design Debt

| Issue | File(s) | Severity | Fix Approach |
|-------|---------|----------|--------------|
| Brand colors not applied — `WillTheme` uses Material3 defaults (purple `#6750A4`), not brand palette (`#4F6EF7`, `#000000`) | `ui/theme/Theme.kt` | **high** | Replace `darkColorScheme()` values with brand tokens from `color-system.md` |
| No Geist fonts — typography uses Android system fonts, not Geist Sans / Geist Mono | `ui/theme/Theme.kt` | **high** | Bundle fonts in `res/font/`, declare `FontFamily`, pass custom `Typography` to `MaterialTheme` |
| Hardcoded dp values — spacing uses raw `.dp` literals (8.dp, 12.dp, 16.dp, 24.dp) throughout | All `ui/*.kt` | medium | Define spacing constants (8dp grid) in theme or a `Spacing` object |
| Vestigial XML resources — `themes.xml` uses purple/teal M1 colors; `colors.xml` defines `purple_500`, `teal_200` — none used at runtime | `res/values/` | low | Delete or clear both files to avoid confusion; the Activity's `android:theme` in Manifest can use a bare no-action-bar Material3 theme |

## Component Fragility

| Issue | File(s) | Severity | Fix Approach |
|-------|---------|----------|--------------|
| `FontWeight.SemiBold` (600) used in TaskItem and HomeTopBar — brand weight policy caps at 500 (Medium) | `ui/TaskFeed.kt:123`, `ui/HomeTopBar.kt:116` | medium | Replace with `FontWeight.Medium` (500) — matches brand rule "no 700 anywhere, 500 for section headers only" |
| `HomeTopBar` mixes concerns — battery receiver, clock timer, and active task actions all in one composable | `ui/HomeTopBar.kt` | medium | Extract battery/clock into a separate composable or ViewModel state; HomeTopBar then just renders |
| `TaskActionSheet` shows `task.text` as sheet title (body text), not `task.title` | `ui/TaskActionSheet.kt:49` | medium | Use `task.title` — the notification title is the identifying signal |
| `NotificationPermissionPrompt` uses `errorContainer` as banner background — semantic mismatch (this is not an error state, it's a first-run prompt) | `ui/NotificationPermissionPrompt.kt:19` | low | Use a neutral surface token or a dedicated `warningContainer` if added |

## Accessibility Gaps

| Issue | File(s) | Severity | Fix Approach |
|-------|---------|----------|--------------|
| Inline task actions (Done/Later/Delete) in `HomeTopBar` are plain `Text` with `.clickable{}` — no semantic role, no min touch target | `ui/HomeTopBar.kt:128-142` | **high** | Use `Modifier.semantics { role = Role.Button }` + ensure 48dp touch target |
| `AppItem` and `TaskItem` use `.clickable{}` without content description — screen readers announce nothing | `ui/AppGrid.kt:104`, `ui/TaskFeed.kt:113` | medium | Add `Modifier.semantics { contentDescription = "..." }` |
| `SheetAction` rows — no semantic role | `ui/TaskActionSheet.kt:79` | medium | Add `Role.Button` semantics |

## Token Coverage Gaps

| Category | Status | Details |
|----------|--------|---------|
| Colors | hardcoded | `WillTheme` values are M3 defaults, not brand tokens. Every component uses `MaterialTheme.colorScheme.*` (semantic) — good structure, wrong values. |
| Typography | hardcoded | `MaterialTheme.typography.*` uses M3 defaults. No Geist fonts, no brand scale (11–16sp Major Second). |
| Spacing | hardcoded | 8.dp, 12.dp, 16.dp, 24.dp raw literals. No spacing token object. |
| Radii | N/A | Brand specifies no corner radius on task cards. M3 shape defaults not overridden — worth setting `shapes = Shapes(...)` explicitly to prevent M3 defaults from leaking in. |
| Shadows / Elevation | N/A | Brand specifies zero shadows. M3 elevation not explicitly zeroed. |
| Dark mode | partial | Structure exists (`ThemeMode` enum, `WillTheme` parameter), but dark palette is M3 purple, not OLED black. |

## Dark Mode Gaps

| Issue | File(s) | Severity | Fix Approach |
|-------|---------|----------|--------------|
| App never reaches true black — `DarkColors.background = Color(0xFF1B1B1F)` instead of `Color(0xFF000000)` | `ui/theme/Theme.kt:20` | **high** | Set `background = Color(0xFF000000)` in `DarkColors` — the OLED argument is load-bearing |
| Light mode theme (`LightColors`) defined but brand is dark-only for MVP | `ui/theme/Theme.kt:11-17` | low | Keep light scheme stub but default `ThemeMode` to `DARK` in theme entry; document that light mode is post-MVP |

## Responsive Gaps

| Issue | File(s) | Severity | Fix Approach |
|-------|---------|----------|--------------|
| None — launcher is portrait-only by manifest, all layouts use `WindowInsets` and `fillMaxSize`/`fillMaxWidth`. Responsive is not a concern here. | — | N/A | N/A |

## Naming Inconsistencies

| Issue | File(s) | Severity | Fix Approach |
|-------|---------|----------|--------------|
| `ThemeMode.kt` and `ThemePreference.kt` both exist — `ThemeMode` is the enum, `ThemePreference` is the DataStore persistence; confusable at first glance | `ui/ThemeMode.kt`, `ui/ThemePreference.kt` | low | Clear by naming — already correct. Consider moving `ThemePreference` to `infrastructure/` to match the domain/infra split pattern. |

## Summary

- **High severity:** 3 (brand colors not applied, fonts not bundled, inline actions lack touch targets / semantics)
- **Medium severity:** 6 (SemiBold weight, HomeTopBar concerns, TaskActionSheet title, spacing literals, content descriptions, SheetAction semantics)
- **Low severity:** 3 (vestigial XML, permission prompt color, light mode stub)
- **Overall health:** needs-attention — structure is solid, domain/infra separation is correct, but the entire theming layer is Material3 defaults. Applying the brand tokens resolves the high-severity issues cleanly.
