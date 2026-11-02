package tech.benedicta.will.infrastructure.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.benedicta.will.task.TaskEvent
import tech.benedicta.will.task.TaskEventRepository

class RoomTaskEventRepository(private val dao: TaskEventDao) : TaskEventRepository {

    override suspend fun append(event: TaskEvent): Long = dao.insert(event.toEntity())

    override fun eventsForTask(taskId: Long): Flow<List<TaskEvent>> =
        dao.forTask(taskId).map { rows -> rows.mapNotNull { it.toTaskEvent() } }

    override fun eventsSince(from: Long): Flow<List<TaskEvent>> =
        dao.since(from).map { rows -> rows.mapNotNull { it.toTaskEvent() } }

    override suspend fun deleteEventsForTask(taskId: Long) = dao.deleteForTask(taskId)

    override suspend fun deleteAllEvents() = dao.deleteAll()
}
