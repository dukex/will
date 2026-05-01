package tech.benedicta.will.debug

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryPinnedAppsRepository
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository
import tech.benedicta.will.task.Task

class DemoSeederTest {

    private val now = 1_730_000_000_000L
    private val seeder by lazy {
        DemoSeeder(taskRepo, pinnedRepo, nowProvider = { now })
    }
    private val taskRepo = MemoryTaskRepository()
    private val pinnedRepo = MemoryPinnedAppsRepository()

    @Test
    fun `seed inserts every task from the lucas morning seed`() = runTest {
        seeder.seed()

        val tasks = taskRepo.getAllTasks().first()
        assertEquals(10, tasks.size)
    }

    @Test
    fun `seed wipes existing tasks first`() = runTest {
        taskRepo.insertTask(
            Task(title = "stale", text = "old", sourcePackage = "x", createdAt = 1L)
        )

        seeder.seed()

        val tasks = taskRepo.getAllTasks().first()
        assertTrue(
            "expected no task with title 'stale', got ${tasks.map { it.title }}",
            tasks.none { it.title == "stale" }
        )
    }

    @Test
    fun `seed pins exactly the seed packages and clears prior pins`() = runTest {
        pinnedRepo.pin("com.example.stale")

        seeder.seed()

        val pinned = pinnedRepo.getPinnedPackages().first()
        assertEquals(buildLucasMorningSeed(now).pinnedPackages, pinned)
    }

    @Test
    fun `seed preserves the open task conversation keys for dedup`() = runTest {
        seeder.seed()

        val tasks = taskRepo.getAllTasks().first()
        val openConversationKeys = tasks
            .filter { it.completedAt == null }
            .mapNotNull { it.conversationKey }

        assertEquals(5, openConversationKeys.size)
        assertEquals(openConversationKeys.toSet().size, openConversationKeys.size)
    }

    @Test
    fun `seed preserves notificationKey on open tasks for deep-link tap`() = runTest {
        seeder.seed()

        val tasks = taskRepo.getAllTasks().first()
        tasks.filter { it.completedAt == null }.forEach { task ->
            assertNotNull(
                "${task.title}: open task should preserve notificationKey",
                task.notificationKey
            )
        }
    }

    @Test
    fun `seed populates startedAt and completedAt for the dashboard`() = runTest {
        seeder.seed()

        val tasks = taskRepo.getAllTasks().first()
        val completed = tasks.filter { it.completedAt != null }

        assertEquals(5, completed.size)
        completed.forEach { task ->
            assertNotNull("${task.title}: missing startedAt", task.startedAt)
            assertTrue(
                "${task.title}: startedAt should precede completedAt",
                task.startedAt!! < task.completedAt!!
            )
        }
    }

    @Test
    fun `seed is idempotent — running twice leaves the same final state`() = runTest {
        seeder.seed()
        val firstTasks = taskRepo.getAllTasks().first()
        val firstPins = pinnedRepo.getPinnedPackages().first()

        seeder.seed()
        val secondTasks = taskRepo.getAllTasks().first()
        val secondPins = pinnedRepo.getPinnedPackages().first()

        assertEquals(firstTasks.size, secondTasks.size)
        assertEquals(firstPins, secondPins)
    }
}
