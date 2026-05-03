package tech.benedicta.will.lifecycle

import tech.benedicta.will.task.Task

/**
 * Output of the [TaskLifecycleCoordinator]. Pure data — the host
 * (a ViewModel or coroutine running off the AccessibilityService)
 * applies these to the repository.
 */
sealed class LifecycleEvent {
    data class StartTask(val taskId: Long, val startedAt: Long) : LifecycleEvent()
    data class IncrementContextSwitch(val taskId: Long) : LifecycleEvent()
}

/**
 * Pure-Kotlin decision unit for US-04 lifecycle tracking. Given a sequence
 * of foreground-app changes (sourced from an AccessibilityService at
 * runtime, but injected here so we can test without Android) plus the
 * currently active [Task], emits the lifecycle events that should be
 * applied to the repository.
 *
 * Rules:
 * - Active task is null → no events.
 * - Foreground = active task's `sourcePackage` and the task has no
 *   `startedAt` → emit [LifecycleEvent.StartTask].
 * - Foreground transitions to anything other than the source package
 *   *and* the launcher itself, *and* the task has already been started
 *   (the user has engaged with it) → emit
 *   [LifecycleEvent.IncrementContextSwitch].
 *   - Starting the task before the first switch matters: jumping from
 *     the launcher straight to an unrelated app does not count, because
 *     the user never started the task.
 * - Duplicate consecutive events for the same foreground package → no events.
 * - Returning to the launcher is never a context switch.
 *
 * Stateful: keeps the last-seen foreground to detect transitions.
 * [reset] clears it (call when the active task changes or the host
 * disconnects).
 */
class TaskLifecycleCoordinator(
    private val launcherPackage: String,
    private val now: () -> Long = System::currentTimeMillis
) {
    private var lastForeground: String? = null

    fun onForegroundChange(
        newForeground: String,
        activeTask: Task?
    ): List<LifecycleEvent> {
        val previous = lastForeground
        lastForeground = newForeground

        if (activeTask == null) return emptyList()
        if (newForeground == previous) return emptyList()

        val sourcePackage = activeTask.sourcePackage
        val events = mutableListOf<LifecycleEvent>()

        if (newForeground == sourcePackage && activeTask.startedAt == null) {
            events += LifecycleEvent.StartTask(taskId = activeTask.id, startedAt = now())
        }

        val movedAwayFromTaskBoundary =
            newForeground != sourcePackage &&
            newForeground != launcherPackage &&
            activeTask.startedAt != null

        if (movedAwayFromTaskBoundary) {
            events += LifecycleEvent.IncrementContextSwitch(taskId = activeTask.id)
        }

        return events
    }

    fun reset() {
        lastForeground = null
    }
}
