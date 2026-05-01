# design system

Will has a complete brand identity at `.design/branding/will/patterns/`. this page summarizes the rules that show up most often when contributing UI. the source of truth is `will.yml`.

## tokens

| token | value | use |
|-------|-------|-----|
| background | `#000000` | home screen, OLED true black |
| foreground | `#F0F0F0` | primary text |
| muted-foreground | `#888888` | timestamps, source labels, metadata |
| card | `#0D0D0D` | task cards, list surfaces |
| popover | `#161616` | bottom sheets, overlay panels |
| primary / accent / ring | `#4F6EF7` | interactive elements only |
| destructive | `#E05252` | delete and unrecoverable actions |
| border | `#2A2A2A` | structural divider, card edge at rest |
| border-active | `#404040` | card edge on focus or active |

## typography

- Geist Sans for everything the user reads (titles, labels, action text)
- Geist Mono for system-generated values (timestamps, source labels, task IDs)
- weight 400 (Regular) for body, weight 500 (Medium) for headings — never 600, never 700

## shape

- task cards: 0dp radius. flat, undecorated. the 0dp is load-bearing — never override
- adaptive icon grid: 4dp radius
- overlay panels and bottom sheets: 8dp radius
- 1dp borders define structure. shadows do not exist

## non-negotiable rules

these are enforced in the codebase. break one, and the brand drifts.

- `background = Color(0xFF000000)` — true black, never dark gray
- accent `#4F6EF7` only on interactive elements. M3 leaks `primary` into Snackbar, Checkbox, Switch, CircularProgressIndicator, Chip — override explicitly
- `FontWeight.Normal` for content, `FontWeight.Medium` for section headers
- `MaterialTheme.typography.labelSmall` uses Geist Mono — timestamps and source labels only, never titles
- `extraSmall = RoundedCornerShape(0.dp)` — task cards always 0dp
- no shadows, no gradients, no elevation values
- 48dp minimum touch target (`Spacing.touchTarget`)
- use `Spacing.lg` (16dp), `Spacing.xl` (24dp), etc. — never hardcode dp

## voice rules

- sentence case everywhere. product name `Will` is capitalized, everything else lowercase
- no exclamation marks. one breaks the voice permanently
- no emoji in product or documentation surfaces
- no wellness language: `calm`, `balance`, `mindful`, `focus mode`
- no engagement mechanics: `streaks`, `badges`, `achievements`, `score indicators`

## launcher constraints

Will is a launcher, not a regular app. these rules govern every layout:

- no app chrome — no `TabRow`, no `TopAppBar`, no bottom navigation
- the task feed is the home screen, edge-to-edge, status bar overlays content
- the app grid is secondary, reachable through an explicit button (and a swipe-up bonus)
- no decorative containers around the main content — the screen is the launcher
- permission prompts are small banners at the bottom — never full-screen dialogs

## further reading

- `.design/branding/will/patterns/will.yml` — every token in machine-readable form
- `.design/branding/will/patterns/STYLE.md` — implementation rules
- `.design/branding/will/patterns/components/token-mapping.md` — Compose Material 3 mapping
- `.design/branding/will/patterns/guidelines.html` — visual reference, open in a browser
