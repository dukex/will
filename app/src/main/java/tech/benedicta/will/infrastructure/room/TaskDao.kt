package tech.benedicta.will.infrastructure.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: TaskEntity): Long

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("""
        UPDATE tasks
        SET pausedAt = :now
        WHERE startedAt IS NOT NULL
          AND pausedAt IS NULL
          AND completedAt IS NULL
    """)
    suspend fun pauseCurrentlyActive(now: Long)

    @Query("""
        UPDATE tasks
        SET startedAt = COALESCE(startedAt, :now),
            pausedAt = NULL
        WHERE id = :taskId
          AND completedAt IS NULL
    """)
    suspend fun activateById(taskId: Long, now: Long)

    @Transaction
    suspend fun setActiveAtomically(taskId: Long, now: Long) {
        pauseCurrentlyActive(now)
        activateById(taskId, now)
    }

    @Query("""
        UPDATE tasks
        SET pausedAt = :now
        WHERE startedAt IS NOT NULL
          AND pausedAt IS NULL
          AND completedAt IS NULL
    """)
    suspend fun clearAllActive(now: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Query("UPDATE tasks SET conversationKey = NULL WHERE id = :taskId")
    suspend fun clearConversationKey(taskId: Long)

    @Query("UPDATE tasks SET startedAt = :startedAt WHERE id = :taskId AND startedAt IS NULL")
    suspend fun markStartedIfNeeded(taskId: Long, startedAt: Long)

    @Query("UPDATE tasks SET contextSwitchCount = contextSwitchCount + 1 WHERE id = :taskId")
    suspend fun incrementContextSwitch(taskId: Long)

    @Query("SELECT * FROM tasks WHERE conversationKey = :key AND completedAt IS NULL LIMIT 1")
    suspend fun findOpenByConversationKey(key: String): TaskEntity?

    @Query("""
        UPDATE tasks
        SET notificationKey = :notificationKey,
            createdAt = :createdAt,
            title = :title,
            text = :text,
            shortcutId = :shortcutId,
            unreadCount = unreadCount + 1
        WHERE id = :taskId
    """)
    suspend fun bumpConversation(
        taskId: Long,
        notificationKey: String?,
        createdAt: Long,
        title: String,
        text: String,
        shortcutId: String?
    )

    @Transaction
    suspend fun upsertByConversationKey(task: TaskEntity): Long {
        val key = task.conversationKey
        if (key != null) {
            val existing = findOpenByConversationKey(key)
            if (existing != null) {
                bumpConversation(
                    taskId = existing.id,
                    notificationKey = task.notificationKey ?: existing.notificationKey,
                    createdAt = task.createdAt,
                    title = task.title,
                    text = task.text,
                    shortcutId = task.shortcutId ?: existing.shortcutId
                )
                return existing.id
            }
        }
        return insert(task.copy(unreadCount = 1))
    }
}
