package tech.benedicta.will.infrastructure.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskRepository

class RoomTaskRepository(private val dao: TaskDao) : TaskRepository {
    override suspend fun insertTask(task: Task): Long = dao.insert(task.toEntity())
    override fun getAllTasks(): Flow<List<Task>> = dao.getAll().map { list -> list.map { it.toTask() } }
    override suspend fun updateTask(task: Task) = dao.update(task.toEntity())
    override suspend fun deleteTask(task: Task) = dao.delete(task.toEntity())
    override suspend fun deleteTaskById(taskId: Long) = dao.deleteById(taskId)
    override suspend fun setActiveTask(taskId: Long) {
        dao.clearAllActive()
        dao.setActive(taskId)
    }
    override suspend fun clearActiveTask() = dao.clearAllActive()
    override suspend fun deleteAllTasks() = dao.deleteAll()
    override suspend fun clearConversationKey(taskId: Long) = dao.clearConversationKey(taskId)
    override suspend fun upsertByConversationKey(task: Task): Long =
        dao.upsertByConversationKey(task.toEntity())
}
