package tech.benedicta.will.task

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskSearchWithMeaningTest {

    private var asked = 0

    private fun task(id: Long, title: String, completedAt: Long? = null) = Task(
        id = id,
        title = title,
        text = "",
        sourcePackage = "com.whatsapp",
        createdAt = 0L,
        completedAt = completedAt
    )

    private val feed = listOf(
        task(1, "pagar o boleto do Itaú"),
        task(2, "responder Maria"),
        task(3, "comprar pão", completedAt = 9L)
    )

    private fun search(answer: List<Task>) = TaskSearchWithMeaning(
        TaskMatcher(),
        SemanticTaskSearch { _, _ -> asked++; answer }
    )

    @Test
    fun `a word that matches is answered without the model`() = runTest {
        val found = search(listOf(task(2, "responder Maria"))).filter(feed, "boleto")

        assertEquals(listOf(1L), found.map { it.id })
        assertEquals("the matcher answered, so inference is not worth paying for", 0, asked)
    }

    @Test
    fun `a word that matches nothing goes to the model`() = runTest {
        val found = search(listOf(task(1, "pagar o boleto do Itaú"))).filter(feed, "banco")

        assertEquals(listOf(1L), found.map { it.id })
        assertEquals(1, asked)
    }

    @Test
    fun `the model cannot name a task that is not in the feed`() = runTest {
        val found = search(listOf(task(99, "something nobody wrote"))).filter(feed, "banco")

        assertTrue(found.isEmpty())
    }

    @Test
    fun `the model never sees a finished task, and cannot return one`() = runTest {
        var offered = emptyList<Task>()
        val search = TaskSearchWithMeaning(
            TaskMatcher(),
            SemanticTaskSearch { _, tasks -> offered = tasks; listOf(task(3, "comprar pão")) }
        )

        val found = search.filter(feed, "padaria")

        assertTrue("a finished task has its own screen", offered.none { it.id == 3L })
        assertTrue(found.isEmpty())
    }

    @Test
    fun `a broken model leaves the search exactly as it was`() = runTest {
        val broken = TaskSearchWithMeaning(
            TaskMatcher(),
            SemanticTaskSearch { _, _ -> error("AICore fell over") }
        )

        assertTrue(broken.filter(feed, "banco").isEmpty())
        assertEquals(listOf(1L), broken.filter(feed, "boleto").map { it.id })
    }

    @Test
    fun `with no model at all the search behaves as it always did`() = runTest {
        val plain = TaskSearchWithMeaning(TaskMatcher())

        assertEquals(listOf(1L), plain.filter(feed, "boleto").map { it.id })
        assertTrue(plain.filter(feed, "banco").isEmpty())
    }

    @Test
    fun `an empty query finds nothing rather than everything`() = runTest {
        val search = search(feed)

        assertTrue(search.filter(feed, "").isEmpty())
        assertTrue(search.filter(feed, "   ").isEmpty())
        assertEquals("a blank box is not a question", 0, asked)
    }

    @Test
    fun `the model cannot hand back more than the box shows`() = runTest {
        val many = (1L..40L).map { task(it, "task $it") }
        val search = TaskSearchWithMeaning(TaskMatcher(), SemanticTaskSearch { _, _ -> many })

        assertEquals(TaskMatcher.MAX_RESULTS, search.filter(many, "zzzz").size)
    }
}
