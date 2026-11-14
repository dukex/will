package tech.benedicta.will.notification

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryModelTriageLog
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin

/**
 * ML Kit's GenAI APIs only run while the app is the top foreground app, and a
 * call from anywhere else comes back `BACKGROUND_USE_BLOCKED`. A foreground
 * service does not get around it. `WillNotificationListenerService` is a
 * background service, so asking the model as a notification arrives was never
 * going to work on a real phone.
 *
 * So the model's part of the work happens here instead, when Will is open and
 * is the foreground app. The notification path keeps doing everything that
 * needs no model, which is most of it, and this catches up afterwards.
 */
class ModelReviewPassTest {

    private val tasks = MemoryTaskRepository()
    private val log = MemoryModelTriageLog()
    private var askedTriage = 0
    private var askedReader = 0

    private fun pass(
        noise: Boolean = false,
        read: ReadIntention? = null,
        triageEnabled: Boolean = true,
        readingEnabled: Boolean = true
    ) = ModelReviewPass(
        tasks = tasks,
        triage = { _, _ -> askedTriage++; noise },
        reader = { _, _ -> askedReader++; read },
        log = log,
        phrase = { kind, subject -> "${kind.name.lowercase()} $subject" },
        triageEnabled = { triageEnabled },
        readingEnabled = { readingEnabled },
        now = { 5_000L }
    )

    private suspend fun aNotificationTask(
        title: String = "Agenda",
        text: String = "Confirme sua presença",
        createdAt: Long = 1_000L,
        origin: TaskOrigin = TaskOrigin.NOTIFICATION,
        rawTitle: String? = null,
        completedAt: Long? = null
    ): Long = tasks.insertTask(
        Task(
            title = title,
            text = text,
            sourcePackage = "com.calendar",
            createdAt = createdAt,
            origin = origin,
            rawTitle = rawTitle,
            completedAt = completedAt
        )
    )

    private suspend fun runPass(pass: ModelReviewPass, since: Long = 0L) =
        pass.review(tasks.getAllTasks().first(), since)

    // --- reading ---

    @Test
    fun `a task the category could not read gets its title from the model`() = runTest {
        val id = aNotificationTask()

        runPass(pass(read = ReadIntention(IntentionKind.CONFIRM, "sua presença")))

        val task = tasks.getAllTasks().first().single { it.id == id }
        assertEquals("confirm sua presença", task.title)
        assertEquals("the original is never lost", "Agenda", task.rawTitle)
    }

    @Test
    fun `a title the category already derived is left alone`() = runTest {
        aNotificationTask(title = "responder Maria", rawTitle = "Maria")

        runPass(pass(read = ReadIntention(IntentionKind.PAY, "Maria")))

        assertEquals("responder Maria", tasks.getAllTasks().first().single().title)
        assertEquals("already answered, so not worth asking again", 0, askedReader)
    }

    @Test
    fun `the model declining leaves the title exactly as it arrived`() = runTest {
        aNotificationTask()

        runPass(pass(read = null))

        val task = tasks.getAllTasks().first().single()
        assertEquals("Agenda", task.title)
        assertNull(task.rawTitle)
    }

    // --- triage ---

    @Test
    fun `a task the model judges noise is removed and listed`() = runTest {
        aNotificationTask(title = "Experimente o premium")

        runPass(pass(noise = true))

        assertTrue(tasks.getAllTasks().first().isEmpty())
        assertEquals(listOf("Experimente o premium"), log.dropped().map { it.title })
    }

    @Test
    fun `a task removed by the model is never also read, because it is gone`() = runTest {
        aNotificationTask()

        runPass(pass(noise = true, read = ReadIntention(IntentionKind.PAY, "x")))

        assertEquals(0, askedReader)
    }

    @Test
    fun `a shape the user rescued is never judged again`() = runTest {
        aNotificationTask(title = "Seu pedido chegou")
        log.showAgain("com.calendar", "Seu pedido chegou")

        runPass(pass(noise = true))

        assertEquals(1, tasks.getAllTasks().first().size)
        assertEquals(0, askedTriage)
    }

    // --- what the pass must never touch ---

    @Test
    fun `a task the user typed is never sent to the model`() = runTest {
        aNotificationTask(origin = TaskOrigin.MANUAL, title = "pagar o boleto")

        runPass(pass(noise = true, read = ReadIntention(IntentionKind.PAY, "o boleto")))

        assertEquals("pagar o boleto", tasks.getAllTasks().first().single().title)
        assertEquals(0, askedTriage)
        assertEquals(0, askedReader)
    }

    @Test
    fun `a task an agent or a share created is left alone too`() = runTest {
        aNotificationTask(origin = TaskOrigin.SHARED)
        aNotificationTask(origin = TaskOrigin.AGENT)

        runPass(pass(noise = true))

        assertEquals(2, tasks.getAllTasks().first().size)
        assertEquals(0, askedTriage)
    }

    @Test
    fun `a finished task is not worth the inference`() = runTest {
        aNotificationTask(completedAt = 2_000L)

        runPass(pass(noise = true))

        assertEquals(1, tasks.getAllTasks().first().size)
        assertEquals(0, askedTriage)
    }

    @Test
    fun `anything reviewed on an earlier pass is not reviewed again`() = runTest {
        aNotificationTask(createdAt = 1_000L)

        runPass(pass(noise = true), since = 2_000L)

        assertEquals(1, tasks.getAllTasks().first().size)
        assertEquals(0, askedTriage)
    }

    @Test
    fun `the pass reports a watermark so the next one starts after it`() = runTest {
        aNotificationTask(createdAt = 1_000L)

        val watermark = runPass(pass())

        assertEquals(5_000L, watermark)
    }

    // --- the switches ---

    @Test
    fun `with both switches off nothing is asked and nothing changes`() = runTest {
        aNotificationTask()

        runPass(pass(noise = true, read = ReadIntention(IntentionKind.PAY, "x"),
            triageEnabled = false, readingEnabled = false))

        assertEquals("Agenda", tasks.getAllTasks().first().single().title)
        assertEquals(0, askedTriage)
        assertEquals(0, askedReader)
    }

    @Test
    fun `reading can be on while hiding stays off`() = runTest {
        aNotificationTask()

        runPass(pass(noise = true, read = ReadIntention(IntentionKind.CONFIRM, "sua presença"),
            triageEnabled = false))

        val task = tasks.getAllTasks().first().single()
        assertEquals("confirm sua presença", task.title)
        assertEquals("hiding is off, so nothing may be hidden", 0, askedTriage)
    }

    @Test
    fun `a model that throws leaves every task exactly as it was`() = runTest {
        aNotificationTask()
        val breaking = ModelReviewPass(
            tasks = tasks,
            triage = { _, _ -> error("BACKGROUND_USE_BLOCKED") },
            reader = { _, _ -> error("BACKGROUND_USE_BLOCKED") },
            log = log,
            phrase = { _, subject -> subject },
            triageEnabled = { true },
            readingEnabled = { true },
            now = { 5_000L }
        )

        runPass(breaking)

        assertEquals("Agenda", tasks.getAllTasks().first().single().title)
        assertFalse(log.dropped().isNotEmpty())
    }
}
