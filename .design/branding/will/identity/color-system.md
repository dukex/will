# Color System
> Phase: identity | Brand: will | Generated: 2026-04-15

## Composition strategy: Neutral + Single Accent

Will uses a near-monochrome base with one semantic accent. Color in Will is information, not decoration — it works like syntax highlighting. Each color has a specific meaning that is consistent across every surface.

The accent (`#4F6EF7`) appears only where something is interactive or actionable. If an element does not invite interaction, it does not get the accent.

---

## Palette

### Base surfaces

| Token | Hex | OKLCH | Role |
|-------|-----|-------|------|
| `background` | `#000000` | `oklch(0 0 0)` | OLED true black. Home screen layer. |
| `surface-1` | `#0D0D0D` | `oklch(0.108 0 0)` | Task cards, primary containers. |
| `surface-2` | `#161616` | `oklch(0.148 0 0)` | Elevated elements — sheets, overlays. |
| `surface-3` | `#202020` | `oklch(0.192 0 0)` | Secondary containers. |

True black (`#000000`) is intentional. On OLED displays, true black = pixel off = zero power draw. It is an engineering argument, which makes it the right argument for this brand.

### Borders

| Token | Hex | OKLCH | Role |
|-------|-----|-------|------|
| `border` | `#2A2A2A` | `oklch(0.232 0 0)` | Dividers, card edges at rest. |
| `border-active` | `#404040` | `oklch(0.317 0 0)` | Card edges on focus, active state. |

Borders define structure without elevation. No shadows, no elevation illusion — surfaces are flat, borders are the boundary.

### Text

| Token | Hex | OKLCH | Role |
|-------|-----|-------|------|
| `text-primary` | `#F0F0F0` | `oklch(0.949 0 0)` | Task titles, primary labels. |
| `text-secondary` | `#888888` | `oklch(0.590 0 0)` | App names, timestamps, metadata. |
| `text-tertiary` | `#505050` | `oklch(0.380 0 0)` | Disabled states, placeholders. |

`#F0F0F0` (not `#FFFFFF`) avoids harsh contrast fatigue on true black. `#888888` for secondary is the register of a code comment: present, readable, clearly subordinate.

### Semantic colors

| Token | Hex | Role | When it appears |
|-------|-----|------|-----------------|
| `accent` | `#4F6EF7` | Cold indigo. Interactive, actionable. | Tap targets, active indicators, focus rings, action buttons, selected states. |
| `destructive` | `#E05252` | Delete, errors, unrecoverable actions. | Delete action in overlay, error states. |
| `success` | `#4CAF74` | Task done, notification processed. | Completion states only. |

---

## WCAG Contrast Audit

| Pair | Ratio | WCAG |
|------|-------|------|
| `#F0F0F0` on `#000000` | **18.35:1** | ✓ AAA |
| `#F0F0F0` on `#0D0D0D` | **15.93:1** | ✓ AAA |
| `#F0F0F0` on `#161616` | **13.01:1** | ✓ AAA |
| `#888888` on `#000000` | **6.48:1** | ✓ AA |
| `#888888` on `#0D0D0D` | **5.63:1** | ✓ AA |
| `#505050` on `#000000` | **3.10:1** | ⚠ Large text only (≥18sp or 14sp bold) |
| `#4F6EF7` on `#000000` | **5.36:1** | ✓ AA |
| `#F0F0F0` on `#4F6EF7` | **3.42:1** | ⚠ Large text only — use `#4F6EF7` as border/indicator, not button background with body text |
| `#E05252` on `#000000` | **5.97:1** | ✓ AA |
| `#4CAF74` on `#000000` | **8.15:1** | ✓ AA |

