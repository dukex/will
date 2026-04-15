package tech.benedicta.will.infrastructure.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskRepository

class MemoryTaskRepository : TaskRepository {
    private var nextId = 1L
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    override suspend fun insertTask(task: Task): Long {
        val newTask = task.copy(id = nextId++)
        _tasks.update { it + newTask }
        return newTask.id
    }

    override fun getAllTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun updateTask(task: Task) {
        _tasks.update { list -> list.map { if (it.id == task.id) task else it } }
    }

    override suspend fun deleteTask(task: Task) = deleteTaskById(task.id)

    override suspend fun deleteTaskById(taskId: Long) {
        _tasks.update { list -> list.filter { it.id != taskId } }
    }

    override suspend fun setActiveTask(taskId: Long) {
        _tasks.update { list -> list.map { it.copy(isActive = it.id == taskId) } }
    }

    override suspend fun clearActiveTask() {
        _tasks.update { list -> list.map { it.copy(isActive = false) } }
    }
}
