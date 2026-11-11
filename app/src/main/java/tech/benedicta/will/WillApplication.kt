package tech.benedicta.will

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.benedicta.will.infrastructure.AndroidPermissionInspector
import tech.benedicta.will.infrastructure.SharedPrefsAppFilterRepository
import tech.benedicta.will.infrastructure.SharedPrefsDerivedIntentionPreference
import tech.benedicta.will.infrastructure.SharedPrefsLearnedNoiseRepository
import tech.benedicta.will.infrastructure.SharedPrefsModelReviewWatermark
import tech.benedicta.will.infrastructure.SharedPrefsModelTriageLog
import tech.benedicta.will.infrastructure.SharedPrefsModelTriagePreference
import tech.benedicta.will.infrastructure.SharedPrefsOnboardingRepository
import tech.benedicta.will.infrastructure.SharedPrefsPinnedAppsRepository
import tech.benedicta.will.infrastructure.room.RoomTaskEventRepository
import tech.benedicta.will.infrastructure.room.RoomTaskRepository
import tech.benedicta.will.infrastructure.room.WillDatabase
import android.os.Build
import tech.benedicta.will.infrastructure.SharedPrefsSemanticSearchPreference
import tech.benedicta.will.launcher.ActiveTaskShortcutPublisher
import tech.benedicta.will.launcher.AppMatcher
import tech.benedicta.will.launcher.GeminiNanoAppSearch
import tech.benedicta.will.launcher.SearchWithMeaning
import tech.benedicta.will.launcher.SemanticAppSearch
import tech.benedicta.will.launcher.SemanticSearchModel
import tech.benedicta.will.launcher.SemanticSearchPreference
import tech.benedicta.will.launcher.PinnedAppsRepository
import tech.benedicta.will.lifecycle.TaskLifecycleApplier
import tech.benedicta.will.lifecycle.TaskLifecycleCoordinator
import tech.benedicta.will.notification.AppFilterRepository
import tech.benedicta.will.notification.GeminiNanoIntentions
import tech.benedicta.will.notification.GeminiNanoTriage
import tech.benedicta.will.notification.IntentionKind
import tech.benedicta.will.notification.ModelReviewPass
import tech.benedicta.will.notification.ModelTriageLog
import tech.benedicta.will.notification.SemanticNoiseReader
import tech.benedicta.will.notification.IntentionExtractor
import tech.benedicta.will.notification.ReadIntentionWithMeaning
import tech.benedicta.will.notification.SemanticIntentionReader
import tech.benedicta.will.notification.IntentionClassifier
import tech.benedicta.will.notification.LearnedIntentionClassifier
import tech.benedicta.will.notification.LearnedNoiseRepository
import tech.benedicta.will.notification.PatternIntentionClassifier
import tech.benedicta.will.onboarding.OnboardingRepository
import tech.benedicta.will.onboarding.PermissionInspector
import tech.benedicta.will.share.SharedTask
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskEventRecorder
import tech.benedicta.will.task.TaskEventRepository
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.task.TaskRepository
import tech.benedicta.will.task.isActive

data class OverlayEvent(val action: String, val taskId: Long)

class WillApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val taskRepository: TaskRepository by lazy {
        RoomTaskRepository(WillDatabase.getInstance(this).taskDao())
    }
    val taskEventRepository: TaskEventRepository by lazy {
        RoomTaskEventRepository(WillDatabase.getInstance(this).taskEventDao())
    }
    val taskEventRecorder: TaskEventRecorder by lazy {
        TaskEventRecorder(taskEventRepository)
    }
    val semanticSearchPreference: SharedPrefsSemanticSearchPreference by lazy {
        SharedPrefsSemanticSearchPreference(this)
    }

    /**
     * Exact matching first, meaning only when that finds nothing, and only
     * where the device can serve a model and the user asked for it.
     */
    private val geminiNano: GeminiNanoAppSearch? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            GeminiNanoAppSearch(enabled = { semanticSearchPreference.enabledNow() })
        } else {
            null
        }
    }

    /** What settings asks about the model's presence. */
    val semanticSearchModel: SemanticSearchModel
        get() = geminiNano ?: SemanticSearchModel.Unsupported

    val appSearch: SearchWithMeaning by lazy {
        SearchWithMeaning(AppMatcher(), geminiNano ?: SemanticAppSearch.Unavailable)
    }

    val derivedIntentionPreference: SharedPrefsDerivedIntentionPreference by lazy {
        SharedPrefsDerivedIntentionPreference(this)
    }

    /**
     * The notification path's reader: the system's own category and nothing
     * else. The model cannot be reached from a background service at all, so
     * it is deliberately absent here and runs in [modelReviewPass] instead,
     * from the foreground.
     */
    val intentionReader: ReadIntentionWithMeaning by lazy {
        ReadIntentionWithMeaning(IntentionExtractor(), SemanticIntentionReader.Unavailable)
    }

    val learnedNoiseRepository: LearnedNoiseRepository by lazy {
        SharedPrefsLearnedNoiseRepository(this)
    }
    val modelTriageLog: ModelTriageLog by lazy { SharedPrefsModelTriageLog(this) }

    val modelTriagePreference: SharedPrefsModelTriagePreference by lazy {
        SharedPrefsModelTriagePreference(this)
    }

    /**
     * The patterns, then what the user taught Will. Both are free and need no
     * model, which is what lets them run as the notification arrives. The
     * model's share of triage happens in [modelReviewPass].
     */
    val intentionClassifier: IntentionClassifier by lazy {
        LearnedIntentionClassifier(PatternIntentionClassifier(), learnedNoiseRepository)
    }

    /**
     * Everything the model does, run from the foreground because that is the
     * only place ML Kit's GenAI APIs work: a call from a background service
     * comes back `BACKGROUND_USE_BLOCKED`.
     */
    val modelReviewPass: ModelReviewPass by lazy {
        val onThisPhone = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ModelReviewPass(
            tasks = taskRepository,
            triage = if (onThisPhone) GeminiNanoTriage() else SemanticNoiseReader.Unavailable,
            reader = if (onThisPhone) {
                GeminiNanoIntentions(enabled = { true })
            } else {
                SemanticIntentionReader.Unavailable
            },
            log = modelTriageLog,
            phrase = { kind, subject ->
                when (kind) {
                    IntentionKind.REPLY -> getString(R.string.derived_reply_to, subject)
                    IntentionKind.CALL_BACK -> getString(R.string.derived_call_back, subject)
                    IntentionKind.CONFIRM -> getString(R.string.derived_confirm, subject)
                    IntentionKind.PAY -> getString(R.string.derived_pay, subject)
                    IntentionKind.ATTEND -> getString(R.string.derived_attend, subject)
                }
            },
            triageEnabled = { modelTriagePreference.enabledNow() },
            readingEnabled = { derivedIntentionPreference.enabledNow() }
        )
    }

    /** Where the last review got to, so a pass does not redo the whole feed. */
    val modelReviewWatermark: SharedPrefsModelReviewWatermark by lazy {
        SharedPrefsModelReviewWatermark(this)
    }
    val appFilterRepository: AppFilterRepository by lazy {
        SharedPrefsAppFilterRepository(this)
    }
    val pinnedAppsRepository: PinnedAppsRepository by lazy {
        SharedPrefsPinnedAppsRepository(this)
    }
    val onboardingRepository: OnboardingRepository by lazy {
        SharedPrefsOnboardingRepository(this)
    }
    val permissionInspector: PermissionInspector by lazy {
        AndroidPermissionInspector(this)
    }

    val activeTaskFlow: StateFlow<Task?> by lazy {
        taskRepository.getAllTasks()
            .map { list -> list.firstOrNull { it.isActive } }
            .stateIn(applicationScope, SharingStarted.Eagerly, null)
    }

    val taskLifecycleApplier: TaskLifecycleApplier by lazy {
        TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = packageName),
            activeTaskProvider = { activeTaskFlow.value },
            repository = taskRepository,
            events = taskEventRecorder
        )
    }

    private val activeTaskShortcuts: ActiveTaskShortcutPublisher by lazy {
        ActiveTaskShortcutPublisher(this)
    }

    private val _overlayEvents = MutableSharedFlow<OverlayEvent>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val overlayEvents = _overlayEvents.asSharedFlow()

    fun emitOverlayEvent(event: OverlayEvent) {
        _overlayEvents.tryEmit(event)
    }

    /**
     * Stores a task an outside caller asked for. Runs on the application scope
     * because the caller is usually a no-UI activity that finishes
     * immediately, which would cancel a scope tied to its lifecycle.
     */
    fun acceptTaskFromOutside(
        shared: SharedTask,
        origin: TaskOrigin,
        sourcePackage: String
    ) {
        applicationScope.launch {
            val id = taskRepository.insertTask(
                Task(
                    title = shared.title,
                    text = shared.text,
                    sourcePackage = sourcePackage,
                    createdAt = System.currentTimeMillis(),
                    origin = origin
                )
            )
            taskEventRecorder.created(id)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Reset the lifecycle coordinator whenever the active task id flips so
        // the previous task's "last seen foreground" memory does not leak into
        // measurements for the new task.
        applicationScope.launch {
            activeTaskFlow
                .map { it?.id }
                .distinctUntilChanged()
                .collect { taskLifecycleApplier.resetForActiveTaskChange() }
        }
        // What the user is working on is reachable from outside Will, through
        // the shortcut a long press on the icon shows.
        applicationScope.launch {
            // StateFlow already conflates, so no distinctUntilChanged here.
            activeTaskFlow.collect { activeTaskShortcuts.publish(it) }
        }
    }
}
