package tech.benedicta.will.task

/**
 * Turns state changes into [TaskEvent] rows.
 *
 * Call sites stay one line long and the one decision that needs thought lives
 * here: activating a task is [TaskEventType.STARTED] the first time and
 * [TaskEventType.RESUMED] afterwards, which is the same rule
 * `TaskRepository.setActiveTask` applies when it leaves `startedAt` alone on a
 * resume.
 */
class TaskEventRecorder(
    private val events: TaskEventRepository,
    private val now: () -> Long = System::currentTimeMillis
) {
    suspend fun created(taskId: Long) = append(taskId, TaskEventType.CREATED)

    /**
     * The task started at a moment the caller already decided, which is the
     * case for the accessibility pipeline: the coordinator stamps `startedAt`
     * and the log has to agree with it.
     */
    suspend fun started(taskId: Long, at: Long) {
        events.append(TaskEvent(taskId = taskId, type = TaskEventType.STARTED, at = at))
    }

    /** [task] must be the row as it was *before* activation. */
    suspend fun activated(task: Task) = append(
        task.id,
        if (task.startedAt == null) TaskEventType.STARTED else TaskEventType.RESUMED
    )

    suspend fun paused(taskId: Long) = append(taskId, TaskEventType.PAUSED)

    suspend fun switchedAway(taskId: Long, foregroundPackage: String) =
        append(taskId, TaskEventType.SWITCHED_AWAY, foregroundPackage)

    suspend fun completed(taskId: Long) = append(taskId, TaskEventType.COMPLETED)

    suspend fun reopened(taskId: Long) = append(taskId, TaskEventType.REOPENED)

    /** Drops the task's history. See [TaskEventType] for why no tombstone. */
    suspend fun forgot(taskId: Long) = events.deleteEventsForTask(taskId)

    suspend fun forgotEverything() = events.deleteAllEvents()

    private suspend fun append(
        taskId: Long,
        type: TaskEventType,
        foregroundPackage: String? = null
    ) {
        events.append(
            TaskEvent(
                taskId = taskId,
                type = type,
                at = now(),
                foregroundPackage = foregroundPackage
            )
        )
    }
}
