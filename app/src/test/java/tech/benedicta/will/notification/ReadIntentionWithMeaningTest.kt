package tech.benedicta.will.notification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The same shape as `SearchWithMeaning`: the answer that needs no model comes
 * first, and the model is asked only the question it alone can answer.
 *
 * The notification path injects no model, because ML Kit refuses to run
 * outside the foreground; `ModelReviewPassTest` covers the place it does run.
 * These still pin the composition, which is what decides that a category
 * answer is never overridden by a model.
 *
 * The model can only ever name what a notification is asking for. It cannot
 * drop one, so the worst a wrong answer costs is a title the user disagrees
 * with, over an original that `Task.rawTitle` still holds.
 */
class ReadIntentionWithMeaningTest {

    private var asked = 0

    private fun model(answer: ReadIntention?) = SemanticIntentionReader { _, _ ->
        asked++
        answer
    }

    private fun reader(semantic: SemanticIntentionReader) =
        ReadIntentionWithMeaning(IntentionExtractor(), semantic)

    @Test
    fun `a category the system declares is read without the model`() = runTest {
        val read = reader(model(ReadIntention(IntentionKind.PAY, "nothing"))).read(
            category = IntentionExtractor.CATEGORY_MESSAGE,
            title = "Maria",
            text = "oi",
            sourcePackage = "com.whatsapp"
        )

        assertEquals(IntentionKind.REPLY, read?.kind)
        assertEquals("Maria", read?.subject)
        assertEquals("the model must not be asked what the system already said", 0, asked)
    }

    @Test
    fun `what no category can say is asked of the model`() = runTest {
        val read = reader(model(ReadIntention(IntentionKind.CONFIRM, "a reunião de sexta"))).read(
            category = null,
            title = "Agenda",
            text = "Confirme sua presença na reunião de sexta",
            sourcePackage = "com.calendar"
        )

        assertEquals(IntentionKind.CONFIRM, read?.kind)
        assertEquals("a reunião de sexta", read?.subject)
        assertEquals(1, asked)
    }

    @Test
    fun `the original title is kept whichever path read it`() = runTest {
        val read = reader(model(ReadIntention(IntentionKind.PAY, "o boleto"))).read(
            category = null,
            title = "Banco",
            text = "o boleto vence hoje",
            sourcePackage = "com.bank"
        )

        assertEquals("Banco", read?.rawTitle)
    }

    @Test
    fun `the model declining leaves the notification exactly as it arrived`() = runTest {
        val read = reader(model(null)).read(
            category = null,
            title = "Promo",
            text = "50% off",
            sourcePackage = "com.shop"
        )

        assertNull(read)
    }

    @Test
    fun `a model that throws is the same as a model that is not there`() = runTest {
        val broken = SemanticIntentionReader { _, _ -> error("AICore fell over") }

        val read = ReadIntentionWithMeaning(IntentionExtractor(), broken).read(
            category = null,
            title = "Banco",
            text = "o boleto vence hoje",
            sourcePackage = "com.bank"
        )

        assertNull("a broken model must not take the notification with it", read)
    }

    @Test
    fun `with no model at all the behaviour is exactly what it was before`() = runTest {
        val reader = ReadIntentionWithMeaning(IntentionExtractor())

        val fromCategory = reader.read(
            IntentionExtractor.CATEGORY_MISSED_CALL, "Miguel", "", "com.dialer"
        )
        val fromWords = reader.read(null, "Agenda", "Confirme sua presença", "com.calendar")

        assertEquals(IntentionKind.CALL_BACK, fromCategory?.kind)
        assertNull(fromWords)
    }

    @Test
    fun `a notification with nothing in it never reaches the model`() = runTest {
        val read = reader(model(ReadIntention(IntentionKind.PAY, "x"))).read(
            category = null,
            title = "   ",
            text = "",
            sourcePackage = "com.shop"
        )

        assertNull(read)
        assertEquals(0, asked)
    }

    @Test
    fun `the model is never asked about a task the user typed`() = runTest {
        val read = reader(model(ReadIntention(IntentionKind.PAY, "o boleto"))).read(
            category = null,
            title = "pagar o boleto",
            text = "",
            sourcePackage = "manual"
        )

        assertNull(
            "01-IDEA promises a typed intention is kept exactly as written",
            read
        )
        assertEquals(0, asked)
    }

    @Test
    fun `the model cannot hide a notification, only name what it asks for`() = runTest {
        val readings = IntentionKind.entries.map { kind ->
            asked = 0
            reader(model(ReadIntention(kind, "o boleto"))).read(
                category = null,
                title = "Banco",
                text = "o boleto vence hoje",
                sourcePackage = "com.bank"
            )
        }

        assertTrue("every kind yields a reading, never a suppression", readings.all { it != null })
        assertFalse(readings.any { it?.rawTitle == null })
    }

    @Test
    fun `noise never reaches the model, because triage runs first`() = runTest {
        var askedModel = 0
        val convert = NotificationToTask(
            processor = NotificationProcessor(),
            reader = ReadIntentionWithMeaning(
                IntentionExtractor(),
                SemanticIntentionReader { _, _ ->
                    askedModel++
                    ReadIntention(IntentionKind.PAY, "x")
                }
            ),
            phrase = { _, subject -> "pagar $subject" }
        )

        val dropped = convert.convert(
            category = null,
            title = "Baixando vídeo",
            text = "",
            sourcePackage = "com.downloader",
            timestamp = 0L,
            keep = { false }
        )

        assertNull(dropped)
        assertEquals("its words have no business reaching the model", 0, askedModel)
    }
}
