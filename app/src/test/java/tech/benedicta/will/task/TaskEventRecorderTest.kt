package tech.benedicta.will.task

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryTaskEventRepository

class TaskEventRecorderTest {

    private fun task(id: Long, startedAt: Long? = null) = Task(
        id = id,
        title = "Sarah",
        text = "msg",
        sourcePackage = "com.slack",
        createdAt = 1_000L,
        startedAt = startedAt
    )

    @Test
    fun `created stamps the configured clock`() = runTest {
        val events = MemoryTaskEventRepository()
        val recorder = TaskEventRecorder(events, now = { 7_000L })

        recorder.created(taskId = 1L)

        val stored = events.eventsForTask(1L).first().single()
        assertEquals(TaskEventType.CREATED, stored.type)
        assertEquals(7_000L, stored.at)
    }

    @Test
    fun `activating a never-started task records STARTED`() = runTest {
        val events = MemoryTaskEventRepository()
        val recorder = TaskEventRecorder(events, now = { 7_000L })

        recorder.activated(task(id = 1L, startedAt = null))

        assertEquals(
            TaskEventType.STARTED,
            events.eventsForTask(1L).first().single().type
        )
    }

    @Test
    fun `activating an already-started task records RESUMED`() = runTest {
        val events = MemoryTaskEventRepository()
        val recorder = TaskEventRecorder(events, now = { 7_000L })

        recorder.activated(task(id = 1L, startedAt = 2_000L))

        assertEquals(
            TaskEventType.RESUMED,
            events.eventsForTask(1L).first().single().type
        )
    }

    @Test
    fun `a pause and resume cycle reads back as four events in order`() = runTest {
        val events = MemoryTaskEventRepository()
        var clock = 1_000L
        val recorder = TaskEventRecorder(events, now = { clock })

        recorder.created(1L)
        clock = 2_000L
        recorder.activated(task(id = 1L, startedAt = null))
        clock = 3_000L
        recorder.paused(1L)
        clock = 4_000L
        recorder.activated(task(id = 1L, startedAt = 2_000L))

        assertEquals(
            listOf(
                TaskEventType.CREATED,
                TaskEventType.STARTED,
                TaskEventType.PAUSED,
                TaskEventType.RESUMED
            ),
            events.eventsForTask(1L).first().map { it.type }
        )
    }

    @Test
    fun `switchedAway carries the package the user jumped to`() = runTest {
        val events = MemoryTaskEventRepository()
        val recorder = TaskEventRecorder(events, now = { 7_000L })

        recorder.switchedAway(taskId = 1L, foregroundPackage = "com.instagram.android")

        val stored = events.eventsForTask(1L).first().single()
        assertEquals(TaskEventType.SWITCHED_AWAY, stored.type)
        assertEquals("com.instagram.android", stored.foregroundPackage)
    }

    @Test
    fun `completed and reopened are both recorded so a reopen is not a silent edit`() = runTest {
        val events = MemoryTaskEventRepository()
        var clock = 1_000L
        val recorder = TaskEventRecorder(events, now = { clock })

        recorder.completed(1L)
        clock = 2_000L
        recorder.reopened(1L)

        assertEquals(
            listOf(TaskEventType.COMPLETED, TaskEventType.REOPENED),
            events.eventsForTask(1L).first().map { it.type }
        )
    }

    @Test
    fun `forgetting a task drops its history and records no tombstone`() = runTest {
        val events = MemoryTaskEventRepository()
        val recorder = TaskEventRecorder(events, now = { 7_000L })
        recorder.created(1L)
        recorder.activated(task(id = 1L))

        recorder.forgot(1L)

        assertTrue(events.eventsForTask(1L).first().isEmpty())
        assertTrue(events.eventsSince(0L).first().isEmpty())
    }

    @Test
    fun `forgetting one task leaves the others intact`() = runTest {
        val events = MemoryTaskEventRepository()
        val recorder = TaskEventRecorder(events, now = { 7_000L })
        recorder.created(1L)
        recorder.created(2L)

        recorder.forgot(1L)

        assertEquals(1, events.eventsSince(0L).first().size)
        assertEquals(2L, events.eventsSince(0L).first().single().taskId)
    }

    @Test
    fun `forgetting everything clears the log`() = runTest {
        val events = MemoryTaskEventRepository()
        val recorder = TaskEventRecorder(events, now = { 7_000L })
        recorder.created(1L)
        recorder.created(2L)

        recorder.forgotEverything()

        assertTrue(events.eventsSince(0L).first().isEmpty())
    }
}
