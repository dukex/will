package tech.benedicta.will.task

import kotlinx.coroutines.flow.Flow

class RoomTaskRepository(private val dao: TaskDao) : TaskRepository {
    override suspend fun insertTask(task: Task): Long = dao.insert(task)
    override fun getAllTasks(): Flow<List<Task>> = dao.getAll()
    override suspend fun updateTask(task: Task) = dao.update(task)
    override suspend fun deleteTask(task: Task) = dao.delete(task)
    override suspend fun deleteTaskById(taskId: Long) = dao.deleteById(taskId)
}
