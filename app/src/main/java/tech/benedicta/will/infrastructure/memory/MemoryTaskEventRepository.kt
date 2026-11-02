package tech.benedicta.will.infrastructure.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import tech.benedicta.will.task.TaskEvent
import tech.benedicta.will.task.TaskEventRepository

class MemoryTaskEventRepository : TaskEventRepository {
    private var nextId = 1L
    private val _events = MutableStateFlow<List<TaskEvent>>(emptyList())

    override suspend fun append(event: TaskEvent): Long {
        val stored = event.copy(id = nextId++)
        _events.update { it + stored }
        return stored.id
    }

    override fun eventsForTask(taskId: Long): Flow<List<TaskEvent>> =
        _events.asStateFlow().map { list -> list.filter { it.taskId == taskId }.chronological() }

    override fun eventsSince(from: Long): Flow<List<TaskEvent>> =
        _events.asStateFlow().map { list -> list.filter { it.at >= from }.chronological() }

    override suspend fun deleteEventsForTask(taskId: Long) {
        _events.update { list -> list.filter { it.taskId != taskId } }
    }

    override suspend fun deleteAllEvents() {
        _events.value = emptyList()
    }

    private fun List<TaskEvent>.chronological() = sortedWith(compareBy({ it.at }, { it.id }))
}
