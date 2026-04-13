package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskTest {

    @Test
    fun `task has all required fields with correct defaults`() {
        val task = Task(
            title = "Reply to message",
            text = "You have a new message",
            sourcePackage = "com.whatsapp",
            createdAt = 1000L
        )

        assertEquals("Reply to message", task.title)
        assertEquals("You have a new message", task.text)
        assertEquals("com.whatsapp", task.sourcePackage)
        assertEquals(1000L, task.createdAt)
        assertNull(task.startedAt)
        assertNull(task.completedAt)
        assertEquals(0, task.contextSwitchCount)
        assertEquals(0L, task.id)
    }

    @Test
    fun `task with all fields set retains values`() {
        val task = Task(
            id = 42L,
            title = "Pay bill",
            text = "R$150,00 due today",
            sourcePackage = "com.bank",
            createdAt = 1000L,
            startedAt = 2000L,
            completedAt = 3000L,
            contextSwitchCount = 3
        )

        assertEquals(42L, task.id)
        assertEquals(2000L, task.startedAt)
        assertEquals(3000L, task.completedAt)
        assertEquals(3, task.contextSwitchCount)
    }

    @Test
    fun `two tasks with same content but different ids are not equal`() {
        val task1 = Task(id = 1L, title = "A", text = "B", sourcePackage = "pkg", createdAt = 0L)
        val task2 = Task(id = 2L, title = "A", text = "B", sourcePackage = "pkg", createdAt = 0L)

        assert(task1 != task2)
    }
}
