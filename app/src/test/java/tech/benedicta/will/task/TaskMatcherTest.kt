package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMatcherTest {

    private val matcher = TaskMatcher()

    private fun task(id: Long, title: String, completedAt: Long? = null) = Task(
        id = id,
        title = title,
        text = "",
        sourcePackage = "com.whatsapp",
        createdAt = id,
        completedAt = completedAt
    )

    private val feed = listOf(
        task(1, "responder Maria"),
        task(2, "pagar o boleto da luz"),
        task(3, "revisar o contrato de aluguel"),
        task(4, "ligar de volta para Miguel"),
        task(5, "renovar o passaporte", completedAt = 10L)
    )

    private fun search(query: String) = matcher.filter(feed, query).map { it.title }

    @Test
    fun `an empty query finds nothing, because the feed is already on screen`() {
        assertTrue(matcher.filter(feed, "").isEmpty())
        assertTrue(matcher.filter(feed, "   ").isEmpty())
    }

    @Test
    fun `a word in the task finds it`() {
        assertEquals(listOf("pagar o boleto da luz"), search("boleto"))
    }

    @Test
    fun `accents do not have to be typed`() {
        assertTrue(search("contrato").contains("revisar o contrato de aluguel"))
    }

    @Test
    fun `a person's name finds the task about them`() {
        assertEquals(listOf("responder Maria"), search("maria"))
    }

    @Test
    fun `a task already finished is not in the way`() {
        assertTrue("done tasks live on their own screen", search("passaporte").isEmpty())
    }

    @Test
    fun `a task whose title starts with the query comes first`() {
        val results = search("re")

        assertEquals("responder Maria", results.first())
    }

    @Test
    fun `a query matching nothing returns nothing`() {
        assertTrue(search("zzzz").isEmpty())
    }

    @Test
    fun `half a word still finds the task`() {
        assertTrue(search("passapo").isEmpty())
        assertTrue(search("alugue").contains("revisar o contrato de aluguel"))
    }

    @Test
    fun `results are capped so the search never buries the apps`() {
        val many = (1..50).map { task(it.toLong(), "tarefa numero $it") }

        assertTrue(matcher.filter(many, "tarefa").size <= TaskMatcher.MAX_RESULTS)
    }
}
