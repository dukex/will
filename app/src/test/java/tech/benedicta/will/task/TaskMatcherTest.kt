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

    // --- what the notification actually said ---

    private fun notificationTask(
        id: Long,
        title: String,
        text: String = "",
        rawTitle: String? = null
    ) = Task(
        id = id,
        title = title,
        text = text,
        sourcePackage = "com.whatsapp",
        createdAt = 0L,
        rawTitle = rawTitle
    )

    @Test
    fun `a task is found by what the message said, not only by its title`() {
        val tasks = listOf(
            notificationTask(1, "responder Maria", text = "pode mandar o boleto hoje?")
        )

        assertEquals(listOf(1L), matcher.filter(tasks, "boleto").map { it.id })
    }

    @Test
    fun `a derived title does not hide the name the notification arrived with`() {
        val tasks = listOf(
            notificationTask(1, "responder Contato", text = "oi", rawTitle = "Banco Itau")
        )

        assertEquals(
            "US-19 rewrote the title; the original is still what the user remembers",
            listOf(1L),
            matcher.filter(tasks, "itau").map { it.id }
        )
    }

    @Test
    fun `a match in the title beats a match in someone else's message`() {
        val tasks = listOf(
            notificationTask(1, "responder Ana", text = "falou do boleto ontem"),
            notificationTask(2, "pagar o boleto")
        )

        assertEquals(listOf(2L, 1L), matcher.filter(tasks, "boleto").map { it.id })
    }

    @Test
    fun `a short query does not match every task through its message`() {
        val tasks = listOf(
            notificationTask(
                1,
                "responder Maria",
                text = "bom dia, consegue revisar aquele documento que mandei na sexta?"
            )
        )

        assertTrue(
            "scattered letters inside a paragraph are not a match, or everything matches everything",
            matcher.filter(tasks, "bcs").isEmpty()
        )
    }

    @Test
    fun `scattered letters still find a short title, as they always did`() {
        val tasks = listOf(notificationTask(1, "responder Maria"))

        assertEquals(listOf(1L), matcher.filter(tasks, "rspndr").map { it.id })
    }

    @Test
    fun `an empty message is not a match for everything`() {
        val tasks = listOf(notificationTask(1, "pagar a conta", text = ""))

        assertTrue(matcher.filter(tasks, "zzz").isEmpty())
    }
}
