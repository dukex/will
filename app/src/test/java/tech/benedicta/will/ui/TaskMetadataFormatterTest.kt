package tech.benedicta.will.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.benedicta.will.task.TaskOrigin

class TaskMetadataFormatterTest {

    @Test
    fun `a notification keeps the app and the time it arrived`() {
        val line = taskMetadataLine(
            appLabel = "Slack",
            origin = TaskOrigin.NOTIFICATION,
            time = "14:32",
            unreadCount = 1
        )

        assertEquals("Slack · 14:32", line)
    }

    @Test
    fun `a notification origin is never spelled out, it is the ordinary case`() {
        val line = taskMetadataLine("Slack", TaskOrigin.NOTIFICATION, "14:32", 1)

        assertEquals(false, line.contains("notification"))
    }

    @Test
    fun `a task the user typed shows only the time, never the manual sentinel`() {
        val line = taskMetadataLine(
            appLabel = null,
            origin = TaskOrigin.MANUAL,
            time = "14:32",
            unreadCount = 1
        )

        assertEquals("14:32", line)
    }

    @Test
    fun `a task typed for a specific app keeps that app`() {
        val line = taskMetadataLine("Slack", TaskOrigin.MANUAL, "14:32", 1)

        assertEquals("Slack · 14:32", line)
    }

    @Test
    fun `a shared task says so, after the app it came from`() {
        val line = taskMetadataLine("Slack", TaskOrigin.SHARED, "14:32", 1)

        assertEquals("Slack · shared · 14:32", line)
    }

    @Test
    fun `a shared task with no app still says it was shared`() {
        val line = taskMetadataLine(null, TaskOrigin.SHARED, "14:32", 1)

        assertEquals("shared · 14:32", line)
    }

    @Test
    fun `a task an agent wrote says so`() {
        val line = taskMetadataLine(null, TaskOrigin.AGENT, "14:32", 1)

        assertEquals("from agent · 14:32", line)
    }

    @Test
    fun `a task an agent wrote about a specific app keeps both`() {
        val line = taskMetadataLine("Slack", TaskOrigin.AGENT, "14:32", 1)

        assertEquals("Slack · from agent · 14:32", line)
    }

    @Test
    fun `the unread count comes last so the newest fact is at the end`() {
        val line = taskMetadataLine("Slack", TaskOrigin.NOTIFICATION, "14:32", 3)

        assertEquals("Slack · 14:32 · 3 new", line)
    }

    @Test
    fun `a single unread message is not counted out loud`() {
        val line = taskMetadataLine("Slack", TaskOrigin.NOTIFICATION, "14:32", 1)

        assertEquals("Slack · 14:32", line)
    }

    @Test
    fun `an unread count below one is treated as one`() {
        val line = taskMetadataLine("Slack", TaskOrigin.NOTIFICATION, "14:32", 0)

        assertEquals("Slack · 14:32", line)
    }

    @Test
    fun `origin and unread count coexist without colliding`() {
        val line = taskMetadataLine("Slack", TaskOrigin.SHARED, "14:32", 4)

        assertEquals("Slack · shared · 14:32 · 4 new", line)
    }

    @Test
    fun `a blank app label is dropped rather than leaving a stray separator`() {
        val line = taskMetadataLine("   ", TaskOrigin.SHARED, "14:32", 1)

        assertEquals("shared · 14:32", line)
    }

    @Test
    fun `only the origins the user did not author are spelled out`() {
        val spelledOut = TaskOrigin.entries.filter { origin ->
            taskMetadataLine(null, origin, "14:32", 1) != "14:32"
        }

        assertEquals(listOf(TaskOrigin.SHARED, TaskOrigin.AGENT), spelledOut)
    }
}
