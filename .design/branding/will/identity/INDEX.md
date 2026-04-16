# Identity
> Phase: identity | Brand: will | Generated: 2026-04-15

Approach: Selective terminal — flat pane structure, monospace for data, zero decoration. Accent `#4F6EF7` cold indigo. Geist Sans for content, Geist Mono for technical labels. Neutral + Single Accent composition.

| Chunk | File | Lines |
|-------|------|-------|
| Logo Directions | [logo-directions.md](./logo-directions.md) | ~120 |
| Color System | [color-system.md](./color-system.md) | ~110 |
| Typography | [typography.md](./typography.md) | ~65 |
| Imagery Style | [imagery-style.md](./imagery-style.md) | ~75 |
| Brand Applications | [brand-applications.md](./brand-applications.md) | ~110 |

## Key decisions at this phase

- Logo: three directions proposed — wordmark as tool name, bracket mark + wordmark, wordmark with weight-shift double-L. Selection deferred to user.
- Color: `#4F6EF7` is the only accent. It appears only on interactive elements. Never decorative.
- Type: Geist Sans (content) + Geist Mono (data). Max weight 500. No 700 anywhere.
- Imagery: the UI is the imagery. No device mockups, no lifestyle photography, no illustration.
- Dark only for MVP. No light mode, intentionally.

## Next steps

- Select one logo direction — then run `/gsp-logo --enrich` for construction geometry, safe zones, and final asset export specs
- Run `/gsp-color --enrich` for OKLCH values, WCAG contrast ratios, and Material You token mapping
- Run `/gsp-typography --enrich` for precise type scale, line-height, letter-spacing, and Android sp values
- Run `/gsp-visuals --imagery --enrich` for icon library selection and full iconography spec