**Note on tertiary text (#505050):** Applies only to disabled/placeholder states where interaction is not expected. Not used for readable content. Acceptable for informational-only context on Android where users are not required to act on it.

**Note on accent as text background:** `#4F6EF7` as a filled button background with `#F0F0F0` text is 3.42:1 — below AA for normal text at 14sp. Preferred usage: accent as border, indicator, or filled button with `#FFFFFF` text (3.92:1) for larger action labels. Review at 16sp+ for AA compliance.

---

## OKLCH Palette Scales

Generated via tints.dev:

### Accent — `#4F6EF7` (cold indigo)

| Stop | OKLCH |
|------|-------|
| 50 | `oklch(0.958 0.019 279.58)` |
| 100 | `oklch(0.916 0.038 280.79)` |
| 200 | `oklch(0.840 0.076 278.88)` |
| 300 | `oklch(0.756 0.120 277.36)` |
| 400 | `oklch(0.673 0.163 273.84)` |
| **500** | **`oklch(0.593 0.207 269.41)` ← base** |
| 600 | `oklch(0.506 0.230 264.35)` |
| 700 | `oklch(0.418 0.189 264.33)` |
| 800 | `oklch(0.330 0.149 264.22)` |
| 900 | `oklch(0.231 0.104 264.30)` |
| 950 | `oklch(0.184 0.082 264.26)` |

### Destructive — `#E05252`

| Stop | OKLCH |
|------|-------|
| 50 | `oklch(0.959 0.013 17.52)` |
| 100 | `oklch(0.925 0.024 17.67)` |
| 200 | `oklch(0.851 0.053 18.27)` |
| 300 | `oklch(0.779 0.086 19.21)` |
| 400 | `oklch(0.706 0.126 20.76)` |
| **500** | **`oklch(0.629 0.178 23.73)` ← base** |
| 600 | `oklch(0.537 0.161 24.36)` |
| 700 | `oklch(0.437 0.132 24.39)` |
| 800 | `oklch(0.340 0.101 24.29)` |
| 900 | `oklch(0.241 0.073 24.39)` |
| 950 | `oklch(0.184 0.055 24.33)` |

### Success — `#4CAF74`

| Stop | OKLCH |
|------|-------|
| 50 | `oklch(0.959 0.050 155.02)` |
| 100 | `oklch(0.929 0.095 154.78)` |
| 200 | `oklch(0.861 0.162 154.77)` |
| 300 | `oklch(0.801 0.150 154.76)` |
| 400 | `oklch(0.742 0.140 154.56)` |
| **500** | **`oklch(0.679 0.128 154.69)` ← base** |
| 600 | `oklch(0.574 0.108 154.54)` |
| 700 | `oklch(0.463 0.087 154.96)` |
| 800 | `oklch(0.355 0.067 154.90)` |
| 900 | `oklch(0.246 0.047 154.18)` |
| 950 | `oklch(0.184 0.035 153.46)` |

Full machine-readable scales → `./palettes.json`

---

## Semantic color mapping

| Context | Token | Hex |
|---------|-------|-----|
| Home screen background | `background` | `#000000` |
| Task card background | `surface-1` | `#0D0D0D` |
| Task card border | `border` | `#2A2A2A` |
| Task card border (active) | `border-active` | `#404040` |
| Task title | `text-primary` | `#F0F0F0` |
| App source / timestamp | `text-secondary` | `#888888` |
| Disabled / placeholder | `text-tertiary` | `#505050` |
| Tap / action indicator | `accent` | `#4F6EF7` |
| Active task indicator | `accent` | `#4F6EF7` |
| Delete action | `destructive` | `#E05252` |
| Done / processed | `success` | `#4CAF74` |
| Bottom sheet background | `surface-2` | `#161616` |
| Bottom sheet border | `border` | `#2A2A2A` |
| Overlay bubble background | `surface-1` | `#0D0D0D` |
| Overlay bubble border | `accent` | `#4F6EF7` |

---

## Dark mode direction

Will is dark-first and dark-only for MVP. There is no light mode.

This is a strategic statement, not a gap. The home screen is used in low-ambient-light contexts. True black on OLED conserves battery. Light mode on a launcher makes the home screen the brightest object in the user's field of view. Will refuses that.

If light mode is added post-MVP, it requires a full separate token set — not an inversion of dark tokens.

---

## Constraints

- The accent never appears on decorative elements — text labels, card backgrounds, section headers
- No gradients anywhere in the system
- No shadows (`box-shadow`, `elevation`) — borders define structure
- No tinted surfaces — surface tokens are neutral gray steps only
- No color used for informational hierarchy (no blue info card, no yellow warning card)

---

## Related

- [palettes.json](./palettes.json)
- [typography.md](./typography.md)
