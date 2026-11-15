package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Triage is the one judgement where being wrong is expensive, so the parser
 * is biased the way the product is: only an unambiguous verdict of noise
 * drops anything. Silence, prose, a refusal, a hedge, an unreadable answer
 * and an outright error all keep the notification.
 *
 * `IntentionPrompt` asks a different question and its NONE is not this
 * answer: a bank balance asks the user for nothing and is still not noise.
 */
class TriagePromptTest {

    private fun verdict(answer: String) = TriagePrompt.parse(answer)

    @Test
    fun `the prompt carries what the notification said`() {
        val prompt = TriagePrompt.build("Spotify", "Baixando música")!!

        assertTrue(prompt.contains("Spotify"))
        assertTrue(prompt.contains("Baixando música"))
    }

    @Test
    fun `a notification with nothing in it is not worth asking about`() {
        assertEquals(null, TriagePrompt.build("", "   "))
    }

    @Test
    fun `an unambiguous verdict of noise is the only thing that drops`() {
        assertTrue(verdict("NOISE"))
        assertTrue(verdict("  noise  "))
        assertTrue(verdict("Answer: NOISE"))
    }

    @Test
    fun `a verdict of keep keeps`() {
        assertFalse(verdict("KEEP"))
    }

    @Test
    fun `silence keeps, because the model said nothing`() {
        assertFalse(verdict(""))
        assertFalse(verdict("   "))
    }

    @Test
    fun `prose keeps, because it decided nothing`() {
        assertFalse(verdict("This looks like it might be promotional content"))
    }

    @Test
    fun `an answer naming both verdicts keeps`() {
        assertFalse(verdict("NOISE or KEEP"))
        assertFalse(verdict("KEEP, definitely not NOISE"))
    }

    @Test
    fun `a word that merely contains the verdict does not count as one`() {
        assertFalse(verdict("NOISELESS"))
        assertFalse(verdict("the signal-to-NOISEband ratio"))
    }

    @Test
    fun `a refusal keeps`() {
        assertFalse(verdict("I cannot help with that"))
        assertFalse(verdict("NONE"))
    }

    @Test
    fun `the default on anything unexpected is to keep`() {
        listOf("\u0000", "🙂", "null", "undefined", "{\"verdict\":\"noise\"}").forEach { answer ->
            assertFalse("`$answer` must not drop a notification", verdict(answer))
        }
    }
}
