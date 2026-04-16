# Mood Board Direction
> Brand: Will | Phase: discover | Date: 2026-04-15

## Brand Heartbeat Reminder

> "A marca que recusa participar da economia da atenção — mesmo sendo um app de celular."

This is the frame every visual decision lives inside. Will is not a calming app. It is not a beautiful app. It is a tool with an opinion, built to work, built to disappear.

---

## Color Palette

OLED-first. One accent. No decoration.

### Base Surfaces

| Role | Hex | Usage |
|------|-----|-------|
| Background | `#000000` | Wallpaper layer, true OLED black — this is the home screen itself |
| Surface 1 | `#0D0D0D` | Task cards, primary containers |
| Surface 2 | `#161616` | Elevated elements, sheets, overlays |
| Surface 3 | `#202020` | Secondary containers, hover states |
| Border | `#2A2A2A` | Dividers, card edges at rest |
| Border active | `#404040` | Card edges on focus / interactive state |

### Typography

| Role | Hex | Usage |
|------|-----|-------|
| Primary text | `#F0F0F0` | Task titles, primary labels |
| Secondary text | `#888888` | App names, timestamps, metadata |
| Tertiary text | `#505050` | Disabled states, placeholders |

### Accent

Will uses one accent only. It signals action — not mood, not brand personality.

| Role | Hex | Rationale |
|------|-----|-----------|
| Action accent | `#4F6EF7` | Cold indigo-blue — technical, not warm. Reads as "interactive" in the developer tool tradition (matches Linear/Raycast palette lineage). High contrast on `#000000` surface. |
| Destructive | `#E05252` | Delete, error — semantic only, never decorative |
| Success / active | `#4CAF74` | Task done, notification processed — semantic only |

### What the palette is not

- No warm amber. Amber suggests "cozy productivity" (Notion, Bear). Will is not cozy.
- No purple. Purple reads startup-generic.
- No gradients. Zero.
- No white backgrounds. Will is OLED-first. Light mode is not a priority for the MVP.

---

## Typography

### Primary — Interface Labels, Task Titles

