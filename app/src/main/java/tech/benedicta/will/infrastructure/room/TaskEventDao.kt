package tech.benedicta.will.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Append and read only. There is no `@Update`: the log is append-only, and
 * leaving the annotation out keeps that true by construction.
 *
 * `ORDER BY at ASC, id ASC` is the ordering the [tech.benedicta.will.task.TaskEventRepository]
 * contract promises: chronological, ties broken by insertion order.
 */
@Dao
interface TaskEventDao {
    @Insert
    suspend fun insert(event: TaskEventEntity): Long

    @Query("SELECT * FROM task_events WHERE taskId = :taskId ORDER BY at ASC, id ASC")
    fun forTask(taskId: Long): Flow<List<TaskEventEntity>>

    @Query("SELECT * FROM task_events WHERE at >= :from ORDER BY at ASC, id ASC")
    fun since(from: Long): Flow<List<TaskEventEntity>>

    @Query("DELETE FROM task_events WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Long)

    @Query("DELETE FROM task_events")
    suspend fun deleteAll()
}
