package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskStatusTest {

    private fun task(
        startedAt: Long? = null,
        pausedAt: Long? = null,
        completedAt: Long? = null
    ) = Task(
        title = "t",
        text = "",
        sourcePackage = "pkg",
        createdAt = 0L,
        startedAt = startedAt,
        pausedAt = pausedAt,
        completedAt = completedAt
    )

    @Test
    fun `task with no timestamps is PENDING`() {
        assertEquals(TaskStatus.PENDING, task().status)
    }

    @Test
    fun `task with startedAt and no pausedAt or completedAt is IN_PROGRESS`() {
        assertEquals(TaskStatus.IN_PROGRESS, task(startedAt = 100L).status)
    }

    @Test
    fun `task with startedAt and pausedAt is PAUSED`() {
        assertEquals(
            TaskStatus.PAUSED,
            task(startedAt = 100L, pausedAt = 200L).status
        )
    }

    @Test
    fun `task with completedAt is DONE regardless of pausedAt`() {
        assertEquals(
            TaskStatus.DONE,
            task(startedAt = 100L, pausedAt = 200L, completedAt = 300L).status
        )
    }

    @Test
    fun `task with completedAt and no startedAt is still DONE`() {
        assertEquals(TaskStatus.DONE, task(completedAt = 300L).status)
    }

    @Test
    fun `task with pausedAt but no startedAt is PENDING`() {
        // Defensive: should not happen in practice (paused implies started),
        // but the derivation must not classify it as PAUSED.
        assertEquals(TaskStatus.PENDING, task(pausedAt = 200L).status)
    }
}
