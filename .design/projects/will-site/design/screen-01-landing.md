# Screen 01 — Landing Page
> Phase: design | Project: Will Site | Generated: 2026-04-16

---

## Purpose

Home page of the project site. Presents Will to developers who find the repo.
Communicates: what it is, why it's different, and where to get it.

---

## Layout

### Hero

```
┌─────────────────────────────────────────────┐
│                                             │
│  Will                                       │  ← display text, Geist Sans 400, #F0F0F0
│                                             │
│  an Android launcher that treats your phone │  ← bodyLarge, #888888
│  as a work surface, not an attention sink.  │
│                                             │
│  [view on github]   [read the docs]         │  ← primary + secondary buttons
│                                             │
│  ─────────────────────────────────────────  │  ← 1px #2A2A2A divider
│                                             │
└─────────────────────────────────────────────┘
```

- Background: `#000000` (OLED true black — same as the app)
- Hero text: `clamp(3rem, 8vw, 6rem)`, `font-weight: 400`, letter-spacing `-0.02em`
- No gradient, no illustration, no device mockup — the typography IS the visual
- Max width on text block: `640px`
- Horizontal padding: `24px` (same as app `screen-horizontal` token)

### Features section

```
┌──────────────────────────────────────────────┐
│                                              │
│  why Will                                    │  ← section label, Geist Mono 400, #888888, 11px
│                                              │
├──────────────────────────────────────────────┤  ← 1px #2A2A2A divider
│                                              │
│  notifications → tasks                       │  ← feature title, Geist Sans 400, #F0F0F0, 15px
│  every notification becomes an actionable    │  ← feature desc, Geist Sans 400, #888888, 13px
│  item. tap to open the source app.           │
│                                              │
├──────────────────────────────────────────────┤
│                                              │
│  no attention mechanics                      │
│  no streaks, no badges, no score indicators. │
│  your focus is the only metric.              │
│                                              │
├──────────────────────────────────────────────┤
│                                              │
│  OLED-first                                  │
│  true black (#000000). pixel off on OLED.   │
│  zero power draw on the home screen.         │
│                                              │
└──────────────────────────────────────────────┘
```

- Layout: vertical list of features, each separated by a 1px #2A2A2A divider
- No icons, no cards — pure text list (matches app's no-decoration philosophy)
- Feature titles: Geist Sans 400, `#F0F0F0`, 15px
- Feature descriptions: Geist Sans 400, `#888888`, 13px

### Download / CTA section

```
┌──────────────────────────────────────────────┐
│                                              │
│  open source                                 │  ← section label, Geist Mono 400, #888888
│                                              │
├──────────────────────────────────────────────┤
│                                              │
│  build it yourself or wait for the           │  ← bodyLarge, #888888
│  Play Store release.                         │
│                                              │
│  [view source ↗]   [open issues ↗]           │
│                                              │
└──────────────────────────────────────────────┘
```

### Footer

```
┌──────────────────────────────────────────────┐
│  Will · MIT License · made by Duke           │  ← Geist Mono 400, #505050, 11px
└──────────────────────────────────────────────┘
```

---

## VitePress layout

Use VitePress home layout (`layout: home`) configured via frontmatter, with a heavily customized theme.
Override VitePress CSS variables to match Will brand tokens.

Hero section uses VitePress `hero` frontmatter config.
Features section uses VitePress `features` config.

Custom footer via `.vitepress/theme/` custom layout override.

---

## Copy

### Hero
- **Name:** Will
- **Tagline:** an Android launcher that treats your phone as a work surface, not an attention sink.
- **CTA 1:** view on github (links to `https://github.com/dukex/will`)
- **CTA 2:** read the docs (links to `/docs/`)

### Features
1. **notifications → tasks** — every notification becomes an actionable item. tap to open the source app.
2. **no attention mechanics** — no streaks, no badges, no score indicators. your focus is the only metric.
3. **OLED-first** — true black (#000000). pixel off on OLED. zero power draw on the home screen.

### CTA section
- **Label:** open source
- **Body:** build it yourself or wait for the Play Store release.
- **Link 1:** view source ↗
- **Link 2:** open issues ↗

---

## Brand constraints (from STYLE.md)

- No gradients, no shadows, no rounded corners
- No device mockups or lifestyle photography
- No exclamation marks
- Sentence case everywhere (Will is capitalized, everything else lowercase)
- `#4F6EF7` only on interactive elements (buttons, links)
- Geist Mono for labels/metadata, Geist Sans for content
- 1px #2A2A2A border as structural separator

---

## Accessibility

- All interactive elements: `min-height: 44px` (WCAG 2.5.5 target size)
- Focus ring: `outline: 2px solid #4F6EF7`, `outline-offset: 2px`
- Color contrast: `#F0F0F0` on `#000000` = 18.08:1 (AAA)
- `prefers-reduced-motion`: disable all transitions
