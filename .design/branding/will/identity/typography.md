# Typography
> Phase: identity | Brand: will | Generated: 2026-04-15

## Primary: Geist Sans

Geist Sans is a Vercel-commissioned typeface built for software product interfaces. It has the visual DNA of the Linear/Raycast/Vercel cluster — sharp terminals, neutral geometry, high legibility at small sizes on dark backgrounds. It reads as engineered, not designed.

For Will, this matters. Lucas trusts tools that look like they were built by someone with opinions. Geist Sans signals that.

**Use Geist Sans for:** task titles, app names, bottom sheet labels, button text, onboarding copy, settings labels — any text that is content, not data.

---

## Secondary: Geist Mono

Geist Mono is the monospace companion to Geist Sans, sharing the same optical metrics and visual weight at equivalent sizes. The pairing is seamless — they can share a line without visual tension.

The strategic rationale is identical to syntax highlighting. When text is set in Geist Mono, it means: this is data, not prose. The user does not have to read to know what kind of content it is — the typeface communicates the category.

**Use Geist Mono for:** timestamps, notification source labels, task IDs, metadata rows, any system-generated or frequently-changing values.

**Never use Geist Mono for:** task titles, action buttons, section headers, any text longer than a short label.

---

## Type Scale

Scale ratio: **Major Second (1.125)** — compact progression appropriate for an information-dense launcher UI. Editorial scales (1.333+) waste screen real estate.

Base: **14sp** (standard Android body text baseline at 360dp viewport)

| Level | Token | sp | rem | Weight | Line Height | Letter Spacing | Usage |
|-------|-------|----|-----|--------|-------------|----------------|-------|
| xs | `type-xs` | 11sp | 0.688rem | 400 | 1.45 (16sp) | +0.01em | Timestamps, source labels (Geist Mono) |
| sm | `type-sm` | 12sp | 0.750rem | 400 | 1.50 (18sp) | 0 | Secondary metadata (Geist Mono) |
| md | `type-md` | 13sp | 0.813rem | 500 | 1.46 (19sp) | 0 | Section headers (Geist Sans 500) |
| base | `type-base` | 14sp | 0.875rem | 400 | 1.50 (21sp) | 0 | Action labels, empty states (Geist Sans) |
| lg | `type-lg` | 15sp | 0.938rem | 400 | 1.47 (22sp) | -0.01em | Task titles (Geist Sans) |
| xl | `type-xl` | 16sp | 1.000rem | 400 | 1.50 (24sp) | -0.01em | Primary task titles, prominent labels |

All sizes tested at 360dp viewport width minimum (the Android constraint floor for Will).

---

## Weight strategy

400 Regular for nearly everything. 500 Medium for section headers only.

No 700 Bold anywhere. Bold in a launcher reads as badge energy — it draws the eye involuntarily, which is exactly the engagement mechanic Will refuses. The flat typographic weight is a structural statement: everything in the feed is equal priority until the user decides otherwise.

| Weight | Use |
|--------|-----|
| 400 | All content: task titles, labels, action text, metadata |
| 500 | Section headers only, when hierarchy must be explicit |
| 600+ | Never — reserved for icon variant only (logo) |

---

## Line height and letter spacing

Line heights are tight (1.45–1.50) — this is a compact data interface, not a reading environment. Generous line height would reduce information density without increasing readability at these sizes.

Letter spacing rules:
- `+0.01em` for the smallest sizes (11–12sp) — compensates for tight rendering at small sizes
- `0` for mid sizes (13–14sp) — Geist Sans default is well-calibrated here
- `-0.01em` for task title sizes (15–16sp) — tightens slightly, reads as a unit

---

## Android font loading

Both Geist fonts are licensed under the SIL Open Font License. For Will, **bundle the fonts directly** — do not use Downloadable Fonts API (requires network access, inconsistent availability offline, contradicts Will's privacy-first approach).

### Bundling in Android
```
app/src/main/res/font/
  geist_sans_regular.ttf       # weight 400
  geist_sans_medium.ttf        # weight 500
  geist_mono_regular.ttf       # weight 400
```

Declare in `res/font/geist_sans.xml` and `res/font/geist_mono.xml` as font family XML files. Reference via `@font/geist_sans` in theme attributes.

### Compose TextStyle definitions
```kotlin
val TaskTitleStyle = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Normal,  // 400
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.01).em
)

val MetadataStyle = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Normal,  // 400
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.01.em
)
```

### Fallback chain
```
Geist Sans → Inter → Roboto (Android system default)
Geist Mono → JetBrains Mono → Roboto Mono → monospace
```

---

## Vertical rhythm

Base unit: 8dp grid (Android standard touch target baseline)

- Minimum touch target: 48dp height
- Task card padding: 12dp vertical, 16dp horizontal
- Section header spacing: 8dp above, 4dp below
- Between task cards: 1dp divider or 8dp gap (flat separator, no shadow)

---

## Related

- [color-system.md](./color-system.md)
