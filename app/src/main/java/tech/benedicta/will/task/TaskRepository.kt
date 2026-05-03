package tech.benedicta.will.task

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun insertTask(task: Task): Long
    fun getAllTasks(): Flow<List<Task>>
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteTaskById(taskId: Long)
    suspend fun setActiveTask(taskId: Long)
    suspend fun clearActiveTask()
    suspend fun deleteAllTasks()
    suspend fun clearConversationKey(taskId: Long)
    suspend fun upsertByConversationKey(task: Task): Long

    /** Set [Task.startedAt] only if currently null. No-op if already set. */
    suspend fun markStartedIfNeeded(taskId: Long, startedAt: Long)

    /** Increment [Task.contextSwitchCount] by one. */
    suspend fun incrementContextSwitch(taskId: Long)
}
