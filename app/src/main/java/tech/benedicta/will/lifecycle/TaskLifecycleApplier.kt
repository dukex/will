package tech.benedicta.will.lifecycle

import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskRepository

/**
 * Bridges a stream of foreground-app events from the host (the
 * `WillAccessibilityService`) into the [TaskLifecycleCoordinator] and
 * applies its decisions to the [TaskRepository].
 *
 * Stateless beyond the underlying coordinator. The host calls
 * [onForegroundChange] whenever a new package comes to the foreground,
 * and [resetForActiveTaskChange] whenever the active task switches so
 * the coordinator's last-seen-foreground memory does not bleed across
 * tasks.
 */
class TaskLifecycleApplier(
    private val coordinator: TaskLifecycleCoordinator,
    private val activeTaskProvider: () -> Task?,
    private val repository: TaskRepository
) {
    suspend fun onForegroundChange(packageName: String) {
        val events = coordinator.onForegroundChange(packageName, activeTaskProvider())
        events.forEach { event ->
            when (event) {
                is LifecycleEvent.StartTask ->
                    repository.markStartedIfNeeded(event.taskId, event.startedAt)
                is LifecycleEvent.IncrementContextSwitch ->
                    repository.incrementContextSwitch(event.taskId)
            }
        }
    }

    fun resetForActiveTaskChange() {
        coordinator.reset()
    }
}
