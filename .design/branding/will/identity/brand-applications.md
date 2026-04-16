# Brand Applications
> Brand: will | Phase: identity | Date: 2026-04-15

Five touchpoints. Each is an independent surface with its own constraints and its own version of the brand. The system only holds if each application is internally consistent and visibly related to the others.

---

## 1. Launcher icon

The Android home screen icon. This is the most constrained surface — 48dp adaptive icon, rendered inside the system's icon shape (varies by launcher and Android version, typically squircle or circle).

**Visual direction:**
The selected logo direction's icon variant centered in a true black (`#000000`) adaptive icon background. The foreground layer uses the icon mark with enough padding to clear the system mask across all icon shape variants. No background tint, no gradient, no shadow on the icon layer.

**Typography:** None — icon only, no wordmark at this size.

**Color usage:** `#F0F0F0` mark on `#000000` background, or `#4F6EF7` mark if Direction 2 (bracket) is selected. No other colors.

**Voice:** No voice — icons do not speak. The icon's job is immediate recognition at a glance on the home screen.

**Constraint:** The icon must hold legibility at 48dp actual display size, not at the design file's 512px export size. Review at actual size on a physical device before committing.

---

## 2. Task feed

The primary home screen surface. This is where the brand lives at runtime — not in a splash screen, not in a settings panel. Every design decision made in the identity system is tested here.

**Visual direction:**
Full-screen, edge-to-edge. `#000000` background. Task cards on `#0D0D0D` surface with `#2A2A2A` border. No shadows, no elevation illusion. The border IS the structure.

Cards are left-edge-anchored, full width, with internal padding creating the information hierarchy. There is no card list margin — the feed runs wall to wall, pane-style.

**Typography applied:**
- Task title: Geist Sans 400, 15–16sp, `#F0F0F0`
- App source: Geist Mono 400, 11–12sp, `#888888`
- Timestamp: Geist Mono 400, 11sp, `#888888`, right-aligned
- Empty state: Geist Sans 400, 14sp, `#505050`, centered

**Color usage:**
- Accent (`#4F6EF7`) on the active task indicator only — a 2px left border or a small dot, not a full card tint
- `#4CAF74` on the task card only when it transitions to "done" state — brief confirmation, not persistent
- No accent on task titles, card backgrounds, or section headers

**Voice:**
The feed is silent. No microcopy unless a state requires it. Empty state: "No pending tasks." Not "You're all caught up!" Not "Nothing here yet — great job!" One sentence. Period.

---

## 3. GitHub README

Developer-facing first impression. This is the Will brand in the wild for Lucas's persona — he will find Will here before he installs it.

**Visual direction:**
Dark GitHub theme assumed (`github-dark`). README header uses the Will wordmark as a text heading (no image logo) or an SVG export of the wordmark if image embedding is used. No hero illustration. Screenshots embedded inline at natural README width, no device frames, dark backgrounds, tight crops.

Structure mirrors a serious open source tool README: what it does, how to install, what permissions it requires and why, how to contribute. No marketing copy in the technical sections.

**Typography applied:**
README is Markdown — GitHub's rendered typography. The Will typeface system does not apply here. The writing voice applies instead:
- Headers: sentence case, no all-caps, no emoji
- Descriptions: declarative, no enthusiasm markers
- Permission explanations: complete and honest. "Will requires notification access to convert notifications to tasks. This data never leaves your device."

**Color usage:**
If any colored elements appear (badges, labels), they must be minimal. Prefer text status over badge-heavy status rows.

**Voice notes:**
The README is the brand statement in prose form. Lead with the product promise: "Will is an Android launcher that converts notifications into tasks. Pick up your phone, do the thing, put it down." No origin story, no founding team section, no growth metrics.

---

## 4. Notification overlay

The `ActiveTaskOverlayService` floating bubble and its expanded panel. This is Will's most interactive surface — the system that appears on top of other apps when a task is active.

**Visual direction:**
Bubble: small, `#0D0D0D` background, `#4F6EF7` border (1–2px) — the accent border signals "this is Will, it is interactive." No shadow. The bubble drag handle is a muted indicator, not a colored affordance.

Expanded panel: `#161616` background (elevated surface), `#2A2A2A` border. Three action buttons in a row: Done, Later, Delete. Done uses `#4CAF74` label. Delete uses `#E05252` label. Later uses `#888888` label — it is the least urgent action.

No animation in the expanded transition beyond a fast fade — no spring, no bounce. Launchers that animate excessively are showing off. Will gets out of the way.

**Typography applied:**
- Task title in the expanded panel: Geist Sans 400, 14–15sp, `#F0F0F0`
- Action labels: Geist Sans 400, 13sp, in their semantic colors
- Source app name: Geist Mono 400, 11sp, `#888888`

**Color usage:**
The overlay is the one surface where all three semantic colors appear simultaneously (Done = `#4CAF74`, Later = `#888888`, Delete = `#E05252`). This is appropriate — these are three categorically different actions with real consequences. Color disambiguates them without requiring icons.

**Voice:**
Action labels are one word each: "Done." "Later." "Delete." No confirmation copy on tap — Will assumes the user means what they tapped.

---

## 5. Play Store listing

The commercial surface. This is where users who are not developers first encounter Will — the listing description, screenshots, and feature graphic.

**Visual direction:**
Feature graphic: wordmark on `#000000`, optionally with the primary tagline in Geist Sans below. No photography. No illustration. If a screenshot is composited into the feature graphic, it is the task feed at a representative state, set against black with no device frame.

Screenshots: five states, tight crop, true black, no device mockups. The sequence should tell a story in order: empty home screen → notification arrives → task appears in feed → overlay activates → task marked done.

**Typography applied:**
Feature graphic: wordmark using the selected logo direction. Tagline if used: Geist Sans 400, secondary text color `#888888` or near-white.

**Color usage:**
Feature graphic: two-tone only — `#F0F0F0` mark, `#000000` background. The accent does not appear in the feature graphic — it is not interactive.

**Voice notes for description copy:**

Short description (80 chars): "The home screen that gets you off your phone."

Long description opening: "Will is an Android launcher. When a notification arrives, Will converts it into a task. Tap it to execute. Mark it done. Your feed stays clean."

What the description never says: "boost your productivity," "take control of your digital life," "designed to help you focus," "wellness-aware," "mindful." None of that. The description sounds like a man-page entry written by someone who cares about craft.
