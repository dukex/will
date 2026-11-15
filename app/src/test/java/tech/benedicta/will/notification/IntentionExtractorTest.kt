package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IntentionExtractorTest {

    private val extractor = IntentionExtractor()

    private fun extract(
        title: String,
        text: String = "",
        category: String? = IntentionExtractor.CATEGORY_MESSAGE,
        pkg: String = "com.whatsapp"
    ) = extractor.extract(category = category, title = title, text = text, sourcePackage = pkg)

    // --- a message becomes something to do about it ---

    @Test
    fun `a message from a person becomes replying to that person`() {
        val intention = extract("Maria", "pode mandar o contrato?")

        assertEquals(IntentionKind.REPLY, intention?.kind)
        assertEquals("Maria", intention?.subject)
    }

    @Test
    fun `a sender written into the title before a colon is the subject`() {
        val intention = extract("Maria: pode mandar o contrato?")

        assertEquals("Maria", intention?.subject)
    }

    @Test
    fun `a group conversation keeps the group as the subject`() {
        val intention = extract("Grupo Família")

        assertEquals(IntentionKind.REPLY, intention?.kind)
        assertEquals("Grupo Família", intention?.subject)
    }

    @Test
    fun `a message count suffix is not part of anyone's name`() {
        assertEquals("Maria", extract("Maria (3 messages)")?.subject)
        assertEquals("Maria", extract("Maria (2 mensagens)")?.subject)
    }

    @Test
    fun `surrounding whitespace is trimmed off the subject`() {
        assertEquals("Maria", extract("  Maria  ")?.subject)
    }

    // --- a missed call becomes calling back ---

    @Test
    fun `a missed call becomes calling that person back`() {
        val intention = extract("Miguel", category = IntentionExtractor.CATEGORY_MISSED_CALL)

        assertEquals(IntentionKind.CALL_BACK, intention?.kind)
        assertEquals("Miguel", intention?.subject)
    }

    // --- an email becomes replying ---

    @Test
    fun `an email becomes replying to the sender`() {
        val intention = extract(
            "Emerson Almeida",
            text = "contrato para revisar",
            category = IntentionExtractor.CATEGORY_EMAIL,
            pkg = "com.google.android.gm"
        )

        assertEquals(IntentionKind.REPLY, intention?.kind)
        assertEquals("Emerson Almeida", intention?.subject)
    }

    // --- everything else is left exactly as it arrived ---

    @Test
    fun `a notification that is not a conversation is left alone`() {
        assertNull(extract("Boleto vence hoje", category = null, pkg = "com.itau"))
        assertNull(extract("Seu pedido chegou", category = "promo", pkg = "com.shop"))
    }

    @Test
    fun `a calendar reminder is not a conversation`() {
        assertNull(
            extract(
                "Reunião de arquitetura",
                category = IntentionExtractor.CATEGORY_EVENT,
                pkg = "com.google.android.calendar"
            )
        )
    }

    @Test
    fun `a blank title derives nothing`() {
        assertNull(extract(""))
        assertNull(extract("   "))
    }

    @Test
    fun `a title long enough to be the message itself is not a name`() {
        val sentence = "pode me mandar o contrato assinado ainda hoje antes das cinco por favor"

        assertNull("that is the message, not who sent it", extract(sentence))
    }

    @Test
    fun `a title that is only punctuation derives nothing`() {
        assertNull(extract("..."))
        assertNull(extract(":"))
    }

    @Test
    fun `a colon with nothing before it derives nothing`() {
        assertNull(extract(": pode mandar?"))
    }

    @Test
    fun `a timestamp-looking title is not treated as a name`() {
        assertNull(extract("12:45"))
    }

    // --- the original is never lost ---

    @Test
    fun `the extraction reports the title it started from`() {
        val intention = extract("Maria: pode mandar o contrato?")

        assertEquals("Maria: pode mandar o contrato?", intention?.rawTitle)
    }
}
