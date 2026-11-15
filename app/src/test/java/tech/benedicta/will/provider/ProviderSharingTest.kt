package tech.benedicta.will.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin

/**
 * The boundary `01-IDEA.md` draws, as a function rather than a paragraph.
 *
 * The promise is about tracking data: what app you switched to, how long you
 * spent, what a notification said. A task you typed is not that, and syncing
 * it to Todoist is a thing you asked for. A task Will built out of someone's
 * WhatsApp message is, and it is not Will's to send anywhere.
 */
class ProviderSharingTest {

    private fun task(
        origin: TaskOrigin,
        title: String = "pagar o boleto",
        text: String = "",
        rawTitle: String? = null
    ) = Task(
        id = 7L,
        title = title,
        text = text,
        sourcePackage = "com.whatsapp",
        createdAt = 1_000L,
        startedAt = 2_000L,
        pausedAt = 3_000L,
        contextSwitchCount = 9,
        notificationKey = "0|com.whatsapp|1|null|10",
        conversationKey = "com.whatsapp:maria",
        unreadCount = 4,
        origin = origin,
        rawTitle = rawTitle
    )

    @Test
    fun `what the user wrote is theirs to send`() {
        listOf(TaskOrigin.MANUAL, TaskOrigin.SHARED, TaskOrigin.AGENT).forEach { origin ->
            assertEquals(
                "$origin was authored or chosen by the user",
                "pagar o boleto",
                ProviderSharing.payloadFor(task(origin))?.title
            )
        }
    }

    @Test
    fun `a notification is not the user's to send`() {
        assertNull(
            "a task Will built from someone's message is observed, not authored",
            ProviderSharing.payloadFor(task(TaskOrigin.NOTIFICATION))
        )
    }

    @Test
    fun `nothing observed rides along with what is sent`() {
        val payload = ProviderSharing.payloadFor(
            task(TaskOrigin.MANUAL, text = "o texto da mensagem", rawTitle = "Maria")
        )

        assertEquals("pagar o boleto", payload?.title)

        // Reads every value the payload would serialise, so a field added to
        // `ProviderPayload` carrying any of this fails here rather than at the
        // other end of an HTTP request.
        val everythingItCarries = payload.toString()
        listOf(
            "o texto da mensagem" to "the message body",
            "Maria" to "the title the notification arrived with",
            "com.whatsapp" to "which app it came from",
            "3000" to "when the user was pulled away",
            "9" to "how many times"
        ).forEach { (observed, what) ->
            assertFalse(
                "$what must not leave the device, found in $everythingItCarries",
                everythingItCarries.contains(observed)
            )
        }
    }

    @Test
    fun `completion travels, because that is the point of two-way`() {
        val open = task(TaskOrigin.MANUAL)
        val done = open.copy(completedAt = 9_000L)

        assertEquals(false, ProviderSharing.payloadFor(open)?.isCompleted)
        assertEquals(true, ProviderSharing.payloadFor(done)?.isCompleted)
    }

    @Test
    fun `a blank title is nothing to send`() {
        assertNull(ProviderSharing.payloadFor(task(TaskOrigin.MANUAL, title = "   ")))
    }
}
