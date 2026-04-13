package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tech.benedicta.will.task.Task

class NotificationProcessorTest {

    private val processor = NotificationProcessor()

    @Test
    fun `converts notification data to Task with correct fields`() {
        val result = processor.process(
            title = "New message",
            text = "Hello from John",
            sourcePackage = "com.whatsapp",
            timestamp = 1000L
        )

        assertEquals("New message", result?.title)
        assertEquals("Hello from John", result?.text)
        assertEquals("com.whatsapp", result?.sourcePackage)
        assertEquals(1000L, result?.createdAt)
    }

    @Test
    fun `returns null when title is null and text is null`() {
        val result = processor.process(
            title = null,
            text = null,
            sourcePackage = "com.example",
            timestamp = 0L
        )

        assertNull(result)
    }

    @Test
    fun `returns null when title and text are blank`() {
        val result = processor.process(
            title = "  ",
            text = "  ",
            sourcePackage = "com.example",
            timestamp = 0L
        )

        assertNull(result)
    }

    @Test
    fun `uses text as title fallback when title is null`() {
        val result = processor.process(
            title = null,
            text = "You have a new notification",
            sourcePackage = "com.example",
            timestamp = 0L
        )

        assertEquals("You have a new notification", result?.title)
        assertEquals("", result?.text)
    }

    @Test
    fun `task id defaults to zero before insertion`() {
        val result = processor.process(
            title = "Test",
            text = "body",
            sourcePackage = "com.test",
            timestamp = 0L
        )

        assertEquals(0L, result?.id)
    }

    @Test
    fun `task startedAt and completedAt are null by default`() {
        val result = processor.process(
            title = "Test",
            text = "body",
            sourcePackage = "com.test",
            timestamp = 0L
        )

        assertNull(result?.startedAt)
        assertNull(result?.completedAt)
    }

    @Test
    fun `returns null when sourcePackage is blank`() {
        val result = processor.process(
            title = "Test",
            text = "body",
            sourcePackage = "",
            timestamp = 0L
        )

        assertNull(result)
    }
}
