package tech.benedicta.will.notification

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tech.benedicta.will.infrastructure.memory.MemoryModelTriageLog
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository
import tech.benedicta.will.task.Task

/**
 * The real `GeminiNanoTriage` and `GeminiNanoIntentions`, against the real ML
 * Kit library, on a runtime with no AICore. No stand-in for the model: this
 * is the shipped class taking the path that most phones will take.
 *
 * That path is the one that matters most. A phone without AICore is the
 * common case, and what these classes do there decides whether turning a
 * switch on can cost someone a notification. Here they must behave exactly as
 * if the feature did not exist.
 *
 * What a phone *with* AICore answers is a different question, and the only
 * one left for `ModelAnswersWellTest` on real hardware. No emulator can
 * answer it either: AICore is a system service shipped on particular phones,
 * absent from every emulator image, so this is hardware or nothing.
 */
@RunWith(RobolectricTestRunner::class)
class GeminiDegradationJvmTest {

    // --- triage: the shipped class, with no model behind it ---

    @Test
    fun `with no AICore the real triage keeps everything`() {
        val triage = GeminiNanoTriage()

        val verdicts = listOf(
            "Download complete" to "",
            "Experimente o premium" to "oferta",
            "Maria" to "pode revisar o contrato?"
        ).map { (title, text) -> runBlocking { triage.isNoise(title, text) } }

        assertTrue(
            "a classifier that cannot think must not hide anything",
            verdicts.none { it }
        )
    }

    @Test
    fun `the real triage answers rather than throwing, whatever it is handed`() {
        val triage = GeminiNanoTriage()

        listOf("" to "", "   " to "   ", "\u0000" to "🙂", "x".repeat(5_000) to "").forEach { (t, b) ->
            assertFalse("`${t.take(12)}` must come back as not-noise", runBlocking { triage.isNoise(t, b) })
        }
    }

    // --- reading: the shipped class, with no model behind it ---

    @Test
    fun `with no AICore the real reader derives nothing`() {
        val reader = GeminiNanoIntentions(enabled = { true })

        val read = runBlocking { reader.read("Agenda", "Confirme sua presença na reunião") }

        assertNull("the title must arrive as it was sent", read)
    }

    @Test
    fun `with the switch off the real reader does not even ask`() {
        val reader = GeminiNanoIntentions(enabled = { false })

        assertNull(runBlocking { reader.read("Agenda", "Confirme sua presença") })
    }

    // --- the whole path, with the shipped classes in place ---

    @Test
    fun `the notification path is unchanged on a phone without the model`() = runBlocking {
        val convert = NotificationToTask(
            processor = NotificationProcessor(),
            reader = ReadIntentionWithMeaning(
                IntentionExtractor(),
                GeminiNanoIntentions(enabled = { true })
            ),
            phrase = { _, subject -> "confirmar $subject" }
        )

        val task = convert.convert(
            category = null,
            title = "Agenda",
            text = "Confirme sua presença na reunião de sexta",
            sourcePackage = "com.calendar",
            timestamp = 1_000L
        )

        assertEquals("Agenda", task?.title)
        assertNull("nothing was derived, so nothing was replaced", task?.rawTitle)
    }

    @Test
    fun `the whole foreground pass on a phone without the model changes nothing`() = runBlocking {
        val tasks = MemoryTaskRepository()
        val log = MemoryModelTriageLog()
        tasks.insertTask(
            Task(
                title = "Experimente o premium",
                text = "oferta por tempo limitado",
                sourcePackage = "com.shop",
                createdAt = 1_000L
            )
        )
        val pass = ModelReviewPass(
            tasks = tasks,
            triage = GeminiNanoTriage(),
            reader = GeminiNanoIntentions(enabled = { true }),
            log = log,
            phrase = { _, subject -> "derived $subject" },
            triageEnabled = { true },
            readingEnabled = { true },
            now = { 5_000L }
        )

        pass.review(tasks.getAllTasks().first(), since = 0L)

        val remaining = tasks.getAllTasks().first()
        assertEquals("with both switches on and no model, nothing may be hidden", 1, remaining.size)
        assertEquals("Experimente o premium", remaining.single().title)
        assertTrue(log.dropped().isEmpty())
    }
}
