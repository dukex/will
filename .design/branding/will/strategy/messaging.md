# Messaging
> Brand: Will | Phase: strategy | Date: 2026-04-15

## Core Message

Your home screen is built to keep you on your phone. Will is built to get you off it.

**Specificity test:** Apply this to Niagara. Niagara reduces visual noise — it does not have a structural position against engagement mechanics, and it does not claim one. Apply it to Minimalist Phone. Minimalist Phone speaks addiction and recovery. Will speaks architecture and tools. The core message cannot be swapped.

---

## Supporting Messages

### 1. Structural differentiation: notifications are task objects

Every other launcher treats notifications as a layer on top of the home screen — something to manage, reduce, or ignore. Will treats them as the primary input: a notification arrives, Will converts it into a task with the source app's intent attached. When Lucas taps the task, Will fires the PendingIntent. The notification is gone. The action is done.

**This is not a feature. It is a domain model.** Notifications are first-class entities in Will's architecture. They are not displayed differently — they are represented differently, at the data layer.

Proof points:
- `notification/NotificationProcessor.kt`: pure domain logic mapping notification data to Task objects — no Android UI imports
- `task/Task.kt`: domain model with zero Android/Room dependencies — a notification becomes a Task at the boundary, not at display time
- `notification/NotificationActionStore.kt`: PendingIntents stored in memory, keyed by notification key, fired on tap — the action travels with the task, not with the notification tray
- `notification/NotificationKeyTracker.kt`: when the source notification is dismissed elsewhere (you acted on it on another device, or the app cleared it), Will removes the task — the feed reflects reality

**Implication for Lucas:** The Slack thread he needs to respond to is a task in his feed. Tapping it opens Slack directly to that thread. Not to Slack's home screen — to the thread. He responds, comes back to Will, marks it done. Total phone time: under 60 seconds. The phone did not generate a reason to stay.

---

### 2. Open source as trust primitive

"We have no dark patterns" is a claim. In a closed-source product, it is unverifiable. Lucas knows this.

Will's architecture is public. The notification listener is readable. The task repository is readable. There is no analytics library phoning home. There is no A/B testing framework running experiments on tap behavior. Lucas can verify this in an afternoon.

Proof points:
- Full source on GitHub under open license
- No proprietary backend — all data stays on-device in Room
- No analytics, no telemetry, no crash reporting with PII
- F-Droid compatible (no Google Play Services dependency in core)

**Implication for Lucas:** He does not have to take Will's word for anything. The trust is structural, not rhetorical. This is the only launcher where "no dark patterns" is an architectural fact rather than a brand promise.

---

### 3. Anti-engagement architecture

Will's home screen has no surface engineered to maximize time-on-device. There is no feed that refreshes to show you new content. There are no badges that escalate. There are no streaks that make leaving feel like failure. The home screen shows the tasks that are pending. When there are no pending tasks, it shows nothing.

This is not restraint. It is a different objective function. Competitor home screens optimize for engagement. Will optimizes for task completion. These are not the same direction.

Proof points:
- No streak system — not deferred, not planned, not optional
- No usage statistics presented as motivational dashboards
- Notifications are removed from the feed when completed or when the source dismisses them — the feed does not grow indefinitely
- App grid is a secondary surface (swipe-up), not the default — the phone's most common gesture does not take Lucas to a grid of apps competing for taps

**Implication for Lucas:** He can pick up the phone, see what needs to be done, do it, and put it down. The phone has no mechanism to generate the next reason to stay. That is the entire design.

---

## Elevator Pitch

**30-second version (GitHub README / Play Store listing):**

Will replaces the Android home screen with a task feed. Every notification becomes an actionable task — tap it to execute the source app's intent directly, without navigating to the notification tray or finding the app. When the task is done, it's gone.

No widgets. No app grid as default. No streaks, badges, or engagement mechanics. Open source.

The home screen is designed to keep you on your phone. Will is designed to get you off it.

---

## Tagline Directions

### Option A — Functional
**"Your notifications. Your tasks. Nothing else."**

Strategy: functional/declarative. States what Will does and what it does not do in one sentence. The "nothing else" carries the position — no feeds, no decoration, no engagement mechanics. Works well in UI contexts, README header, App Store listing. Closest to product description as tagline.

Rationale: Lucas evaluates tools on what they do and what they don't do. This answers both questions in nine words.

---

### Option B — Declarative
**"The workbench. Not the storefront."**

Strategy: positional/declarative. Contrasts Will's model (workbench: a place you work and leave) with the competitor model (storefront: a place designed to keep you browsing). Does not name competitors. References the original positioning brief framing directly. Works as a manifesto line, conference talk title, about page header.

Rationale: Speaks to how Lucas thinks about the phone — as an environment with a purpose, not a surface. Forces a mental model shift in the reader without explaining it.

---

### Option C — Provocative
**"Pick it up. Do the thing. Put it down."**

Strategy: behavioral/provocative. The three-step sequence is the entire value proposition. It has rhythm. It is also an implicit critique — if your current phone requires more than this, something is wrong with your phone. Usable as a manifesto opening, onboarding copy, or social media bio line.

Rationale: Lucas's aspiration from BRIEF.md is stated almost verbatim here. When he reads it, he already agrees. That is when a tagline works — it names something the reader already believes, which creates immediate alignment rather than persuasion.

---

## Audience Mapping: Lucas

**Who:** Developer / product designer, 25–35. Has tried Notion, Todoist, multiple launchers. Understands engagement mechanics as intentional design. Discovers tools through GitHub, indie hacker communities, developer Twitter/X.

**Tone shift:** No shift needed — Will's default voice was designed for Lucas. Peer-to-peer, technical, dry. He is not an edge case; he is the center.

**Emphasis:** Lead with architecture. Lucas's first question is not "does this look good" — it is "how does this work." The domain model (notification → Task, PendingIntent storage, key tracking) is the hook. Show it in the README before the screenshots.

**Trust signals in order of weight for Lucas:**
1. Open source with readable code (architecture is auditable)
2. No dark patterns in the codebase (verifiable, not claimed)
3. Builder-to-builder tone in docs and README (signals shared values)
4. Active commits and honest changelogs (signals the project is alive and honest)
5. Community on GitHub (signals others in his cohort have validated it)

**What Lucas does not need:**
- Testimonials from "productivity gurus"
- Usage statistics or focus score dashboards
- A landing page with stock photography of a focused person
- Email capture with a lead magnet ("10 tips for digital minimalism")

**Channel implications:**
- **GitHub README:** Primary discovery and conviction surface. Architecture-first, code snippets, honest caveats, dry voice.
- **F-Droid listing:** Shorter. Lead with "no tracking, open source, on-device only." Technical trust signals.
- **Play Store listing (if published):** Most constrained. Lead with the 30-second elevator pitch. No wellness language. Trust signal: "Open source — read the code."
- **Twitter/X / developer communities:** Dry observations about the attention economy + architecture decisions. No growth-hacking, no engagement bait. Ship something, explain what it does and why, link the commit.
