package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tech.benedicta.will.task.Task

// Whether a title is noise is PatternIntentionClassifierTest's subject now;
// this covers the structural mapping only.
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
    fun `shouldIgnoreFlags returns true for ONGOING_EVENT flag`() {
        val ongoingEventFlag = 0x00000002
        assert(processor.shouldIgnoreFlags(ongoingEventFlag))
    }

    @Test
    fun `shouldIgnoreFlags returns false for zero flags`() {
        assert(!processor.shouldIgnoreFlags(0))
    }

    @Test
    fun `passes notificationKey through to Task`() {
        val result = processor.process(
            title = "New message",
            text = "Hello",
            sourcePackage = "com.whatsapp",
            timestamp = 1000L,
            notificationKey = "0|com.whatsapp|123|null|10042"
        )

        assertEquals("0|com.whatsapp|123|null|10042", result?.notificationKey)
    }

    @Test
    fun `notificationKey defaults to null`() {
        val result = processor.process(
            title = "Test",
            text = "body",
            sourcePackage = "com.test",
            timestamp = 0L
        )

        assertNull(result?.notificationKey)
    }

    @Test
    fun `derives conversationKey from packageName and title`() {
        val result = processor.process(
            title = "Maria",
            text = "Hi",
            sourcePackage = "com.whatsapp",
            timestamp = 1000L
        )

        assertEquals("com.whatsapp:maria", result?.conversationKey)
    }

    @Test
    fun `same packageName and same title produce identical conversationKey`() {
        val first = processor.process("Maria", "Hi", "com.whatsapp", 1000L)
        val second = processor.process("Maria", "Different message", "com.whatsapp", 2000L)

        assertEquals(first?.conversationKey, second?.conversationKey)
    }

    @Test
    fun `different titles produce different conversationKeys`() {
        val maria = processor.process("Maria", "Hi", "com.whatsapp", 1000L)
        val joao = processor.process("Joao", "Hi", "com.whatsapp", 1000L)

        assert(maria?.conversationKey != joao?.conversationKey)
    }

    @Test
    fun `conversationKey is case insensitive and trimmed`() {
        val a = processor.process("  Maria  ", "Hi", "com.whatsapp", 1000L)
        val b = processor.process("MARIA", "Hi", "com.whatsapp", 2000L)
        val c = processor.process("maria", "Hi", "com.whatsapp", 3000L)

        assertEquals(a?.conversationKey, b?.conversationKey)
        assertEquals(b?.conversationKey, c?.conversationKey)
    }

    @Test
    fun `same title in different packages produce different conversationKeys`() {
        val whats = processor.process("Maria", "Hi", "com.whatsapp", 1000L)
        val tg = processor.process("Maria", "Hi", "org.telegram.messenger", 1000L)

        assert(whats?.conversationKey != tg?.conversationKey)
    }

    @Test
    fun `conversationTitle is preferred over title for the key`() {
        val a = processor.process(
            title = "Group A (2 messages): Sender X",
            text = "msg",
            sourcePackage = "com.example.messenger",
            timestamp = 1000L,
            conversationTitle = "Group A"
        )
        val b = processor.process(
            title = "Group A (3 messages): Sender Y",
            text = "msg",
            sourcePackage = "com.example.messenger",
            timestamp = 2000L,
            conversationTitle = "Group A"
        )

        assertEquals(a?.conversationKey, b?.conversationKey)
        assertEquals("com.example.messenger:group a", a?.conversationKey)
    }

    @Test
    fun `null or blank conversationTitle falls back to title-based key`() {
        val withNull = processor.process(
            title = "Contact One",
            text = "hi",
            sourcePackage = "com.example.messenger",
            timestamp = 1000L,
            conversationTitle = null
        )
        val withBlank = processor.process(
            title = "Contact One",
            text = "hi",
            sourcePackage = "com.example.messenger",
            timestamp = 2000L,
            conversationTitle = "   "
        )

        assertEquals("com.example.messenger:contact one", withNull?.conversationKey)
        assertEquals("com.example.messenger:contact one", withBlank?.conversationKey)
    }

    @Test
    fun `conversationKey falls back to text when title is null`() {
        val result = processor.process(
            title = null,
            text = "You have a new notification",
            sourcePackage = "com.example",
            timestamp = 0L
        )

        assertEquals("com.example:you have a new notification", result?.conversationKey)
    }

    @Test
    fun `android system package is ignored`() {
        assertNull(processor.process("title", "body", "android", 0L))
    }

    @Test
    fun `systemui package is ignored`() {
        assertNull(processor.process("title", "body", "com.android.systemui", 0L))
    }

    @Test
    fun `deskclock package is ignored`() {
        assertNull(processor.process("title", "body", "com.google.android.deskclock", 0L))
    }

    @Test
    fun `messenger grouped-conversation message-count suffix is stripped from key`() {
        val first = processor.process("Group A", "msg", "com.whatsapp", 1000L)
        val second = processor.process(
            "Group A (2 messages): Sender X",
            "msg",
            "com.whatsapp",
            2000L
        )
        val later = processor.process(
            "Group A (28 messages): Sender X",
            "msg",
            "com.whatsapp",
            3000L
        )

        assertEquals(first?.conversationKey, second?.conversationKey)
        assertEquals(second?.conversationKey, later?.conversationKey)
    }

    @Test
    fun `singular message suffix is also stripped`() {
        val first = processor.process("Contact One", "msg", "com.whatsapp", 1000L)
        val second = processor.process(
            "Contact One (1 message): Contact One",
            "msg",
            "com.whatsapp",
            2000L
        )

        assertEquals(first?.conversationKey, second?.conversationKey)
    }
}
