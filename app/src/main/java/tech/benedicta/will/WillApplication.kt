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
     * The category first, on every device, and the model only for a
     * notification whose category said nothing. With the switch off this is
     * exactly `IntentionExtractor` and nothing else.
     */
    val intentionReader: ReadIntentionWithMeaning by lazy {
        val model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            GeminiNanoIntentions(enabled = { derivedIntentionPreference.enabledNow() })
        } else {
            SemanticIntentionReader.Unavailable
        }
        ReadIntentionWithMeaning(IntentionExtractor(), model)
    }

    val learnedNoiseRepository: LearnedNoiseRepository by lazy {
        SharedPrefsLearnedNoiseRepository(this)
    }
    val intentionClassifier: IntentionClassifier by lazy {
        LearnedIntentionClassifier(PatternIntentionClassifier(), learnedNoiseRepository)
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
