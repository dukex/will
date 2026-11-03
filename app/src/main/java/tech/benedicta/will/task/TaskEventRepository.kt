package tech.benedicta.will.task

import kotlinx.coroutines.flow.Flow

/**
 * Append-only store for [TaskEvent].
 *
 * Contract every implementation must honour:
 * - [append] assigns the id and returns it; nothing is ever updated in place.
 * - both read flows emit in chronological order, ties broken by insertion
 *   order, so a replay reconstructs the task's history exactly.
 * - [deleteEventsForTask] and [deleteAllEvents] are the only ways rows leave.
 *   They exist for US-07: when the user removes a task, or all of them, the
 *   behavioural history goes with it.
 */
interface TaskEventRepository {
    suspend fun append(event: TaskEvent): Long

    fun eventsForTask(taskId: Long): Flow<List<TaskEvent>>

    /** Events at or after [from], across all tasks. */
    fun eventsSince(from: Long): Flow<List<TaskEvent>>

    suspend fun deleteEventsForTask(taskId: Long)

    suspend fun deleteAllEvents()
}
