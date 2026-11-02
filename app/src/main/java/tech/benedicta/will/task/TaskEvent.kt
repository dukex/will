package tech.benedicta.will.task

/**
 * What happened to a task.
 *
 * There is no `DELETED` type. Deleting a task removes its events with it, so a
 * tombstone would point at a row the feed and the dashboard can no longer
 * resolve, and US-07 promises the history goes when the user removes it.
 * Completing a task keeps the row, so [COMPLETED] stays meaningful.
 */
enum class TaskEventType {
    CREATED,
    STARTED,
    PAUSED,
    RESUMED,
    SWITCHED_AWAY,
    COMPLETED,
    REOPENED;

    companion object {
        /**
         * Reads a persisted type, or null when the stored value is not one of
         * these. The caller drops the row: in a behavioural log a mislabelled
         * event is worse than a missing one.
         */
        fun fromStored(stored: String?): TaskEventType? =
            entries.firstOrNull { it.name == stored }
    }
}

/**
 * One entry in the append-only task log. Pure domain model, no Android or Room
 * dependency.
 *
 * The scalar lifecycle fields on [Task] (`startedAt`, `pausedAt`,
 * `completedAt`, `contextSwitchCount`) are a materialised view of this log:
 * they answer what the state is now, and the log answers how it got there.
 *
 * [foregroundPackage] is set only on [TaskEventType.SWITCHED_AWAY], where it
 * names the app the user jumped to. It is a package name, never window
 * content: the accessibility service exposes nothing else.
 */
data class TaskEvent(
    val id: Long = 0L,
    val taskId: Long,
    val type: TaskEventType,
    val at: Long,
    val foregroundPackage: String? = null
)
