package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The same shape as `SemanticSearchPrompt` for apps: the tasks are numbered
 * and the answer comes back as numbers, so a reply can only ever name a task
 * the user already has. A model that invents a task, lists everything or
 * answers in prose yields nothing or a bounded subset, never a row nobody
 * wrote.
 */
class TaskSearchPromptTest {

    private fun task(id: Long, title: String, text: String = "") = Task(
        id = id,
        title = title,
        text = text,
        sourcePackage = "com.whatsapp",
        createdAt = 0L
    )

    private val open = listOf(
        task(1, "pagar o boleto do Itaú"),
        task(2, "responder Maria", "pode revisar o contrato?"),
        task(3, "comprar pão")
    )

    @Test
    fun `the prompt numbers the tasks and asks for numbers`() {
        val prompt = TaskSearchPrompt.build("banco", open)!!

        assertTrue(prompt.contains("banco"))
        assertTrue(prompt.contains("1. pagar o boleto do Itaú"))
        assertTrue(prompt.contains("3. comprar pão"))
    }

    @Test
    fun `an empty query or an empty feed is not worth asking about`() {
        assertNull(TaskSearchPrompt.build("", open))
        assertNull(TaskSearchPrompt.build("   ", open))
        assertNull(TaskSearchPrompt.build("banco", emptyList()))
    }

    @Test
    fun `the answer names tasks the user already has`() {
        assertEquals(listOf(1L), TaskSearchPrompt.parse("1", open).map { it.id })
        assertEquals(listOf(1L, 3L), TaskSearchPrompt.parse("1, 3", open).map { it.id })
    }

    @Test
    fun `a number nobody offered is discarded rather than guessed at`() {
        assertTrue(TaskSearchPrompt.parse("9", open).isEmpty())
        assertTrue(TaskSearchPrompt.parse("0", open).isEmpty())
    }

    @Test
    fun `a negative number is not read as a choice`() {
        assertTrue(TaskSearchPrompt.parse("-1", open).isEmpty())
    }

    @Test
    fun `prose with no numbers in it yields nothing`() {
        assertTrue(TaskSearchPrompt.parse("I think they meant the bank one", open).isEmpty())
    }

    @Test
    fun `the same number twice is one result`() {
        assertEquals(listOf(1L), TaskSearchPrompt.parse("1 1 1", open).map { it.id })
    }

    @Test
    fun `the model cannot hand back the whole feed`() {
        val many = (1L..40L).map { task(it, "task $it") }
        val everything = (1..40).joinToString(" ")

        assertTrue(TaskSearchPrompt.parse(everything, many).size <= TaskSearchPrompt.MAX_RESULTS)
    }

    @Test
    fun `a long feed is cut rather than the request failing`() {
        val many = (1L..200L).map { task(it, "task $it") }

        val prompt = TaskSearchPrompt.build("qualquer coisa", many)!!

        assertTrue(prompt.contains("${TaskSearchPrompt.MAX_TASKS}. task ${TaskSearchPrompt.MAX_TASKS}"))
        assertTrue(
            "a cut list must not offer a number it will then refuse",
            !prompt.contains("${TaskSearchPrompt.MAX_TASKS + 1}. task ${TaskSearchPrompt.MAX_TASKS + 1}")
        )
    }

    @Test
    fun `a number past the cut list is refused, so the cut cannot mislead`() {
        val many = (1L..200L).map { task(it, "task $it") }

        val found = TaskSearchPrompt.parse("${TaskSearchPrompt.MAX_TASKS + 5}", many)

        assertTrue(found.isEmpty())
    }

    @Test
    fun `what the message said is offered too, because that is where the word may be`() {
        val prompt = TaskSearchPrompt.build("contrato", open)!!

        assertTrue(
            "the task reads 'responder Maria'; only the message mentions a contract",
            prompt.contains("pode revisar o contrato?")
        )
    }
}
