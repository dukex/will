package tech.benedicta.will.notification

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import tech.benedicta.will.launcher.GeminiNanoAppSearch
import tech.benedicta.will.launcher.ModelAvailability

/**
 * Whether Gemini Nano answers *well*, which is the one question no JVM can
 * settle. Everything else about the model surface is checked by
 * `./gradlew test`: the prompts, the parsing, the composition, and that every
 * failure path keeps the notification.
 *
 * This asks the real model real questions and checks the answers are the
 * obvious ones. It is written down so that checking it on a phone is one
 * command rather than someone remembering to look:
 *
 *     ./gradlew connectedDebugAndroidTest
 *
 * Every test skips on a phone without AICore rather than failing, because
 * "this device cannot serve the model" is a legitimate state that the rest of
 * the app is built to handle.
 *
 * If any of these fail on a device that does have the model, the prompts need
 * work before either switch is worth offering. That is the point of asserting
 * on the obvious cases rather than the hard ones: these are the floor.
 */
@RunWith(AndroidJUnit4::class)
class ModelAnswersWellTest {

    private fun requireModel() {
        assumeTrue("needs API 26", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        val availability = runBlocking { GeminiNanoAppSearch(enabled = { true }).availability() }
        assumeTrue(
            "this phone cannot serve Gemini Nano, which the app is built to handle",
            availability == ModelAvailability.AVAILABLE
        )
    }

    // --- triage: does it recognise housekeeping? ---

    /**
     * The verdict, and what the model actually said to reach it.
     *
     * A bare assertion failure here would say "expected true, got false",
     * which tells whoever ran it nothing about whether the prompt was
     * misread, the model hedged, or it answered in a shape the parser
     * refuses. Every failure message carries the raw reply instead.
     */
    private fun judged(title: String, text: String = ""): Pair<Boolean, String> {
        requireModel()
        val prompt = TriagePrompt.build(title, text) ?: return false to "(nothing to ask)"
        val raw = runBlocking { rawAnswer(prompt) }
        return TriagePrompt.parse(raw) to raw
    }

    private suspend fun rawAnswer(prompt: String): String = runCatching {
        Generation.getClient()
            .generateContent(prompt)
            .candidates
            .firstOrNull()
            ?.text
            .orEmpty()
    }.getOrElse { "(the model threw: ${it.javaClass.simpleName}: ${it.message})" }

    private fun assertNoise(title: String, text: String = "", why: String) {
        val (noise, raw) = judged(title, text)
        assertTrue("$why\n  notification: \"$title\" / \"$text\"\n  model said: $raw", noise)
    }

    private fun assertNotNoise(title: String, text: String = "", why: String) {
        val (noise, raw) = judged(title, text)
        assertFalse("$why\n  notification: \"$title\" / \"$text\"\n  model said: $raw", noise)
    }

    @Test
    fun obvious_housekeeping_is_recognised_as_noise() {
        assertNoise("Download complete", why = "a download finishing is not something to act on")
    }

    @Test
    fun obvious_housekeeping_is_recognised_in_portuguese_too() {
        assertNoise("Backup concluído", why = "the patterns catch this one; the model should too")
    }

    @Test
    fun an_advert_is_recognised_as_noise() {
        assertNoise(
            "Experimente o premium por 7 dias", "oferta por tempo limitado",
            why = "an advert nobody wrote a pattern for is the whole reason for this feature"
        )
    }

    @Test
    fun a_message_from_a_person_is_never_noise() {
        assertNotNoise(
            "Maria", "pode revisar o contrato hoje?",
            why = "dropping this is the failure the whole design is arranged against"
        )
    }

    @Test
    fun something_about_the_users_own_money_is_never_noise() {
        assertNotNoise("Banco Itaú", "seu boleto vence amanhã", why = "a bill is not housekeeping")
    }

    @Test
    fun a_delivery_is_never_noise() {
        assertNotNoise("Seu pedido chegou", "deixado na portaria", why = "a delivery needs acting on")
    }

    @Test
    fun a_notification_mentioning_a_housekeeping_word_is_still_a_message() {
        assertNotNoise(
            "Tom", "alarm did not go off, running late",
            why = "this is the boundary the linear model could not learn; see US-15"
        )
    }

    // --- reading: does it name what the notification asks for? ---

    private fun read(title: String, text: String): ReadIntention? {
        requireModel()
        return runBlocking { GeminiNanoIntentions(enabled = { true }).read(title, text) }
    }

    /** What the model replied, so a failure says why rather than only that. */
    private fun rawReading(title: String, text: String): String {
        val prompt = IntentionPrompt.build(title, text) ?: return "(nothing to ask)"
        return runBlocking { rawAnswer(prompt) }
    }

    @Test
    fun an_invitation_reads_as_something_to_confirm() {
        val text = "Confirme sua presença na reunião de sexta"
        val read = read("Agenda", text)

        assertEquals(
            "model said: ${rawReading("Agenda", text)}",
            IntentionKind.CONFIRM,
            read?.kind
        )
    }

    @Test
    fun a_bill_reads_as_something_to_pay() {
        val text = "seu boleto de R$ 230 vence amanhã"
        val read = read("Banco Itaú", text)

        assertEquals(
            "model said: ${rawReading("Banco Itaú", text)}",
            IntentionKind.PAY,
            read?.kind
        )
    }

    @Test
    fun whatever_it_reads_is_named_with_words_the_notification_used() {
        val text = "Confirme sua presença na reunião de sexta"
        val read = read("Agenda", text)

        assertTrue(
            "the parser refuses an invented subject, so a non-null answer proves this",
            read == null || "$text Agenda".contains(read.subject, ignoreCase = true)
        )
    }

    @Test
    fun a_notification_asking_for_nothing_is_left_alone() {
        val text = "seu saldo hoje é R$ 1.240,00"
        val read = read("Banco", text)

        assertEquals(
            "a balance asks for nothing; the title should survive. " +
                "model said: ${rawReading("Banco", text)}",
            null,
            read
        )
    }
}
