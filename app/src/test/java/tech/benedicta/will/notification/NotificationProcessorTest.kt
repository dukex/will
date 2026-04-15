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

    @Test
    fun `returns null for ignored packages`() {
        NotificationProcessor.IGNORED_PACKAGES.forEach { pkg ->
            val result = processor.process(
                title = "Some notification",
                text = "body",
                sourcePackage = pkg,
                timestamp = 0L
            )
            assertNull("Expected null for ignored package: $pkg", result)
        }
    }

    @Test
    fun `Digital Wellbeing is in the ignored packages list`() {
        assert("com.google.android.apps.wellbeing" in NotificationProcessor.IGNORED_PACKAGES)
    }

    @Test
    fun `shouldIgnoreFlags returns true for GROUP_SUMMARY flag`() {
        val groupSummaryFlag = 0x00000200
        assert(processor.shouldIgnoreFlags(groupSummaryFlag))
    }

    @Test
    fun `shouldIgnoreFlags returns true for FOREGROUND_SERVICE flag`() {
        val foregroundServiceFlag = 0x00000040
        assert(processor.shouldIgnoreFlags(foregroundServiceFlag))
    }

    @Test
    fun `shouldIgnoreFlags returns false for normal notification flags`() {
        val onlyAlertOnceFlag = 0x00000008
        assert(!processor.shouldIgnoreFlags(onlyAlertOnceFlag))
    }

    @Test
    fun `shouldIgnoreFlags returns false for zero flags`() {
        assert(!processor.shouldIgnoreFlags(0))
    }
}
