# Reference Specs
> Phase: research | Project: Will | Generated: 2026-04-16

---

## Compose SwipeToDismissBox API

**URL:** https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss

**Key takeaway:** `rememberSwipeToDismissBoxState` accepts a `confirmValueChange` lambda and a `positionalThreshold`. The lambda receives the target `SwipeToDismissBoxValue` and returns a Boolean: `true` = animate dismiss, `false` = spring back. The `state.progress` Float drives background color interpolation during the drag.

**Will application:**
- `positionalThreshold = { d -> d * 0.40f }` for the 40% width threshold
- StartToEnd (right swipe) = Done: return `false` to keep item in list, call `markDone(task)` as side effect
- EndToStart (left swipe) = Delete: return `true` to animate dismiss, call `deleteTask(task)` as side effect
- Use `lerp(Color.Transparent, WillSuccess, state.progress)` in background for done direction
- Use `lerp(Color.Transparent, WillDestructive, state.progress)` in background for delete direction

**Reference code pattern:** [DEV Community swipe archive/delete patterns](https://dev.to/myougatheaxo/swipe-actions-in-compose-swipetodismissbox-archive-delete-patterns-2i9m)

---

## ModalBottomSheet API

**URL:** https://developer.android.com/develop/ui/compose/components/bottom-sheets

**Key takeaway:** Sheet visibility is controlled by a `Boolean` state variable, not by `sheetState` directly. Use `sheetState.hide().invokeOnCompletion { if (!sheetState.isVisible) showSheet = false }` to sync state after hide animation. `onDismissRequest` handles all other dismiss paths (scrim tap, drag down).

**Will application:**
- `skipPartiallyExpanded = false` for IntentInputSheet — starts half-expanded, expands with keyboard
- `TaskActionSheet` can use `skipPartiallyExpanded = true` — it shows a fixed list of actions, no need for partial state
- The sheet content uses `ColumnScope` — no need for an inner `Column`
- Corner radius is controlled by `shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)` matching the STYLE.md 8dp sheet radius

---

## SpeechRecognizer API

**URL:** https://developer.android.com/reference/android/speech/SpeechRecognizer

**Key takeaway:** `SpeechRecognizer.createSpeechRecognizer(context)` must be called on the main thread. The `RecognitionListener` callbacks fire on the main thread. Call `destroy()` when done to free resources. Requires `RECORD_AUDIO` permission at runtime.

**Key methods:**
- `startListening(Intent)` — begin recognition session
- `stopListening()` — stop capturing, process what was recorded
- `cancel()` — abort session, no result
- `destroy()` — release all resources

**Intent configuration:**
```kotlin
Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
}
```

**Will application:** ViewModel implements `RecognitionListener`, exposes `StateFlow<VoiceState>`. UI observes state and changes mic icon color. Voice result feeds into the NLP pipeline as text input, identical to typed input.

**Reference implementation:** [Voice to Text in Compose — Medium](https://medium.com/@daniel.atitienei/voice-to-text-in-jetpack-compose-android-c1e077627abe)

---

## AccessibilityService Implementation Guide

**URL:** https://developer.android.com/guide/topics/ui/accessibility/service

**Key takeaway:** The service requires `android:canRetrieveWindowContent="true"` to read view hierarchies. Event filtering via `android:accessibilityEventTypes` in the XML config and via `serviceInfo` in `onServiceConnected()`. The service cannot be programmatically enabled — the user must toggle it in system settings.

**Critical event types for Will:**
- `TYPE_WINDOW_STATE_CHANGED` — new screen/activity opened (detect context switch)
- `TYPE_VIEW_CLICKED` — user tapped something (potential completion signal)
- `TYPE_VIEW_TEXT_CHANGED` — text field changed (detect form completions)

**Package filtering strategy:** Start with `packageNames = null` (all packages) and filter in `onAccessibilityEvent` by checking against currently active task package names. This avoids needing to reconfigure the service when active tasks change.

**Will application:** Detect `startedAt` when the user opens an app associated with an active task. Detect `completedAt` via window title or visible text matching known completion patterns ("Sent", "Message delivered", "Payment confirmed").

**Supplementary guide:** [Unusual uses of AccessibilityService — Tooploox](https://tooploox.com/unusual-ways-using-android-accessibility-services)

---

## Material3 Switch Colors API

**URL:** https://developer.android.com/develop/ui/compose/components/switch

**Key takeaway:** `Switch` accepts a `colors` parameter using `SwitchDefaults.colors(...)`. Override `checkedThumbColor` and `checkedTrackColor` for Will's brand colors.

**Will application:**
```kotlin
Switch(
    checked = isEnabled,
    onCheckedChange = onToggle,
    colors = SwitchDefaults.colors(
        checkedThumbColor = WillTextPrimary,   // #F5F5F5
        checkedTrackColor = WillAccent,         // #4F6EF7
        uncheckedThumbColor = WillTextTertiary, // #525252
        uncheckedTrackColor = Color.Transparent
    )
)
```

The override matches STYLE.md target-adaptations spec exactly.

**Reference:** [Jetpack Compose Switch Colors — kotlinandroid.org](https://kotlinandroid.org/android-jetpack-compose-switch-colors/)

---

## Room + Flow Patterns

**URL:** https://developer.android.com/training/data-storage/room

**Key takeaway:** Return `Flow<List<Entity>>` from DAO. In ViewModel, `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())` converts to `StateFlow`. In Composable, use `collectAsStateWithLifecycle()` (not `collectAsState()`) to stop collection in background.

**New fields needed for US-04:**
```kotlin
// TaskEntity additions
val startedAt: Long? = null,
val completedAt: Long? = null,
val contextSwitchCount: Int = 0
```

Add a Room migration for these nullable columns: `ALTER TABLE tasks ADD COLUMN startedAt INTEGER` (SQL NULL maps to Kotlin `Long?`).

**Will application:** The existing `getAllTasks(): Flow<List<TaskEntity>>` in `TaskDao` already provides the reactive foundation. US-06 feed improvements only require ordering changes to the query.

**Reference:** [Room + Compose advanced patterns — Medium](https://medium.com/@androidlab/room-compose-advanced-patterns-for-local-data-ui-integration-809909b8ab5d)

---

## ML Kit On-Device Entity Extraction

**URL:** https://developers.google.com/ml-kit/language/entity-extraction/android

**Key takeaway:** `~5.6MB` model download on first use. Supported entities include date/time, money, phone, email, address, URL. The API is in beta — no SLA. `downloadModelIfNeeded()` must be called before `annotate()`. Requires API level 23+.

**Will application:** Not recommended for MVP. The 5.6MB download impacts cold start UX, the beta status introduces instability risk, and the supported entity types don't cover Will's primary intent patterns ("respond to X", "open Y app"). Use regex pipeline for MVP, add ML Kit as an enhancement layer in a later sprint if precision on date/money entities proves insufficient.

---

## Android 48dp Touch Target Specification

**URL:** https://m3.material.io/foundations/accessible-design/accessibility-basics

**Key takeaway:** All interactive elements must have a minimum touch target of 48×48dp. This is measured by the touchable area, not the visual size. Padding can be used to extend touch area without changing visual appearance.

**Will application:**
- `TaskItem`: `Modifier.heightIn(min = 48.dp)` on the row
- `HomeTopBar` actions: `Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { Text("Done") }`
- `TaskStatusIndicator` dot: not interactive, no touch target needed
- Filter toggle row in `NotificationFilterSettings`: full row must be clickable (propagate click from row to Switch), not just the Switch thumb

**Compose API default:** `IconButton` applies 48dp touch target automatically. Custom clickable elements do not.

---

## TalkBack Developer Guide

**URL:** https://developer.android.com/guide/topics/ui/accessibility/testing

**Key takeaway:** TalkBack traversal follows the Compose tree order. `mergeDescendants = true` in semantics collapses child nodes into a single announcement. `customActions` in semantics provides accessible alternatives for gesture-only interactions (like swipe-to-dismiss). `invisibleToUser()` hides decorative elements from the accessibility tree.

**Will application:**
- Every `TaskItem` row uses `Modifier.semantics(mergeDescendants = true)`
- `SwipeToDismissBox` items add `customActions` for Done and Delete
- `TaskStatusIndicator` uses `Modifier.semantics { invisibleToUser() }`
- `ModalBottomSheet` (IntentInputSheet): add `FocusRequester` to request focus on reopen

**Supplementary:** [TalkBack gestures 2024 — TripleTapTech](https://tripletaptech.org/talkback-gestures-for-android-in-2024/), [Crafting narratives with Compose semantics — droidcon](https://www.droidcon.com/2024/11/22/crafting-narratives-shaping-talkback-with-compose-semantics/)
