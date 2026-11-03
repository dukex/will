package tech.benedicta.will.task

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryTaskEventRepository

class TaskEventRepositoryTest {

    private fun event(
        taskId: Long,
        type: TaskEventType,
        at: Long,
        foregroundPackage: String? = null
    ) = TaskEvent(taskId = taskId, type = type, at = at, foregroundPackage = foregroundPackage)

    @Test
    fun `append assigns an id and returns it`() = runTest {
        val repo = MemoryTaskEventRepository()

        val id = repo.append(event(1L, TaskEventType.CREATED, 1_000L))

        assertEquals(1L, id)
    }

    @Test
    fun `appended events come back for their task`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))
        repo.append(event(1L, TaskEventType.STARTED, 2_000L))

        val events = repo.eventsForTask(1L).first()

        assertEquals(2, events.size)
        assertEquals(TaskEventType.CREATED, events[0].type)
        assertEquals(TaskEventType.STARTED, events[1].type)
    }

    @Test
    fun `events of other tasks are not returned`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))
        repo.append(event(2L, TaskEventType.CREATED, 1_000L))

        val events = repo.eventsForTask(1L).first()

        assertEquals(1, events.size)
        assertEquals(1L, events[0].taskId)
    }

    @Test
    fun `events are emitted in chronological order even when appended out of order`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.COMPLETED, 3_000L))
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))
        repo.append(event(1L, TaskEventType.STARTED, 2_000L))

        val events = repo.eventsForTask(1L).first()

        assertEquals(
            listOf(TaskEventType.CREATED, TaskEventType.STARTED, TaskEventType.COMPLETED),
            events.map { it.type }
        )
    }

    @Test
    fun `events sharing a timestamp keep their insertion order`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.PAUSED, 5_000L))
        repo.append(event(1L, TaskEventType.RESUMED, 5_000L))

        val events = repo.eventsForTask(1L).first()

        assertEquals(
            listOf(TaskEventType.PAUSED, TaskEventType.RESUMED),
            events.map { it.type }
        )
    }

    @Test
    fun `nothing is ever updated in place so repeated types all survive`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.SWITCHED_AWAY, 1_000L, "com.instagram.android"))
        repo.append(event(1L, TaskEventType.SWITCHED_AWAY, 2_000L, "com.twitter"))
        repo.append(event(1L, TaskEventType.SWITCHED_AWAY, 3_000L, "com.instagram.android"))

        val events = repo.eventsForTask(1L).first()

        assertEquals(3, events.size)
        assertEquals(
            listOf("com.instagram.android", "com.twitter", "com.instagram.android"),
            events.map { it.foregroundPackage }
        )
    }

    @Test
    fun `eventsSince includes the boundary timestamp`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 999L))
        repo.append(event(1L, TaskEventType.STARTED, 1_000L))

        val events = repo.eventsSince(1_000L).first()

        assertEquals(1, events.size)
        assertEquals(TaskEventType.STARTED, events[0].type)
    }

    @Test
    fun `eventsSince spans every task`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))
        repo.append(event(2L, TaskEventType.CREATED, 1_500L))

        val events = repo.eventsSince(0L).first()

        assertEquals(listOf(1L, 2L), events.map { it.taskId })
    }

    @Test
    fun `eventsSince is empty when nothing is recent enough`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))

        assertTrue(repo.eventsSince(2_000L).first().isEmpty())
    }

    @Test
    fun `deleting a task's events leaves other tasks untouched`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))
        repo.append(event(2L, TaskEventType.CREATED, 1_000L))

        repo.deleteEventsForTask(1L)

        assertTrue(repo.eventsForTask(1L).first().isEmpty())
        assertEquals(1, repo.eventsForTask(2L).first().size)
    }

    @Test
    fun `deleting all events clears every task`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))
        repo.append(event(2L, TaskEventType.STARTED, 2_000L))

        repo.deleteAllEvents()

        assertTrue(repo.eventsSince(0L).first().isEmpty())
    }

    @Test
    fun `ids keep climbing after a delete so a reused task id cannot collide`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.CREATED, 1_000L))
        repo.deleteEventsForTask(1L)

        val id = repo.append(event(1L, TaskEventType.CREATED, 2_000L))

        assertEquals(2L, id)
    }

    @Test
    fun `foregroundPackage is null on everything but a switch`() = runTest {
        val repo = MemoryTaskEventRepository()
        repo.append(event(1L, TaskEventType.STARTED, 1_000L))

        val stored = repo.eventsForTask(1L).first().single()

        assertEquals(null, stored.foregroundPackage)
    }
}
