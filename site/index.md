---
layout: home

hero:
  name: "Will"
  tagline: "an Android launcher that treats your phone as a work surface, not an attention sink."
  actions:
    - theme: brand
      text: get started
      link: /docs/getting-started
    - theme: alt
      text: view on github
      link: https://github.com/dukex/will

features:
  - title: notifications become tasks
    details: every notification turns into an actionable card. tap it to open the exact conversation, not the app's home screen. messengers that spam the same chat collapse into one card with an unread counter.
  - title: no attention mechanics
    details: no streaks, no badges, no scores, no dopamine loops. the home screen does not reward you for opening it.
  - title: OLED-first
    details: true black at the root. on OLED panels every background pixel is off, which means zero power draw on the home screen.
  - title: pinned apps that share the scroll
    details: pin up to 5 apps. the row sits above the feed but scrolls with it, so messages reclaim the space when they pile up.
  - title: focus dashboard, no scoreboard
    details: focus score, time-on-task, and context switches for the day. the dashboard exists to inform, not to nudge.
  - title: stays on device
    details: no analytics, no crash reporting, no network calls. Will does not even request the INTERNET permission.
---

## see it

<div class="screenshots">
  <figure>
    <div class="android-frame">
      <img src="/screenshots/home.png" alt="task feed with pinned apps and notification-derived tasks" />
    </div>
    <figcaption>home — pinned apps and the task feed share one scroll</figcaption>
  </figure>
  <figure>
    <div class="android-frame">
      <img src="/screenshots/dashboard.png" alt="focus dashboard with score, time-on-task chart, and context switches" />
    </div>
    <figcaption>focus dashboard — informs, never nudges</figcaption>
  </figure>
  <figure>
    <div class="android-frame">
      <img src="/screenshots/drawer.png" alt="app drawer with search field" />
    </div>
    <figcaption>drawer — type to filter, long-press to pin</figcaption>
  </figure>
  <figure>
    <div class="android-frame">
      <img src="/screenshots/overlay.png" alt="active task overlay over Google Calendar" />
    </div>
    <figcaption>active task overlay — done, later, or delete without leaving the app you opened</figcaption>
  </figure>
</div>

<style>
.screenshots {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 32px;
  margin: 32px 0 48px;
  justify-items: center;
}
.screenshots figure {
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.screenshots figcaption {
  font-family: var(--vp-font-family-mono);
  font-size: 12px;
  color: #888888;
  text-align: center;
  line-height: 1.45;
}
</style>

## why Will exists

most launchers compete for your attention. they want time on screen because their business depends on it. Will has no business model. it is a tool, the way a hammer is a tool — used when needed, ignored otherwise.

the home screen is not a destination. it is a surface where work surfaces, gets handled, and disappears.

## what makes it different

- **the feed is the home screen.** no app chrome, no tabs, no bottom navigation. the launcher gets out of the way.
- **notifications you already get are the source of truth.** Will does not ask you to type your day into yet another inbox. it reads what your phone already knows.
- **deep links survive restarts.** tapping a task opens the specific conversation, not the app's home screen — even after Android kills the listener service.
- **filters belong to you.** decide per app whether its notifications should become tasks. the rest stay where they are.

## design constraints

Will follows a published [design system](/docs/design-system). a few rules show up in every contribution:

- one accent color, used only on interactive elements
- 1dp borders define structure. shadows do not exist
- Geist Sans for what you read, Geist Mono for what the system generated
- sentence case everywhere. no exclamation marks, no emoji

## status

basic launcher, notification capture, conversation dedup, pinned apps, manual text input, per-app filter, and the today-only focus dashboard are shipped. voice input, accessibility-driven lifecycle tracking, deeper privacy controls, and trend charts are in flight. see the [roadmap](/docs/roadmap) for the full breakdown.
