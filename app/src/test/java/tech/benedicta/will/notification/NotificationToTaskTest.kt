package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import tech.benedicta.will.task.TaskOrigin

/**
 * The whole path a posted notification takes before it reaches the database:
 * structural mapping, then reading the conversation as something to do. The
 * notification service is a thin shell over this, so this is where the path is
 * covered.
 */
class NotificationToTaskTest {

    private val convert = NotificationToTask(
        processor = NotificationProcessor(),
        reader = ReadIntentionWithMeaning(IntentionExtractor()),
        phrase = { kind, subject ->
            when (kind) {
                IntentionKind.REPLY -> "responder $subject"
                IntentionKind.CALL_BACK -> "ligar de volta para $subject"
                IntentionKind.CONFIRM -> "confirmar $subject"
                IntentionKind.PAY -> "pagar $subject"
                IntentionKind.ATTEND -> "comparecer a $subject"
            }
        }
    )

    @Test
    fun `a WhatsApp message becomes replying to the person who sent it`() = runTest {
        val task = convert.convert(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = "Maria",
            text = "pode mandar o contrato?",
            sourcePackage = "com.whatsapp",
            timestamp = 1_000L,
            notificationKey = "k-1"
        )

        assertEquals("responder Maria", task?.title)
        assertEquals("pode mandar o contrato?", task?.text)
    }

    @Test
    fun `the notification's own title is kept so the change can be undone`() = runTest {
        val task = convert.convert(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = "Maria",
            text = "oi",
            sourcePackage = "com.whatsapp",
            timestamp = 1_000L
        )

        assertEquals("Maria", task?.rawTitle)
    }

    @Test
    fun `a missed call becomes calling back`() = runTest {
        val task = convert.convert(
            category = IntentionExtractor.CATEGORY_MISSED_CALL,
            title = "Miguel",
            text = "",
            sourcePackage = "com.android.dialer",
            timestamp = 1_000L
        )

        assertEquals("ligar de volta para Miguel", task?.title)
    }

    @Test
    fun `a notification that is not a conversation arrives exactly as it was`() = runTest {
        val task = convert.convert(
            category = null,
            title = "Boleto vence hoje",
            text = "R$ 212,40",
            sourcePackage = "com.itau",
            timestamp = 1_000L
        )

        assertEquals("Boleto vence hoje", task?.title)
        assertNull("nothing was derived, so there is nothing to undo", task?.rawTitle)
    }

    @Test
    fun `the conversation key still follows the original title, not the derived one`() = runTest {
        val first = convert.convert(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = "Maria",
            text = "oi",
            sourcePackage = "com.whatsapp",
            timestamp = 1_000L
        )
        val second = convert.convert(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = "Maria",
            text = "ainda esta ai?",
            sourcePackage = "com.whatsapp",
            timestamp = 2_000L
        )

        assertEquals(
            "two messages from Maria must still collapse into one task",
            first?.conversationKey,
            second?.conversationKey
        )
    }

    @Test
    fun `a notification the processor throws away is still thrown away`() = runTest {
        val task = convert.convert(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = null,
            text = null,
            sourcePackage = "com.whatsapp",
            timestamp = 1_000L
        )

        assertNull(task)
    }

    @Test
    fun `a task from a notification keeps the notification origin`() = runTest {
        val task = convert.convert(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = "Maria",
            text = "oi",
            sourcePackage = "com.whatsapp",
            timestamp = 1_000L
        )

        assertEquals(TaskOrigin.NOTIFICATION, task?.origin)
    }

    @Test
    fun `a conversation whose title is the message itself is left alone`() = runTest {
        val sentence = "pode me mandar o contrato assinado ainda hoje antes das cinco"

        val task = convert.convert(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = sentence,
            text = "",
            sourcePackage = "com.whatsapp",
            timestamp = 1_000L
        )

        assertEquals(sentence, task?.title)
        assertNull(task?.rawTitle)
    }
}
