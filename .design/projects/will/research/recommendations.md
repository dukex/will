# Recommendations — Adopt / Adapt / Avoid
> Phase: research | Project: Will | Generated: 2026-04-16

---

## Adopt

### `SwipeToDismissBox` with 40% positional threshold
The Material3 `SwipeToDismissBox` is the correct component. The 40% width threshold is well-calibrated — below 33% feels accidental, above 50% feels laborious. Right swipe = Done (keep in list, call `markDone`), left swipe = Delete (dismiss from list, call `deleteTask`). Implement background color via `lerp()` with `state.progress`.

See: [reference-specs.md — SwipeToDismissBox API](./reference-specs.md)

### Regex pipeline for MVP NLP
ML Kit EntityExtraction adds 5.6MB download, is in beta, and does not cover Will's primary intent categories (respond, pay, open, call). A hand-crafted regex pipeline over the transcript is faster to build, fully deterministic, and zero-cost at runtime. Precision improves per intent added. ML Kit can be layered later for date/money entity enrichment.

See: [technical-research.md — ML Kit vs Regex](./technical-research.md)

### Custom Canvas bar chart (no library)
For a single bar chart showing time-on-task, there is no reason to add a charting library dependency. The `Canvas` composable with `drawRect()` for bars and `nativeCanvas.drawText()` for labels is sufficient and takes under 50 lines. Use `drawWithCache` to avoid per-frame object allocation. Libraries add 200KB–500KB and introduce their own theming systems that conflict with Will's OLED black aesthetic.

See: [technical-research.md — Canvas Drawing](./technical-research.md)

### `collectAsStateWithLifecycle()` for all Room-backed StateFlows
Always use `collectAsStateWithLifecycle()` (not `collectAsState()`) in Compose for Room-backed state. This stops collection when the composable is in the background, which is especially important for a launcher that is always the background app. The `SharingStarted.WhileSubscribed(5000)` in ViewModel keeps the upstream Flow hot across recompositions.

See: [technical-research.md — Room Flow Patterns](./technical-research.md)

### `mergeDescendants = true` + `customActions` on all TaskItems
Every swipe-dismissible row in the LazyColumn must have merged semantics and custom accessibility actions for Done and Delete. This is non-negotiable for TalkBack compatibility and is required to meet Android accessibility guidelines for interactive list items.

See: [accessibility-patterns.md — TalkBack Traversal](./accessibility-patterns.md)

---

## Adapt

### `ModalBottomSheet` with `skipPartiallyExpanded = false` for IntentInputSheet
The default Material3 bottom sheet snaps to full height. For IntentInputSheet, partial expansion gives a better first impression: the field appears halfway up the screen, keyboard opens and pushes it up. This requires `skipPartiallyExpanded = false`. Adapt the dismissal pattern: always use `sheetState.hide().invokeOnCompletion { ... }` — do not remove from composition synchronously.

Shape override required: `shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)` to match STYLE.md. The M3 default is 28dp radius — too decorative for Will.

See: [technical-research.md — ModalBottomSheet State Management](./technical-research.md)

### `AccessibilityService` event filtering: start narrow, expand as needed
Register only `TYPE_WINDOW_STATE_CHANGED` globally. When a task becomes active (user taps it), dynamically add the source package to the `packageNames` filter via `serviceInfo` update. This reduces battery impact significantly compared to monitoring all event types across all packages. The completion detection heuristic should start simple: check if the window title contains known completion strings. Refine per-app as patterns are discovered.

See: [technical-research.md — AccessibilityService Architecture](./technical-research.md)

### Niagara's gesture-first philosophy — adapted for a task context
Niagara reserves horizontal swipes for system notification access. Will's home screen has a task feed as primary content, so horizontal swipes at the item level (SwipeToDismissBox) are appropriate and do not conflict with OS gestures (which are vertical: swipe up = app drawer, swipe down = notification shade). Adopt Niagara's constraint: do not add horizontal swipes at the screen level — reserve them exclusively for task item actions.

See: [competitor-ux.md — Niagara Launcher](./competitor-ux.md)

---

## Avoid

### ML Kit EntityExtraction for the MVP sprint
Beta API, 5.6MB model download, poor coverage of Will's intent space. The download happens on first use — a new user would experience a visible delay. Avoid until the regex pipeline is proven insufficient for date and money entity resolution.

See: [technical-research.md — ML Kit vs Regex](./technical-research.md), [reference-specs.md — ML Kit](./reference-specs.md)

### Charting libraries (Vico, MPAndroidChart, any third party)
All charting libraries for Android are designed for light-background, Material-primary-color aesthetics. Overriding them to produce OLED-black, accent-only bars requires fighting the library's theming system. The effort exceeds writing a custom Canvas composable from scratch. For Will's single bar chart use case, libraries are over-engineered.

### `typeAllMask` in AccessibilityService configuration
Registering for all accessibility events on all packages will cause visible battery drain and may trigger Play Store review flags for excessive accessibility service scope. Register only the specific event types needed and filter to active task packages dynamically.

See: [technical-research.md — AccessibilityService Architecture](./technical-research.md)

### Wellness language and engagement mechanics in copy
Minimalist Phone's wellness framing (screen time guilt, app blockers) and typical productivity app mechanics (streaks, badges, congratulatory empty states) are both explicitly out of scope per STYLE.md. Any copy that congratulates, motivates, or gamifies should be rejected. The interface is a neutral execution layer.

See: [content-strategy.md — Voice Adaptation Principles](./content-strategy.md)

### Two-tap navigation to reach NotificationFilterSettings
Settings must be reachable in one action from the home screen. Any pattern requiring: home screen → some intermediate screen → settings is too deep for a launcher. The settings icon or entry point must be directly on the top bar or accessible via a single long-press gesture.

See: [ux-patterns.md — Settings Screen Patterns](./ux-patterns.md)

