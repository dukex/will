# roadmap

snapshot of what is shipped, what is in flight, and what is intentionally deferred. mirrors the canonical list in `docs/99-TASKS.md` inside the repo.

## shipped

- **PT-01 — project setup**: minSdk 24, Compose, Room, KSP
- **PT-02 — core architecture**: Task domain model, TaskDao, RoomTaskRepository, TaskViewModel
- **US-01 — basic launcher**: app grid with launch
- **US-02 — notification capture**: NotificationListenerService → Task → Room, tap fires the original `PendingIntent`, conversation dedup via Room unique index, unread counter
- **US-Pin-01 — pin up to 5 apps**: long-press to pin or unpin, pinned row scrolls with the feed
- **US-08 — notification filter**: `SettingsScreen` with per-app toggles backed by `SharedPreferences`
- **US-10 — first-run onboarding (core)**: guided three-step flow (default launcher, overlay, notification listener), lifecycle-aware auto-advance, persisted completion flag, re-run from settings. Instrumented end-to-end test deferred.
- **US-09 — brand-aligned Android icon set**: adaptive icon vectors, Android 13+ monochrome variant, density-specific raster mipmaps regenerated from a single ImageMagick render, true-black splash on Android 12+, wallpaper-legibility audit archived in the brand directory.

## in flight

- **US-03 — text input**: text input through `IntentInputSheet` is shipped. the NLP pipeline that turns free-form input into deep links / actions is pending.
- **US-04 — lifecycle tracking**: domain fields, manual completion, and `TaskStatusIndicator` are shipped. AccessibilityService for automatic detection is pending.
- **US-05 — focus dashboard**: today's focus score, time-on-task chart, and context switches are shipped. date-range filtering and trends are pending.
- **US-06 — reactive feed and deep linking**: StateFlow feed, lifecycle ordering, and deep linking are shipped. swipe actions, search, and deep-link feedback are pending.
- **US-07 — privacy controls**: `PrivacySettings`, delete-all, and permission rationale are shipped. selective deletion and explicit on-device guarantees for NLP / accessibility are still to enforce. see [privacy](/docs/privacy).

## explicitly deferred

- voice / speech-to-text input — out of scope; will not be built. Will is for typing-and-tapping users; speech recognition pulls in cloud-leaning APIs that conflict with the on-device privacy story.
- light mode — dark only on MVP
- recent-apps view — the API is restricted on Android 5+
- notification grouping by source — collapsing already happens by conversation, broader grouping has not earned its complexity yet
- analytics or telemetry of any kind — see [privacy](/docs/privacy)

## how this list stays honest

the [changelog](/docs/changelog) records every user-facing change as it ships. the source of truth for tasks lives at `docs/99-TASKS.md` in the repo and gets updated alongside every merged PR.
