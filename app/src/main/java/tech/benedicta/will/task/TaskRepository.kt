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
}
