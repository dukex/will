package tech.benedicta.will.lifecycle

import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskEventRecorder
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
 *
 * Every decision lands twice: on the task row, where it updates the current
 * state, and in the task log, where it keeps the timestamp and the package the
 * user jumped to.
 */
class TaskLifecycleApplier(
    private val coordinator: TaskLifecycleCoordinator,
    private val activeTaskProvider: () -> Task?,
    private val repository: TaskRepository,
    private val events: TaskEventRecorder
) {
    suspend fun onForegroundChange(packageName: String) {
        val decisions = coordinator.onForegroundChange(packageName, activeTaskProvider())
        decisions.forEach { event ->
            when (event) {
                is LifecycleEvent.StartTask -> {
                    repository.markStartedIfNeeded(event.taskId, event.startedAt)
                    events.started(event.taskId, event.startedAt)
                }
                is LifecycleEvent.IncrementContextSwitch -> {
                    repository.incrementContextSwitch(event.taskId)
                    events.switchedAway(event.taskId, packageName)
                }
            }
        }
    }

    fun resetForActiveTaskChange() {
        coordinator.reset()
    }
}
