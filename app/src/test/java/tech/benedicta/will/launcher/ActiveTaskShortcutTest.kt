package tech.benedicta.will.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.task.Task

class ActiveTaskShortcutTest {

    private fun task(
        id: Long = 7L,
        title: String = "responder Maria",
        startedAt: Long? = 1_000L,
        completedAt: Long? = null
    ) = Task(
        id = id,
        title = title,
        text = "",
        sourcePackage = "com.whatsapp",
        createdAt = 0L,
        startedAt = startedAt,
        completedAt = completedAt
    )

    @Test
    fun `the task you are working on becomes the shortcut`() {
        val spec = ActiveTaskShortcut.describe(task())

        assertEquals("responder Maria", spec?.longLabel)
    }

    @Test
    fun `nothing active means no shortcut`() {
        assertNull(ActiveTaskShortcut.describe(null))
    }

    @Test
    fun `a finished task is not what you are working on`() {
        assertNull(ActiveTaskShortcut.describe(task(completedAt = 5_000L)))
    }

    @Test
    fun `a task you have not started is not what you are working on`() {
        assertNull(ActiveTaskShortcut.describe(task(startedAt = null)))
    }

    @Test
    fun `a blank title cannot be a shortcut label`() {
        assertNull(ActiveTaskShortcut.describe(task(title = "   ")))
    }

    @Test
    fun `the id is stable for a task, so the shortcut is replaced and not duplicated`() {
        assertEquals(
            ActiveTaskShortcut.describe(task(id = 7L))?.id,
            ActiveTaskShortcut.describe(task(id = 7L, title = "outro titulo"))?.id
        )
    }

    @Test
    fun `the short label fits where the system only gives a few characters`() {
        val spec = ActiveTaskShortcut.describe(
            task(title = "responder Maria sobre o contrato de aluguel")
        )

        assertTrue(
            "short label was ${spec?.shortLabel?.length} characters",
            (spec?.shortLabel?.length ?: 0) <= ActiveTaskShortcut.SHORT_LABEL_LIMIT
        )
    }

    @Test
    fun `the long label is kept whole when it already fits`() {
        val spec = ActiveTaskShortcut.describe(task(title = "pagar boleto"))

        assertEquals("pagar boleto", spec?.longLabel)
    }

    @Test
    fun `a very long title is cut rather than rejected`() {
        val spec = ActiveTaskShortcut.describe(task(title = "a".repeat(200)))

        assertTrue((spec?.longLabel?.length ?: 0) <= ActiveTaskShortcut.LONG_LABEL_LIMIT)
    }

    @Test
    fun `the title is trimmed before it is measured`() {
        val spec = ActiveTaskShortcut.describe(task(title = "   pagar boleto   "))

        assertEquals("pagar boleto", spec?.longLabel)
    }

    @Test
    fun `the shortcut carries the task id so opening it can find the task`() {
        assertEquals(7L, ActiveTaskShortcut.describe(task(id = 7L))?.taskId)
    }
}
