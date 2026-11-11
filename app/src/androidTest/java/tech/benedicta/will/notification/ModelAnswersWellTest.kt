package tech.benedicta.will.notification

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    private fun judge(title: String, text: String = ""): Boolean {
        requireModel()
        return runBlocking { GeminiNanoTriage().isNoise(title, text) }
    }

    @Test
    fun obvious_housekeeping_is_recognised_as_noise() {
        assertTrue("a download finishing is not something to act on", judge("Download complete"))
    }

    @Test
    fun obvious_housekeeping_is_recognised_in_portuguese_too() {
        assertTrue(judge("Backup concluído"))
    }

    @Test
    fun an_advert_is_recognised_as_noise() {
        assertTrue(judge("Experimente o premium por 7 dias", "oferta por tempo limitado"))
    }

    @Test
    fun a_message_from_a_person_is_never_noise() {
        assertFalse(
            "dropping this is the failure the whole design is arranged against",
            judge("Maria", "pode revisar o contrato hoje?")
        )
    }

    @Test
    fun something_about_the_users_own_money_is_never_noise() {
        assertFalse(judge("Banco Itaú", "seu boleto vence amanhã"))
    }

    @Test
    fun a_delivery_is_never_noise() {
        assertFalse(judge("Seu pedido chegou", "deixado na portaria"))
    }

    @Test
    fun a_notification_mentioning_a_housekeeping_word_is_still_a_message() {
        assertFalse(
            "this is the boundary the linear model could not learn; see US-15",
            judge("Tom", "alarm did not go off, running late")
        )
    }

    // --- reading: does it name what the notification asks for? ---

    private fun read(title: String, text: String): ReadIntention? {
        requireModel()
        return runBlocking { GeminiNanoIntentions(enabled = { true }).read(title, text) }
    }

    @Test
    fun an_invitation_reads_as_something_to_confirm() {
        val read = read("Agenda", "Confirme sua presença na reunião de sexta")

        assertEquals(IntentionKind.CONFIRM, read?.kind)
    }

    @Test
    fun a_bill_reads_as_something_to_pay() {
        val read = read("Banco Itaú", "seu boleto de R$ 230 vence amanhã")

        assertEquals(IntentionKind.PAY, read?.kind)
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
        val read = read("Banco", "seu saldo hoje é R$ 1.240,00")

        assertEquals("a balance asks for nothing; the title should survive", null, read)
    }
}
