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
import tech.benedicta.will.infrastructure.SharedPrefsOnboardingRepository
import tech.benedicta.will.infrastructure.SharedPrefsPinnedAppsRepository
import tech.benedicta.will.infrastructure.room.RoomTaskEventRepository
import tech.benedicta.will.infrastructure.room.RoomTaskRepository
import tech.benedicta.will.infrastructure.room.WillDatabase
import tech.benedicta.will.launcher.PinnedAppsRepository
import tech.benedicta.will.lifecycle.TaskLifecycleApplier
import tech.benedicta.will.lifecycle.TaskLifecycleCoordinator
import tech.benedicta.will.notification.AppFilterRepository
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
    }
}
