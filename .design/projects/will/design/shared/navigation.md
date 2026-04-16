# Navigation
> Phase: design | Project: Will | Generated: 2026-04-16

---

## Pattern: Gesture-first launcher navigation

Will is a launcher — there is no TabRow, no BottomNavigation, no TopAppBar in the Material3 sense. Navigation is gestural and contextual:

| Gesture / Action | Destination | Notes |
|------------------|-------------|-------|
| Default / home button | HomeScreen | singleTask, clearTaskOnLaunch |
| Swipe up from HomeScreen | AppGrid | nestedScroll onPostScroll intercept |
| Swipe down to dismiss AppGrid | HomeScreen | |
| Tap filter icon (HomeTopBar) | NotificationFilterSettings | `startActivity` with overlay-style transition |
| Tap chart icon (HomeTopBar) | FocusDashboard | `startActivity` |
| Tap "+" or bottom edge pull | IntentInputSheet (bottom sheet) | No screen navigation — modal overlay |
| Long-press TaskItem | TaskActionSheet (bottom sheet) | No screen navigation — modal overlay |
| Tap "data & privacy" (FilterSettings) | PrivacySettings | `startActivity` |
| Back gesture | Previous screen or HomeScreen | System back; no custom back behavior |

---

## HomeTopBar layout

```
[ date/time (left) ]   [ filter icon | chart icon ] (right)
```

Both icons: 48dp touch target via `IconButton`. `WillTextSecondary` tint at rest, `WillTextPrimary` when on the destination screen (active indicator).

No hamburger menu. No overflow menu. Direct access only.

---

## AppGrid access

Swipe up from anywhere on HomeScreen. The `nestedScroll` intercepts remaining scroll after `LazyColumn` has consumed its share (already implemented). AppGrid has a `SearchField` at top for filtering. Swipe down or tap outside search to return.

---

## Bottom sheet access

IntentInputSheet: tap the "+ add task" action or a floating add button (TBD in screen design). The sheet is a `ModalBottomSheet` — scrim covers HomeScreen but does not navigate away.

TaskActionSheet: long-press on any `TaskItem`. Same pattern.

Neither bottom sheet is a navigation destination — no back stack entry.

---

## Portrait only

`screenOrientation=portrait` is set. No landscape layout required. The navigation system has no landscape adaptations.

---

## Deep links / external navigation

When a task is tapped (not swiped): fires `PendingIntent` from `NotificationActionStore` to open the source app. If store is empty: launches the source package via `PackageManager`. This is not Will-internal navigation.
