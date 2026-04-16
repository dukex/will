# Technical Research — Compose Stack
> Phase: research | Project: Will | Generated: 2026-04-16

---

## SwipeToDismissBox

**API (Material3 Compose):**
```kotlin
@Composable
fun SwipeToDismissBox(
    state: SwipeToDismissBoxState,
    backgroundContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
)
```

**State creation:**
```kotlin
val state = rememberSwipeToDismissBoxState(
    confirmValueChange = { value ->
        when (value) {
            SwipeToDismissBoxValue.StartToEnd -> { onDone(task); false }  // keep in list
            SwipeToDismissBoxValue.EndToStart -> { onDelete(task); true } // remove
            SwipeToDismissBoxValue.Settled -> false
        }
    },
    positionalThreshold = { totalDistance -> totalDistance * 0.40f }
)
```

**Key behaviors:**
- `confirmValueChange` returning `false` for StartToEnd keeps the item in place after done-swipe (correct for Will: mark done but don't remove from feed immediately)
- `confirmValueChange` returning `true` for EndToStart triggers dismiss animation (item flies off screen)
- `state.progress` is a Float [0..1] — use it with `lerp()` in `backgroundContent` to transition background color as the user drags
- `Modifier.animateItem()` on each list item makes insertion/removal animate correctly in `LazyColumn`

**Will-specific note:** Do not use `enableDismissFromStartToEnd = false` — both directions are active. The background icon should be an icon-less colored surface for Will's text-only aesthetic.

Source: [Android SwipeToDismissBox docs](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss), [ProAndroidDev SwipeTo article](https://proandroiddev.com/swipeto-explore-different-implementations-in-jetpack-compose-8c6cd59bbc3c)

---

## ModalBottomSheet State Management

**Core pattern:**
```kotlin
var showSheet by remember { mutableStateOf(false) }
val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
val scope = rememberCoroutineScope()

if (showSheet) {
    ModalBottomSheet(
        onDismissRequest = { showSheet = false },
        sheetState = sheetState
    ) {
        // IntentInputSheet content
    }
}
```

**Hiding with animation:**
```kotlin
scope.launch { sheetState.hide() }.invokeOnCompletion {
    if (!sheetState.isVisible) showSheet = false
}
```

**Key behaviors:**
- `skipPartiallyExpanded = false` allows the sheet to start at half-height — desirable for IntentInputSheet so it starts compact and expands when keyboard opens
- Remove from composition (`showSheet = false`) only after hide animation completes via `invokeOnCompletion` — do not remove immediately or the animation breaks
- `onDismissRequest` fires on scrim tap and drag-down dismiss — always sync `showSheet` state here
- For multiple sheets (IntentInputSheet vs TaskActionSheet), use a sealed class enum to track which is shown rather than multiple booleans

Source: [Android Bottom Sheets docs](https://developer.android.com/develop/ui/compose/components/bottom-sheets), [ProAndroidDev bottom sheet guide](https://proandroiddev.com/bottom-sheet-in-jetpack-compose-d7e106422606)

---

## SpeechRecognizer Integration in Compose

**Architecture:** SpeechRecognizer is not a Compose-native API. It must live in a ViewModel or a helper class that exposes state to Compose via StateFlow. The ViewModel implements `RecognitionListener`.

**Permission requirement:** `android.permission.RECORD_AUDIO` — must request at runtime before any SpeechRecognizer call.

**ViewModel pattern:**
```kotlin
class IntentInputViewModel : ViewModel(), RecognitionListener {
    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState

    private var recognizer: SpeechRecognizer? = null

    fun startListening(context: Context) {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer?.setRecognitionListener(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        recognizer?.startListening(intent)
        _voiceState.value = VoiceState.Listening
    }

    override fun onResults(bundle: Bundle?) {
        val text = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        _voiceState.value = if (text != null) VoiceState.Result(text) else VoiceState.Idle
    }

    override fun onError(error: Int) {
        _voiceState.value = VoiceState.Error(error)
    }
}
```

**Key behavior notes:**
- `SpeechRecognizer.createSpeechRecognizer(context)` must be called on the main thread
- The `RecognitionListener` callbacks fire on the main thread — safe to update StateFlow directly
- Destroy the recognizer in `onCleared()` to avoid leaks
- Offline speech recognition works on supported devices but requires `EXTRA_PREFER_OFFLINE` extra and is not guaranteed on all minSdk 24 devices
- The mic `IconButton` color should be `WillAccent` when `voiceState == Listening`, `WillTextTertiary` otherwise

Source: [Adding speech recognition to Compose — Medium](https://christopherward.medium.com/adding-speech-recognition-to-your-jetpack-compose-android-app-the-minimum-effort-guide-b7e3446a2482), [Voice to Text Compose — Medium](https://medium.com/@daniel.atitienei/voice-to-text-in-jetpack-compose-android-c1e077627abe), [GeeksForGeeks STT guide](https://www.geeksforgeeks.org/kotlin/speech-to-text-application-in-android-using-jetpack-compose/)

---

## AccessibilityService Architecture for Task Lifecycle

**Setup requirements:**
- `android.permission.BIND_ACCESSIBILITY_SERVICE` in manifest
- `android:canRetrieveWindowContent="true"` in service config XML
- User must manually enable in system Accessibility settings — cannot be granted programmatically

**Config XML key settings:**
```xml
<accessibility-service
    android:accessibilityEventTypes="typeWindowStateChanged|typeViewTextChanged|typeViewClicked"
    android:canRetrieveWindowContent="true"
    android:notificationTimeout="100" />
```

**Completion detection architecture:**
The service receives `onAccessibilityEvent(event)` for every registered event type across all apps. The signal-to-noise ratio is very high — most events are irrelevant. The correct approach:

1. Filter by `event.packageName` — only process events from apps with active Will tasks
2. On `TYPE_WINDOW_STATE_CHANGED`, check if the new window title matches a known completion screen (e.g., "Message sent", "Payment successful")
3. On `TYPE_VIEW_TEXT_CHANGED` or `TYPE_VIEW_CLICKED`, search the `AccessibilityNodeInfo` tree for text patterns indicating completion

**Node tree search:**
```kotlin
fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
    val nodes = root.findAccessibilityNodeInfosByText(text)
    return nodes.firstOrNull()
}
```

**Battery impact:** Registering for `typeAllMask` on all packages will drain battery significantly. Register only for `typeWindowStateChanged` broadly, and narrow to specific packages only when a task for that package is active. Unregister unused packages via `serviceInfo` update.

**Android 11+ restriction:** `TYPE_WINDOWS_CHANGED` requires `FLAG_RETRIEVE_INTERACTIVE_WINDOWS` in service flags. Some completion signals may not be accessible on apps that use `FLAG_SECURE` windows.

Source: [Android AccessibilityService developer guide](https://developer.android.com/guide/topics/ui/accessibility/service), [Track browser usage — Medium](https://medium.com/nerd-for-tech/track-web-browser-usage-in-android-using-accessibility-service-800bfa2745d2)

---

## Canvas Drawing for FocusDashboard Bar Chart

**Core approach:** Custom Canvas composable, no library. Use `drawRect()` for bars, `drawLine()` for axis, `nativeCanvas.drawText()` for labels.

**Performance pattern:** Use `drawWithCache` when the data doesn't change on every frame:
```kotlin
Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
    val barWidth = size.width / tasks.size
    tasks.forEachIndexed { index, task ->
        val barHeight = (task.durationMs / maxDuration.toFloat()) * size.height
        drawRect(
            color = WillAccent,
            topLeft = Offset(index * barWidth + 2.dp.toPx(), size.height - barHeight),
            size = Size(barWidth - 4.dp.toPx(), barHeight)
        )
    }
}
```

**Key restrictions:**
- Do not create `Paint` or `Path` objects inside `drawWithCache` lambda on every recomposition — hoist them to `remember` or use the cached drawing approach
- `nativeCanvas.drawText()` requires a `android.graphics.Paint` (not Compose Paint) for text rendering — create it once with `remember`
- For Will's OLED aesthetic: bar color is `WillAccent` (#4F6EF7), bar background is `#000000`, axis labels use `WillTextTertiary`

Source: [Bar Chart with Compose Canvas — decodeandroid.in](https://decodeandroid.in/bar-chart-jetpack-compose-canvas-custom-android-chart/), [ProAndroidDev custom charts](https://proandroiddev.com/building-your-first-custom-chart-in-android-with-jetpack-compose-a890fb60878b), [DEV Community Canvas charts](https://dev.to/myougatheaxo/jetpack-compose-canvas-custom-drawing-charts-animations-4gib)

---

## Room Flow + StateFlow Reactive Patterns

**Canonical pattern:**
```kotlin
// DAO
@Query("SELECT * FROM tasks ORDER BY createdAt DESC")
fun getAllTasks(): Flow<List<TaskEntity>>

// ViewModel
val tasks: StateFlow<List<Task>> = repository.getAllTasks()
    .map { entities -> entities.map { it.toTask() } }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

// Composable
val tasks by viewModel.tasks.collectAsStateWithLifecycle()
```

**`SharingStarted.WhileSubscribed(5000)`** keeps the upstream Flow active for 5 seconds after the last subscriber disappears — prevents reloading data on screen rotation.

**`collectAsStateWithLifecycle()`** (from `androidx.lifecycle:lifecycle-runtime-compose`) stops collection when the composable is in the background — saves battery compared to `collectAsState()`.

**Will-specific note:** The existing `TaskViewModel` already uses this pattern. For US-03 (IntentInputSheet creating tasks), the new task will appear in the feed reactively without any additional wiring — Room emits a new Flow value on insert.

Source: [Compose UI state with Flow — valueof.io](https://www.valueof.io/blog/compose-ui-state-flow-offline-first-repository), [StateFlow vs MutableState — ProAndroidDev](https://proandroiddev.com/bridging-the-gap-between-jetpack-compose-state-and-stateflow-for-reactive-ui-163efeb1d0d9)

---

## ML Kit Entity Extraction vs Regex NLP

**ML Kit Entity Extraction:**
- SDK: `com.google.mlkit:entity-extraction`
- Model download: ~5.6MB, on-demand, stored on device after first download
- Supported entity types: address, date/time, email, flight number, IBAN, ISBN, money, payment card, phone, tracking number, URL
- API level minimum: 23 (Will's minSdk is 24 — compatible)
- Status: **beta** — not subject to SLA or deprecation policy, may break

**Regex / rule-based NLP:**
- Zero download size, zero latency, fully deterministic
- Must be hand-crafted for each intent type (phone numbers, app names, contacts)
- Handles Will-specific patterns that ML Kit doesn't: "respond to [name]", "pay [amount] for [app]"
- No machine learning — won't handle paraphrasing or natural variation

**Decision point for Will MVP:**
ML Kit handles dates, money, and phone numbers well out of the box, but Will's primary task intents ("message João", "pay the bill") are outside the 11 supported entity types. ML Kit entity extraction doesn't do intent classification — it annotates entities, not actions.

For MVP: regex pipeline for intent classification, ML Kit for entity enrichment (dates, amounts) if needed. The regex pipeline is faster to build and produces predictable, debuggable results. ML Kit can be layered in later for richer entity resolution.

Source: [ML Kit Entity Extraction Android](https://developers.google.com/ml-kit/language/entity-extraction/android), [ProAndroidDev ML Kit entity extraction](https://proandroiddev.com/entity-extraction-using-googles-ml-kit-on-android-71e3b9ec2b44)
