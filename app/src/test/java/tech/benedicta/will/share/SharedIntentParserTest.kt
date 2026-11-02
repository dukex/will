package tech.benedicta.will.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedIntentParserTest {

    // --- share sheet (ACTION_SEND, text/plain) ---

    @Test
    fun `plain shared text becomes the title`() {
        val shared = SharedIntentParser.fromSend(subject = null, text = "pay the water bill")

        assertEquals("pay the water bill", shared?.title)
        assertEquals("", shared?.text)
    }

    @Test
    fun `a subject wins as the title and the text becomes the body`() {
        val shared = SharedIntentParser.fromSend(
            subject = "Contract review",
            text = "https://example.com/contract.pdf"
        )

        assertEquals("Contract review", shared?.title)
        assertEquals("https://example.com/contract.pdf", shared?.text)
    }

    @Test
    fun `a multi-line share puts the first line in the title and the rest in the body`() {
        val shared = SharedIntentParser.fromSend(
            subject = null,
            text = "review the lease\n\nclauses 4 and 7 look wrong"
        )

        assertEquals("review the lease", shared?.title)
        assertEquals("clauses 4 and 7 look wrong", shared?.text)
    }

    @Test
    fun `leading blank lines are skipped when picking the title`() {
        val shared = SharedIntentParser.fromSend(subject = null, text = "\n\n  call the notary\nafter 3pm")

        assertEquals("call the notary", shared?.title)
    }

    @Test
    fun `runs of whitespace inside a title collapse to single spaces`() {
        val shared = SharedIntentParser.fromSend(subject = "  Contract    review  ", text = null)

        assertEquals("Contract review", shared?.title)
    }

    @Test
    fun `collapsing whitespace in the title does not swallow the body`() {
        val shared = SharedIntentParser.fromSend(
            subject = null,
            text = "review  the   lease\n\nclauses 4 and 7 look wrong"
        )

        assertEquals("review the lease", shared?.title)
        assertEquals("clauses 4 and 7 look wrong", shared?.text)
    }

    @Test
    fun `a body longer than the title cap is truncated and kept whole in the text`() {
        val long = "a".repeat(SharedIntentParser.MAX_TITLE_LENGTH + 40)

        val shared = SharedIntentParser.fromSend(subject = null, text = long)

        assertEquals(SharedIntentParser.MAX_TITLE_LENGTH, shared?.title?.length)
        assertEquals(long, shared?.text)
    }

    @Test
    fun `a subject longer than the cap is truncated`() {
        val long = "b".repeat(SharedIntentParser.MAX_TITLE_LENGTH + 10)

        val shared = SharedIntentParser.fromSend(subject = long, text = "body")

        assertEquals(SharedIntentParser.MAX_TITLE_LENGTH, shared?.title?.length)
    }

    @Test
    fun `nothing shared creates nothing`() {
        assertNull(SharedIntentParser.fromSend(subject = null, text = null))
        assertNull(SharedIntentParser.fromSend(subject = "", text = ""))
        assertNull(SharedIntentParser.fromSend(subject = "   ", text = "  \n  "))
    }

    @Test
    fun `the shared text is never reinterpreted`() {
        val literal = "não é pra traduzir: pagar boleto R$ 1.234,56 até 05/11"

        val shared = SharedIntentParser.fromSend(subject = null, text = literal)

        assertEquals(literal, shared?.title)
    }

    // --- will://task links ---

    @Test
    fun `a will task link with a title creates that task`() {
        val shared = SharedIntentParser.fromUri("will://task?title=pay%20the%20bill")

        assertEquals("pay the bill", shared?.title)
        assertEquals("", shared?.text)
    }

    @Test
    fun `a will task link carries both title and text`() {
        val shared = SharedIntentParser.fromUri("will://task?title=Call%20Ana&text=about%20the%20lease")

        assertEquals("Call Ana", shared?.title)
        assertEquals("about the lease", shared?.text)
    }

    @Test
    fun `plus signs and encoded accents survive decoding`() {
        val shared = SharedIntentParser.fromUri("will://task?title=revisar+contrato+da+Quezia")

        assertEquals("revisar contrato da Quezia", shared?.title)
    }

    @Test
    fun `a link with only text promotes the text to the title`() {
        val shared = SharedIntentParser.fromUri("will://task?text=renovar%20o%20passaporte")

        assertEquals("renovar o passaporte", shared?.title)
    }

    @Test
    fun `query parameter order does not matter`() {
        val shared = SharedIntentParser.fromUri("will://task?text=body&title=Head")

        assertEquals("Head", shared?.title)
        assertEquals("body", shared?.text)
    }

    @Test
    fun `a link with no parameters creates nothing`() {
        assertNull(SharedIntentParser.fromUri("will://task"))
        assertNull(SharedIntentParser.fromUri("will://task?title="))
    }

    @Test
    fun `another scheme is not ours to answer`() {
        assertNull(SharedIntentParser.fromUri("https://task?title=nope"))
        assertNull(SharedIntentParser.fromUri("todoist://task?title=nope"))
    }

    @Test
    fun `another host on our scheme is not a task link`() {
        assertNull(SharedIntentParser.fromUri("will://settings?title=nope"))
    }

    @Test
    fun `the scheme and host are matched case-insensitively`() {
        val shared = SharedIntentParser.fromUri("WILL://TASK?title=Upper")

        assertEquals("Upper", shared?.title)
    }

    @Test
    fun `a malformed uri creates nothing instead of throwing`() {
        assertNull(SharedIntentParser.fromUri("will://ta sk?title=broken"))
        assertNull(SharedIntentParser.fromUri(":::"))
        assertNull(SharedIntentParser.fromUri(null))
    }

    @Test
    fun `a bare flag parameter is ignored rather than read as a value`() {
        assertNull(SharedIntentParser.fromUri("will://task?title"))
    }
}