**Geist Sans** (Vercel's typeface, open source)
- Geometric, neutral, engineered for developer interfaces
- Excellent legibility at small sizes on dark surfaces
- The typeface Vercel uses across its entire product and dashboard
- Available: https://vercel.com/font

Fallback: **Inter** — the most used geometric sans in developer tooling (Figma, Linear, Notion). If Geist Sans is unavailable, Inter is the uncontested choice.

Weight usage:
- Task titles: 400 (Regular) — confident but not heavy
- App names / metadata: 400 (Regular) at reduced opacity
- Status labels: 500 (Medium) for clarity at small sizes
- Nothing at 700+ — bold type in a launcher reads as notification badge energy

### Secondary — Technical Labels, Timestamps, Keyboard Shortcuts

**JetBrains Mono** (JetBrains, open license)
- Standard monospace in developer community (VS Code, IntelliJ default)
- 139 programming ligatures; excellent hinting at small sizes
- Use only for: notification source labels, timestamps, status codes, any "metadata" content
- Do not use for primary task titles — monospace at heading size reads as terminal cosplay

Alternative: **Geist Mono** (Vercel) — pairs perfectly with Geist Sans, same design system, same visual weight. Geist Mono + Geist Sans is the most coherent pairing.

### Type Scale (for reference — Android sp units)

| Role | Size | Weight | Font |
|------|------|--------|------|
| Task title | 16sp | 400 | Geist Sans |
| App / source label | 12sp | 400 | Geist Mono |
| Timestamp | 11sp | 400 | Geist Mono |
| Section header | 13sp | 500 | Geist Sans |
| Overlay action | 15sp | 500 | Geist Sans |

---

## Imagery Direction

Will is a launcher. It does not have marketing photography in the UI. The "imagery" is the interface itself.

**For any external-facing assets (GitHub README, landing page, store listing):**

- Screenshots only — no mockup devices, no lifestyle photography, no illustrations
- Screenshots on a true black (#000000) background
- Tight crops on real interactions: a task card, the notification-to-task transition, the overlay in action
- No rounded phone mockups with gradients. Flat art with real UI.
- If text is needed, it is code-style: monospaced, left-aligned, high contrast

**The aesthetic reference is a well-crafted README.md, not a Dribbble shot.**

Avoid absolutely:
- Person holding a phone looking focused
- Pastel cards floating in space
- "Mindfulness" imagery (plants, candles, calm waters)
- App icons arranged in rainbow gradient grids

---

## Overall Feel

**Terminal energy, architectural precision, zero decoration.**

If Linear is a project management tool that looks like a designer built it, Will is a launcher that looks like a senior engineer with design taste built it for themselves and decided to ship it.

The feeling when opening Will should be: you picked up the phone for something, and the thing is right there. No performance. No onboarding. No suggestions. Just the work.

Every visual element exists because it earns its place. Borders because they define task edges cleanly. Monospace type because it signals "this is data, read it." True black because the screen should not glow when it's not needed.

The reference is not just Linear/Vercel/Raycast — it is the gap between those products and what they'd look like if they were a home screen. Will lives in that gap.

---

## Style Affinity

Based on the brand brief, trend research, competitor audit, and Lucas persona, here are the recommended presets, ordered by fit:

### 1. `terminal` — Primary recommendation

**Tags matched:** developer, monospace, dark, minimal, technical

**Rationale:** Will's brand heartbeat is the refusal to participate in the attention economy. The terminal preset encodes this literally — it is the aesthetic of a tool that predates engagement mechanics entirely. Lucas lives in terminals. The monospace grammar says "data, not content." The flat dark surfaces say "function, not experience." This is the most architecturally honest choice for a launcher whose primary metaphor is "your phone as a focused work tool."

Specific alignments:
- True black surfaces = OLED optimization + terminal floor
- Monospace labels = technical metadata (notification source, timestamp, app name)
- No rounded decoration = command-line discipline
- Single accent color = the cursor — one thing is active, everything else waits

### 2. `nothing` — Secondary reference

**Tags matched:** monochrome, industrial, dark, minimal, technical, mechanical

**Rationale:** Nothing OS has proven that monochrome + OLED black + geometric type is legible and marketable on Android. It validates Will's direction as something users will actually accept on a home screen. Where Will diverges: Nothing's aesthetic is brand expression (it is "Nothing Phone DNA"). Will's aesthetic is function expression. Borrow Nothing's spatial discipline and OLED commitment; reject Nothing's self-consciousness. The `nothing` preset is the proof-of-concept that this visual language works on Android; Will is the version that has a reason for looking this way, not just a vibe.

### 3. `modern-dark` — Accent reference only

**Tags matched:** modern, dark, cinematic, linear, vercel

**Rationale:** Linear/Vercel aesthetic — ambient presence, precise contrast, indigo accent. The `modern-dark` preset informs Will's accent color choice (`#4F6EF7`) and the typographic scale. Do not use the full preset (ambient blobs and mouse spotlights have no place in a mobile launcher). Use it as the reference for: accent hue, surface step values, and the tone of typography at rest.

**What to avoid from this preset:** animated background effects, spotlight/ambient glow treatments — these are desktop web patterns that would violate Will's "no decoration" rule on mobile.

---

## Summary Spec

| Element | Decision |
|---------|----------|
| Background | `#000000` OLED true black |
| Primary surface | `#0D0D0D` |
| Elevated surface | `#161616` |
| Border | `#2A2A2A` at rest / `#404040` active |
| Primary text | `#F0F0F0` |
| Secondary text | `#888888` |
| Accent | `#4F6EF7` (cold indigo) — action only |
| Destructive | `#E05252` |
| Success | `#4CAF74` |
| UI typeface | Geist Sans (fallback: Inter) |
| Technical labels | Geist Mono (fallback: JetBrains Mono) |
| Visual grammar | Flat, OLED-first, monospace accents, no shadows, no gradients |
| Reference preset(s) | terminal (primary), nothing (spatial reference), modern-dark (accent only) |

---

Sources:
- [Dark Mode UI Best Practices 2025 — Graphic Eagle](https://www.graphiceagle.com/dark-mode-ui/)
- [Geist Font — Vercel](https://vercel.com/font)
- [JetBrains Mono — JetBrains](https://www.jetbrains.com/lp/mono/)
- [Inter + JetBrains Mono pairing](https://maxibestof.one/typefaces/inter/pairing/jetbrains-mono)
- [Linear Design 2025 — LogRocket](https://blog.logrocket.com/ux-design/linear-design/)
- [The Linear Look — Frontend Horse](https://frontend.horse/articles/the-linear-look/)
- [ComfyUI Linear Theme (Linear+Vercel+Raycast reference)](https://github.com/Arroz-11/ComfyUI-Linear-Theme/tree/master/)
