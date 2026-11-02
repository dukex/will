package tech.benedicta.will.agent

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import tech.benedicta.will.infrastructure.memory.MemoryTaskEventRepository
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskEventRecorder
import tech.benedicta.will.task.TaskEventType
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.task.TaskStatus
import org.junit.Test

class WillTaskFunctionsTest {

    private val tasks = MemoryTaskRepository()
    private val log = MemoryTaskEventRepository()
    private val functions = WillTaskFunctions(tasks, TaskEventRecorder(log))

    private fun params(
        title: String,
        text: String? = null,
        sourcePackage: String? = null
    ) = CreateTaskParams(title = title, text = text, sourcePackage = sourcePackage)

    @Test
    fun `a task an agent creates is marked as written by an agent`() = runTest {
        val created = functions.createTask(params("renew the passport"))

        assertEquals(TaskOrigin.AGENT.name, created.origin)
        assertEquals(TaskOrigin.AGENT, tasks.getAllTasks().first().single().origin)
    }

    @Test
    fun `creating a task returns the id needed to change it later`() = runTest {
        val created = functions.createTask(params("renew the passport"))

        assertEquals(created.id, functions.getTask(TaskIdParams(created.id)).id)
    }

    @Test
    fun `a created task starts pending and unstarted`() = runTest {
        val created = functions.createTask(params("renew the passport"))

        assertEquals(TaskStatus.PENDING.name, created.status)
        assertNull(created.startedAt)
        assertEquals(0, created.contextSwitchCount)
    }

    @Test
    fun `creating a task logs it as created`() = runTest {
        val created = functions.createTask(params("renew the passport"))

        assertEquals(
            listOf(TaskEventType.CREATED),
            log.eventsForTask(created.id).first().map { it.type }
        )
    }

    @Test
    fun `a title with no content is refused instead of creating an empty task`() = runTest {
        listOf("", "   ", "\n\t").forEach { blank ->
            val error = runCatching { functions.createTask(params(blank)) }.exceptionOrNull()
            assertTrue(
                "blank title <$blank> must be refused, got $error",
                error is BlankTaskTitle
            )
        }
        assertTrue(tasks.getAllTasks().first().isEmpty())
    }

    @Test
    fun `the title and text arrive as written, only trimmed`() = runTest {
        val created = functions.createTask(
            params(title = "  pagar boleto R$ 1.234,56  ", text = "  até 05/11  ")
        )

        assertEquals("pagar boleto R$ 1.234,56", created.title)
        assertEquals("até 05/11", created.text)
    }

    @Test
    fun `a task with no app named gets the no-target source package`() = runTest {
        val created = functions.createTask(params("think about it"))

        assertEquals(TaskOrigin.MANUAL_SOURCE_PACKAGE, created.sourcePackage)
    }

    @Test
    fun `a blank app name is treated as no app rather than stored`() = runTest {
        val created = functions.createTask(params("think about it", sourcePackage = "   "))

        assertEquals(TaskOrigin.MANUAL_SOURCE_PACKAGE, created.sourcePackage)
    }

    @Test
    fun `a named app is kept so tapping the task opens it`() = runTest {
        val created = functions.createTask(params("reply to Ana", sourcePackage = "com.slack"))

        assertEquals("com.slack", created.sourcePackage)
    }

    @Test
    fun `listing open tasks leaves finished ones out`() = runTest {
        val open = functions.createTask(params("still to do"))
        val done = functions.createTask(params("already done"))
        functions.completeTask(TaskIdParams(done.id))

        val listed = functions.listOpenTasks().tasks

        assertEquals(listOf(open.id), listed.map { it.id })
    }

    @Test
    fun `open tasks come back most recent first`() = runTest {
        tasks.insertTask(Task(title = "older", text = "", sourcePackage = "p", createdAt = 1_000L))
        tasks.insertTask(Task(title = "newer", text = "", sourcePackage = "p", createdAt = 2_000L))

        val listed = functions.listOpenTasks().tasks

        assertEquals(listOf("newer", "older"), listed.map { it.title })
    }

    @Test
    fun `listing with nothing open comes back empty rather than failing`() = runTest {
        assertTrue(functions.listOpenTasks().tasks.isEmpty())
    }

    @Test
    fun `completing a task marks it done and logs the completion`() = runTest {
        val created = functions.createTask(params("renew the passport"))

        val completed = functions.completeTask(TaskIdParams(created.id))

        assertEquals(TaskStatus.DONE.name, completed.status)
        assertTrue(
            log.eventsForTask(created.id).first().any { it.type == TaskEventType.COMPLETED }
        )
    }

    @Test
    fun `completing a task releases its conversation so the chat can come back`() = runTest {
        val id = tasks.insertTask(
            Task(
                title = "Maria",
                text = "Hi",
                sourcePackage = "com.whatsapp",
                createdAt = 1_000L,
                conversationKey = "com.whatsapp:maria"
            )
        )

        functions.completeTask(TaskIdParams(id))

        assertNull(tasks.getAllTasks().first().single().conversationKey)
    }

    @Test
    fun `reading a task reports what the user has done with it`() = runTest {
        val id = tasks.insertTask(
            Task(
                title = "Sarah",
                text = "msg",
                sourcePackage = "com.slack",
                createdAt = 1_000L,
                startedAt = 2_000L,
                contextSwitchCount = 3
            )
        )

        val read = functions.getTask(TaskIdParams(id))

        assertEquals(TaskStatus.IN_PROGRESS.name, read.status)
        assertEquals(2_000L, read.startedAt)
        assertEquals(3, read.contextSwitchCount)
    }

    @Test
    fun `an unknown id is reported as not found instead of failing silently`() = runTest {
        val onRead = runCatching { functions.getTask(TaskIdParams(404L)) }.exceptionOrNull()
        val onComplete = runCatching { functions.completeTask(TaskIdParams(404L)) }.exceptionOrNull()

        assertTrue("reading, got $onRead", onRead is UnknownTask)
        assertTrue("completing, got $onComplete", onComplete is UnknownTask)
    }
}
