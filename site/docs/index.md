# introduction

Will is an Android launcher that treats your phone as a work surface, not an attention sink.

<div class="android-frame">
  <img src="/screenshots/home.png" alt="Will home screen with pinned apps above a task feed of unread notifications" />
</div>

## what it does

- captures notifications and turns them into a task list
- minimal app grid, with up to 5 pinned apps that scroll alongside the task feed
- text input to capture intents the system did not generate
- visible checkbox to mark a task done — no hidden gestures
- floating overlay for active task management (done / later / delete)
- per-app filter to silence sources that should not become tasks
- focus dashboard with focus score, time-on-task, and context switches for the day
- no streaks, no badges, no engagement mechanics

## what it explicitly avoids

- gradients, shadows, rounded task cards
- wellness language (`calm`, `mindful`, `focus mode`)
- light mode (dark-only on MVP, true black for OLED)
- analytics, crash reporting, network calls — everything stays on device

## status

Will is under active development. the basic launcher, notification capture, pinned apps, manual text input, per-app filter, and the today-only focus dashboard are shipped. accessibility-driven lifecycle tracking, deeper privacy controls, and trend charts are pending.

see the [roadmap](/docs/roadmap) for a detailed breakdown.
