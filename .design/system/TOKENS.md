# Tokens
> Design System Analysis | Generated: 2026-04-16

## Token Source

**Token source:** `app/src/main/java/tech/benedicta/will/ui/theme/Theme.kt`
**Format:** Compose `ColorScheme` — `darkColorScheme()` / `lightColorScheme()` with `Color(0xFFRRGGBB)` literals

## Token Coverage

| Category | Defined? | Details |
|----------|----------|---------|
| Colors | Partial | `WillTheme` defines a color scheme, but values are **Material3 defaults** (purple `#6750A4`), not brand colors. Brand palette (`#4F6EF7`, `#000000`, `#F0F0F0`, etc.) is not applied anywhere. |
| Typography | No | No custom `Typography` object. `MaterialTheme.typography.*` uses M3 defaults — no Geist Sans/Mono, no brand type scale. |
| Spacing | No | All spacing is hardcoded `.dp` literals in composable files (8.dp, 12.dp, 16.dp, 24.dp). No spacing token system. |
| Radii | No | No shape customization. M3 defaults apply. |
| Shadows / Elevation | No | Not applicable — brand specifies zero shadows. M3 elevation not overridden. |
| Dark mode | Partial | `WillTheme` accepts `ThemeMode` (DARK / LIGHT / SYSTEM) and switches color schemes. But the dark scheme uses M3 defaults, not brand's OLED-first true black palette. |

## Theme Configuration

Current `Theme.kt` (to be replaced with brand tokens):

```kotlin
// Current — Material3 defaults, NOT brand colors
private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),      // should be: #4F6EF7
    secondary = Color(0xFFCCB2DC),     // should be: #888888
    tertiary = Color(0xFFEFB8C8),      // should be removed/remapped
    surface = Color(0xFF1B1B1F),       // should be: #0D0D0D (surface-1)
    background = Color(0xFF1B1B1F)     // should be: #000000 (OLED true black)
)
```

**Target mapping** (from brand identity):

```kotlin
// Brand color scheme — target state
background    → #000000  (OLED true black)
surface       → #0D0D0D  (surface-1, task cards)
surfaceVariant → #161616 (surface-2, sheets/overlays)
primary       → #4F6EF7  (accent — interactive/actionable)
onBackground  → #F0F0F0  (text-primary)
onSurface     → #F0F0F0  (text-primary)
onSurfaceVariant → #888888 (text-secondary)
outline       → #2A2A2A  (border)
outlineVariant → #404040 (border-active)
error         → #E05252  (destructive)
```

## Prior GSP Tokens

Brand identity complete at `.design/branding/will/identity/`:
- `color-system.md` — full brand palette, semantic mapping, WCAG audit
- `typography.md` — Geist Sans + Geist Mono, Major Second scale (11sp–16sp)
- `palettes.json` — OKLCH scales for accent (#4F6EF7), destructive (#E05252), success (#4CAF74)

No prior patterns/tokens at `.design/branding/will/patterns/` — this is what Phase 4 (Guidelines) will produce.
