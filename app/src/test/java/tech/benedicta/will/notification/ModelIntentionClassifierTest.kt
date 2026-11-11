package tech.benedicta.will.notification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryModelTriageLog

class ModelIntentionClassifierTest {

    private val log = MemoryModelTriageLog()
    private var asked = 0

    private fun model(noise: Boolean) = SemanticNoiseReader { _, _ ->
        asked++
        noise
    }

    private fun classifier(
        model: SemanticNoiseReader,
        cheap: IntentionClassifier = IntentionClassifier { _, _, _ -> true },
        enabled: Boolean = true
    ) = ModelIntentionClassifier(
        cheap = cheap,
        model = model,
        log = log,
        enabled = { enabled },
        now = { 42L }
    )

    private suspend fun judge(
        classifier: IntentionClassifier,
        title: String = "Veja as novidades",
        pkg: String = "com.shop"
    ) = classifier.isIntention(title, "", pkg)

    @Test
    fun `the model can hide what the patterns missed, which is the whole point`() = runTest {
        assertFalse(judge(classifier(model(noise = true))))
    }

    @Test
    fun `anything the model keeps reaches the feed`() = runTest {
        assertTrue(judge(classifier(model(noise = false))))
    }

    @Test
    fun `what the cheap rules already rejected never reaches the model`() = runTest {
        val alwaysNoise = IntentionClassifier { _, _, _ -> false }

        assertFalse(judge(classifier(model(noise = false), cheap = alwaysNoise)))
        assertEquals("inference is not worth paying for a decided answer", 0, asked)
    }

    @Test
    fun `with the switch off the model is never asked and nothing is hidden`() = runTest {
        assertTrue(judge(classifier(model(noise = true), enabled = false)))
        assertEquals(0, asked)
    }

    @Test
    fun `a model that throws keeps the notification`() = runTest {
        val broken = SemanticNoiseReader { _, _ -> error("AICore fell over") }

        assertTrue("a broken model must not take a message with it", judge(classifier(broken)))
    }

    @Test
    fun `a model that is simply absent keeps everything`() = runTest {
        assertTrue(judge(classifier(SemanticNoiseReader.Unavailable)))
    }

    // --- nothing is hidden without being listed ---

    @Test
    fun `every drop is listed before it takes effect`() = runTest {
        judge(classifier(model(noise = true)))

        assertEquals(
            listOf("Veja as novidades" to 42L),
            log.dropped().map { it.title to it.at }
        )
    }

    @Test
    fun `a notification the model kept is not listed as hidden`() = runTest {
        judge(classifier(model(noise = false)))

        assertTrue(log.dropped().isEmpty())
    }

    @Test
    fun `once the user shows one again the model never judges it again`() = runTest {
        val classifier = classifier(model(noise = true))
        assertFalse(judge(classifier))

        log.showAgain("com.shop", "Veja as novidades")
        asked = 0

        assertTrue("the user already answered this one", judge(classifier))
        assertEquals(0, asked)
    }

    @Test
    fun `the rescue follows the notification past case and accents`() = runTest {
        val classifier = classifier(model(noise = true))
        log.showAgain("com.shop", "Atualização concluída")

        assertTrue(judge(classifier, title = "atualizacao CONCLUIDA"))
    }

    @Test
    fun `the rescue is per app, so one shop cannot speak for another`() = runTest {
        val classifier = classifier(model(noise = true))
        log.showAgain("com.shop", "Veja as novidades")

        assertFalse(judge(classifier, pkg = "com.other"))
    }
}
