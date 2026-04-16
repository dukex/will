# Competitor UX Analysis
> Phase: research | Project: Will | Generated: 2026-04-16

---

## Niagara Launcher

**Core interaction model:** Single-column alphabetical text list. No icons by default. Apps grouped by first letter of name, accessible by tapping the letter on the right-side quick-scroll rail.

**Gesture map:**
- Swipe down from anywhere: notification drawer
- Swipe up from anywhere: Niagara search (instant, always available)
- Tap letter on rail: jump to app group
- Tap and hold app: options menu (uninstall, info, shortcuts)
- Swipe left/right on notification: dismiss (built into OS notification surface, not the launcher itself)

**Information density:** Extremely low — single line per app (name only), large font, generous vertical spacing. Designed for one-handed thumb use. No meta-information on home screen. Notification badges shown as a dot next to app name — not a count.

**What works:** The text-only model eliminates icon recognition overhead. Users know app names, not icon shapes. Search-as-primary-navigation is faster than visual scanning for power users. Single gesture for search feels like a keyboard shortcut.

**What doesn't work:** No task or intent layer. Notifications are still in the system drawer — they're not converted to actionable items. There's no concept of "what do I need to do" — only "which app do I want to open." The launcher is a better directory, not an assistant.

**Interaction density gap:** Niagara gives you apps. Will needs to give you intentions with apps as the execution layer underneath.

Source: [Niagara Launcher Help — Gesture Navigation](https://help.niagaralauncher.app/article/7-gesture-navigation), [Niagara Medium review](https://medium.com/@spence7890/android-lets-take-a-quick-look-at-niagara-launcher-7d05adbb0d00), [Play Store listing](https://play.google.com/store/apps/details?id=bitpit.launcher)

---

## Minimalist Phone

**Core interaction model:** Monochrome all-text interface, radically similar to Niagara but with a wellness framing. App names in plain text, no icons. Battery indicator as a circular progress ring surrounding the clock. Usage timers per app surfaced inline.

**Gesture map:**
- Tap app name: launch
- Long-press app: access usage stats or set timer
- Swipe up: app drawer (text list)
- No horizontal swipes at home screen level

**Information density:** Arguably the densest in terms of meta-information per app — usage time shown beneath each app, daily goal progress shown in the clock circle. But per-item density is low: name + timer, no icons.

**What works:** Showing usage time directly on the launcher creates behavioral friction — seeing "Instagram: 2h 14m" before tapping actually reduces impulsive app opens. The battery-as-clock-ring is clever: high-value information delivered without adding UI elements.

**What doesn't work:** Wellness framing is patronizing at scale. Treating users as needing to be protected from their own phone is a different philosophy from Will. Minimalist Phone adds psychological friction to all app access — Will should add zero friction to intentional actions and high friction to reactive/distraction-driven ones.

**Settings pattern:** All app timers, blockers, and toggles live in a settings screen accessible from the home screen. Single entry point. Settings scroll as a flat list — no nested navigation depth beyond one level.

Source: [minimalistphone.com](https://www.minimalistphone.com/), [Toptal minimalist UX article](https://www.toptal.com/designers/mobile-ux/minimalist-ux-design)

---

## Linear (Mobile App)

**Relevance:** Linear is the best analog for Will's task feed UX, not as a launcher, but as a task management surface that gets density, hierarchy, and swipe actions right on mobile.

**Core interaction model:** Issue feed as a vertical list. Each item shows: status dot (colored), title, priority indicator, assignee avatar. Compact row height — approximately 48–52dp per item.

**Swipe gestures:**
- Swipe right: complete / change status
- Swipe left: delete or archive
- Tap item: expand to detail
- Long-press: bulk select mode

**Information density:** Medium-high. Three to four data points per row, but the visual hierarchy is clear: title dominates, metadata is muted. Status color is the only chromatic element — everything else is grayscale.

**Empty states:** When a project has no issues, Linear shows a centered text line ("No open issues") and a ghost-button to create the first issue. No illustrations. No copy beyond the functional minimum.

**What works for Will:**
- Swipe direction semantics match the pattern Will should adopt (right = affirmative, left = remove)
- Status indicator (dot) per item is the right pattern for Will's TaskStatusIndicator
- Information hierarchy: primary text bold, metadata regular weight, muted color — exactly what Will's type scale should do

**What doesn't work for Will:** Linear has full navigation (tabs, sidebar, modal detail screens). Will is a launcher — the task item tap should fire the source app's PendingIntent, not open a detail screen. The action complexity of Linear is inappropriate for a launcher context.

Source: [Linear Mobile](https://linear.app/mobile), [Linear design refresh article](https://linear.app/now/behind-the-latest-design-refresh)

---

## Pattern Comparison Matrix

| Dimension | Niagara | Minimalist Phone | Linear Mobile | Will (target) |
|-----------|---------|-----------------|---------------|---------------|
| Primary metaphor | App directory | Wellness throttle | Issue tracker | Intent executor |
| Information per item | Name only | Name + usage time | Title + status + meta | Title + source + status |
| Swipe actions | None (home) | None (home) | Right=done, Left=delete | Right=done, Left=delete |
| Search | Gesture-activated | Gesture-activated | Persistent search bar | Not in scope for MVP |
| Empty states | N/A | N/A | Minimal text | Minimal text, no illustrations |
| Settings access | Single tap (gear icon) | Single tap | Nav rail item | Long-press or inline icon |
| Notification handling | Delegates to system | Delegates to system | Separate inbox | Converted to tasks |

---

## Opportunity Gaps

1. **No competitor converts notifications to structured tasks.** Niagara and Minimalist Phone surface notification counts; neither extracts intent from notification content and makes it actionable as a first-class item.

2. **No competitor tracks task lifecycle across apps.** Linear does lifecycle tracking but only within its own data. No launcher tracks "I tapped the WhatsApp task, spent 8 minutes, and then closed WhatsApp" as a meaningful unit of work.

3. **No competitor provides a voice/text creation surface at the launcher level.** Google Assistant comes close but is a separate app, not integrated into the home screen task feed.

4. **Swipe gestures on competitor launchers are underused at the home screen level.** Niagara explicitly reserves horizontal swipes for notifications. Will can own the right-swipe pattern at the task item level without conflicting with global OS gestures.
