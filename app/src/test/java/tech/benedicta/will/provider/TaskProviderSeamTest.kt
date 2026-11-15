package tech.benedicta.will.provider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin

class TaskProviderSeamTest {

    private fun task(origin: TaskOrigin, title: String = "pagar o boleto") = Task(
        id = 1L,
        title = title,
        text = "",
        sourcePackage = "manual",
        createdAt = 1_000L,
        origin = origin
    )

    @Test
    fun `a task can come from somewhere that is not this phone`() {
        assertEquals(
            "a stored PROVIDER row reads back as itself",
            TaskOrigin.PROVIDER,
            TaskOrigin.fromStored("PROVIDER")
        )
    }

    @Test
    fun `an older build renders a provider task rather than throwing`() {
        assertEquals(
            "fromStored tolerates what it does not know, which is why adding an origin is safe",
            TaskOrigin.NOTIFICATION,
            TaskOrigin.fromStored("SOMETHING_A_LATER_BUILD_KNOWS")
        )
    }

    @Test
    fun `a task that came from the user's own account may go back to it`() {
        assertEquals(
            "it is the user's own writing, already on their account",
            "pagar o boleto",
            ProviderSharing.payloadFor(task(TaskOrigin.PROVIDER))?.title
        )
    }

    @Test
    fun `a provider that is not configured is the default, and it does nothing`() = runTest {
        val provider = TaskProvider.None

        assertEquals("", provider.id)
        assertTrue(provider.openTasks().isEmpty())
        assertNull(
            "creating against no provider cannot invent a remote id",
            provider.create(ProviderPayload("pagar o boleto", isCompleted = false))
        )
    }

    @Test
    fun `nothing a provider returns is trusted to be well formed`() {
        assertNull(ProviderTask.of(remoteId = "  ", title = "pagar", isCompleted = false))
        assertNull(ProviderTask.of(remoteId = "abc", title = "   ", isCompleted = false))

        val read = ProviderTask.of(remoteId = " abc ", title = " pagar o boleto ", isCompleted = true)
        assertEquals("abc", read?.remoteId)
        assertEquals("pagar o boleto", read?.title)
        assertEquals(true, read?.isCompleted)
    }
}
