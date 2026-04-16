# Accessibility Patterns — Android / TalkBack
> Phase: research | Project: Will | Generated: 2026-04-16

---

## Touch Target Requirements

Material Design mandates a minimum 48dp interactive area for any tappable element. This applies regardless of the visual size of the element — a 24dp icon inside a `Box` with 48dp min height satisfies the requirement.

**Compose implementation:**
```kotlin
Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp)
```

Material3 components (IconButton, Switch, Checkbox) apply 48dp touch targets internally. Custom components must apply this modifier manually.

**Will scope:** `TaskItem` must use `Modifier.heightIn(min = 48.dp)`. HomeTopBar inline actions (Done/Later/Delete) must each be wrapped in a `Box(Modifier.size(48.dp))` with click handling on the box, not just the label text.

Source: [Android Compose accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults), [Accessibility codelab](https://developer.android.com/codelabs/jetpack-compose-accessibility)

---

## TalkBack Traversal Order for Task Feed

TalkBack reads composables in the order they appear in the composition tree, left-to-right and top-to-bottom by default. For a `LazyColumn` of task items, this is correct and requires no intervention.

**Per-item merging:** Each `TaskItem` should be a merged semantics node so TalkBack reads it as a single unit rather than reading each text element separately:
```kotlin
Box(modifier = Modifier.semantics(mergeDescendants = true) { ... }) {
    // task title, source app, timestamp inside
}
```

Without `mergeDescendants = true`, TalkBack would read "Pay the bill", then focus again on "WhatsApp", then focus again on "2 minutes ago" as three separate elements. Merged, it reads all three in sequence as a single announcement.

**Custom actions for swipe:** Since TalkBack captures swipe gestures for its own navigation, `SwipeToDismissBox` is inaccessible to TalkBack users by default. Add custom actions to the semantics node:
```kotlin
Modifier.semantics {
    customActions = listOf(
        CustomAccessibilityAction("Mark done") { onDone(); true },
        CustomAccessibilityAction("Delete task") { onDelete(); true }
    )
}
```
TalkBack users access these via the local context menu (swipe up then right).

Source: [Android Compose accessibility codelab](https://developer.android.com/codelabs/jetpack-compose-accessibility), [ProAndroidDev accessibility article](https://proandroiddev.com/accessibility-in-jetpack-compose-e2f280810cd), [DEV Community best practices](https://dev.to/fannydemey/best-practice-to-build-accessible-apps-with-jetpack-compose-1l5n)

---

## Semantics for Interactive Elements

**Role assignment:** Any custom composable that behaves like a button must declare `Role.Button` in its semantics. Without this, TalkBack announces it as a generic container, and users do not know it is actionable.

```kotlin
Box(
    modifier = Modifier
        .semantics { role = Role.Button }
        .clickable(onClickLabel = "Mark task done") { onDone() }
)
```

`onClickLabel` provides the action description TalkBack speaks after the element label. Use it to distinguish between multiple tappable items with the same visual label.

**State description for TaskStatusIndicator:** The colored dot alone communicates nothing to TalkBack. Add a state description:
```kotlin
Modifier.semantics {
    stateDescription = when (task.status) {
        TaskStatus.Pending -> "pending"
        TaskStatus.Active -> "in progress"
        TaskStatus.Done -> "completed"
    }
}
```

**Switch semantics (NotificationFilterSettings):** Material3 `Switch` announces its checked state automatically. Ensure the parent row has a `Modifier.toggleable(...)` or that the `Switch` receives a non-empty label via its parent's semantics. The app name in the row must be the semantic label for the switch.

Source: [Android Compose accessibility — semantics modifier](https://developer.android.com/develop/ui/compose/accessibility/api-defaults), [Compose accessibility cheatsheet — Codexpedia](https://www.codexpedia.com/android/android-jetpack-compose-accessibility-cheatsheet/)

---

## Focus Management in ModalBottomSheet

When a `ModalBottomSheet` opens, TalkBack focus should move to the first interactive element inside the sheet. Material3's `ModalBottomSheet` does this automatically for the initial open. However, there is a known issue: if the same sheet is reopened after being closed, TalkBack may not move focus to the sheet's content — it retains focus on the element that was focused before the sheet opened.

**Workaround:** Request accessibility focus explicitly on a container inside the sheet using `FocusRequester`:
```kotlin
val focusRequester = remember { FocusRequester() }
LaunchedEffect(Unit) {
    focusRequester.requestFocus()
}
Box(modifier = Modifier.focusRequester(focusRequester).focusable()) {
    // first content element
}
```

**Scrim behavior:** The scrim behind `ModalBottomSheet` should not be traversable by TalkBack. Material3 handles this by setting `importantForAccessibility = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS` on the scrim layer. No manual intervention needed.

Source: [TalkBack focus issue — Flutter tracker](https://github.com/flutter/flutter/issues/55917) (pattern is OS-level, applies to Compose too), [kotlinlang Slack — requesting TalkBack focus](https://slack-chats.kotlinlang.org/t/10509998/is-there-a-way-to-request-talkback-focus-on-a-particular-ele)

---

## Content Descriptions for Dynamic Data

Timestamps and numeric data require human-readable descriptions, not raw values:

**Bad:** `contentDescription = "1713271200000"` (epoch ms)
**Good:** `contentDescription = "2 minutes ago"`

For timestamps, format relative to now using `DateUtils.getRelativeTimeSpanString()` and use that string as the content description.

**Task source app:** When the source app icon is shown (or its absence is implied), include the app name in the content description of the task row, not separately on the icon. Users don't need to know the icon exists — they need to know which app the task came from.

**Battery/progress indicators:** The `CircularProgressIndicator` used for focus score needs an explicit `contentDescription` since it communicates a numeric value visually. Example: `contentDescription = "Focus score: 74 out of 100"`.

Source: [Android accessibility developer guide](https://developer.android.com/guide/topics/ui/accessibility/service), [ProAndroidDev building accessible UIs](https://proandroiddev.com/building-accessible-android-uis-with-jetpack-compose-b59438fc6a03)

---

## AccessibilityNodeInfo for Custom Components

`TaskStatusIndicator` is a small colored dot — a custom drawn composable with no standard accessible representation. Without intervention, TalkBack ignores it entirely because it has no click handler and no text.

This is actually correct behavior: the indicator's meaning is conveyed by the task row's `stateDescription`. Do not add a separate focus stop for the indicator dot. It should not appear in the accessibility tree at all.

Mark it explicitly as invisible to accessibility:
```kotlin
Modifier.semantics { invisibleToUser() }
```

This prevents TalkBack from ever landing on it, which is the right outcome — the row-level semantics carry the state.

Source: [Accessibility in Jetpack Compose — droidcon](https://www.droidcon.com/2021/11/29/accessibility-in-jetpack-compose/)

---

## Testing Accessibility

The Accessibility Scanner app (Google) and Compose's built-in `AccessibilityChecks` can both validate touch targets and content descriptions automatically. For CI:

```kotlin
// In AndroidTest
AccessibilityChecks.enable()
```

This fails tests when elements don't meet touch target or contrast requirements. Add it to the test suite for all new screens.

Source: [Testing for Accessibility codelab](https://developer.android.com/codelabs/basic-android-kotlin-compose-test-accessibility)
