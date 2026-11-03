package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskOriginTest {

    @Test
    fun `a task defaults to the notification origin`() {
        val task = Task(title = "Maria", text = "Hi", sourcePackage = "com.whatsapp", createdAt = 0L)

        assertEquals(TaskOrigin.NOTIFICATION, task.origin)
    }

    @Test
    fun `stored names round-trip for every origin`() {
        TaskOrigin.entries.forEach { origin ->
            assertEquals(origin, TaskOrigin.fromStored(origin.name))
        }
    }

    @Test
    fun `an unknown stored value reads as notification instead of throwing`() {
        assertEquals(TaskOrigin.NOTIFICATION, TaskOrigin.fromStored("TELEPATHY"))
    }

    @Test
    fun `a null stored value reads as notification`() {
        assertEquals(TaskOrigin.NOTIFICATION, TaskOrigin.fromStored(null))
    }

    @Test
    fun `legacy rows with the manual source package backfill to manual`() {
        assertEquals(
            TaskOrigin.MANUAL,
            TaskOrigin.forLegacySourcePackage(TaskOrigin.MANUAL_SOURCE_PACKAGE)
        )
    }

    @Test
    fun `legacy rows with a real package backfill to notification`() {
        assertEquals(TaskOrigin.NOTIFICATION, TaskOrigin.forLegacySourcePackage("com.whatsapp"))
    }

    @Test
    fun `tasks the user did not write are distinguishable from the ones they did`() {
        val typed = Task(
            title = "pay the bill",
            text = "",
            sourcePackage = TaskOrigin.MANUAL_SOURCE_PACKAGE,
            createdAt = 0L,
            origin = TaskOrigin.MANUAL
        )
        val handedOver = typed.copy(origin = TaskOrigin.AGENT)
        val shared = typed.copy(origin = TaskOrigin.SHARED)

        assertEquals(false, typed.isAuthoredElsewhere)
        assertEquals(true, handedOver.isAuthoredElsewhere)
        assertEquals(true, shared.isAuthoredElsewhere)
    }

    @Test
    fun `a notification task is not treated as authored elsewhere`() {
        val fromNotification = Task(
            title = "Maria",
            text = "Hi",
            sourcePackage = "com.whatsapp",
            createdAt = 0L,
            origin = TaskOrigin.NOTIFICATION
        )

        assertEquals(false, fromNotification.isAuthoredElsewhere)
    }
}
