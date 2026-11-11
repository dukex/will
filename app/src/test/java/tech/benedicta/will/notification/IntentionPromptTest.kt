package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The two things that can go wrong with a model are asking badly and
 * misreading the reply, so both live here where they can be tested. Inference
 * itself needs a device.
 *
 * The answer space is deliberately narrow, the same way the app search's is:
 * there the model answers with numbers from a list Will offered, so it cannot
 * name an app nobody has installed. Here it picks a kind from a closed list
 * and a subject that has to appear in the notification, so it cannot invent a
 * person, a sum or an appointment that was never mentioned. Will writes the
 * final words itself, from a string resource, so the phrasing follows the
 * phone's language rather than the model's.
 */
class IntentionPromptTest {

    private val title = "Banco Itaú"
    private val text = "Confirme sua presença na reunião de sexta"

    private fun parse(answer: String, t: String = title, b: String = text) =
        IntentionPrompt.parse(answer, t, b)

    // --- asking ---

    @Test
    fun `the prompt carries what the notification said`() {
        val prompt = IntentionPrompt.build(title, text)!!

        assertTrue(prompt.contains(title))
        assertTrue(prompt.contains(text))
    }

    @Test
    fun `the prompt offers only the kinds Will can phrase`() {
        val prompt = IntentionPrompt.build(title, text)!!

        IntentionKind.entries.forEach { kind ->
            assertTrue("the model must be told about $kind", prompt.contains(kind.name))
        }
    }

    @Test
    fun `a notification with nothing in it is not worth asking about`() {
        assertNull(IntentionPrompt.build("", ""))
        assertNull(IntentionPrompt.build("   ", "  "))
    }

    // --- reading the reply ---

    @Test
    fun `a well formed answer is read`() {
        val read = parse("CONFIRM | reunião de sexta")

        assertEquals(IntentionKind.CONFIRM, read?.kind)
        assertEquals("reunião de sexta", read?.subject)
    }

    @Test
    fun `the model may answer in any case, with any spacing`() {
        assertEquals(IntentionKind.CONFIRM, parse("  confirm|reunião de sexta ")?.kind)
    }

    @Test
    fun `a subject the notification never mentioned is refused`() {
        assertNull(
            "the model must not invent a person, a sum or a date",
            parse("PAY | boleto do cartão")
        )
    }

    @Test
    fun `a subject is matched past accents and case, because the model may normalise`() {
        assertEquals("reuniao de sexta", parse("CONFIRM | reuniao de sexta")?.subject)
        assertEquals("REUNIÃO DE SEXTA", parse("CONFIRM | REUNIÃO DE SEXTA")?.subject)
    }

    @Test
    fun `a kind Will cannot phrase is refused rather than guessed at`() {
        assertNull(parse("ARCHIVE | reunião de sexta"))
        assertNull(parse("| reunião de sexta"))
    }

    @Test
    fun `an answer naming two kinds is refused, because it decided nothing`() {
        assertNull(parse("CONFIRM or PAY | reunião de sexta"))
    }

    @Test
    fun `an answer with no subject is refused`() {
        assertNull(parse("CONFIRM |   "))
        assertNull(parse("CONFIRM"))
    }

    @Test
    fun `the model declining is a legitimate answer`() {
        assertNull(parse("NONE"))
        assertNull(parse(""))
        assertNull(parse("   "))
    }

    @Test
    fun `prose instead of an answer yields nothing rather than a bad task`() {
        assertNull(parse("I think the user should probably confirm their attendance"))
    }

    @Test
    fun `a subject longer than a title is refused, because it is a summary`() {
        val longText = "pagar " + "a".repeat(200)

        assertNull(parse("PAY | " + "a".repeat(200), b = longText))
    }

    @Test
    fun `a subject found in the title counts, not only one in the body`() {
        assertEquals("Banco Itaú", parse("PAY | Banco Itaú")?.subject)
    }

    @Test
    fun `the answer may carry the model's usual padding`() {
        assertEquals(
            IntentionKind.CONFIRM,
            parse("Answer: CONFIRM | reunião de sexta\n")?.kind
        )
    }
}
