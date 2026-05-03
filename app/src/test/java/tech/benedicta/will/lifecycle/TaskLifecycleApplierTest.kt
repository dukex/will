package tech.benedicta.will.lifecycle

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository
import tech.benedicta.will.task.Task

private const val LAUNCHER = "tech.benedicta.will"
private const val SLACK = "com.slack"
private const val INSTAGRAM = "com.instagram.android"

class TaskLifecycleApplierTest {

    @Test
    fun `entering source app marks startedAt on the task`() = runTest {
        val repo = MemoryTaskRepository()
        val taskId = repo.insertTask(
            Task(title = "Sarah", text = "msg", sourcePackage = SLACK, createdAt = 1_000L)
        )
        var active: Task? = null
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER, now = { 5_000L }),
            activeTaskProvider = { active },
            repository = repo
        )
        active = repo.getAllTasks().first().first()

        applier.onForegroundChange(SLACK)

        val task = repo.getAllTasks().first().first { it.id == taskId }
        assertEquals(5_000L, task.startedAt)
    }

    @Test
    fun `context switches increment counter on every non-launcher non-source jump`() = runTest {
        val repo = MemoryTaskRepository()
        val taskId = repo.insertTask(
            Task(
                title = "Sarah",
                text = "msg",
                sourcePackage = SLACK,
                createdAt = 1_000L,
                startedAt = 5_000L
            )
        )
        var active: Task? = repo.getAllTasks().first().first { it.id == taskId }
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER),
            activeTaskProvider = { active },
            repository = repo
        )

        applier.onForegroundChange(SLACK)
        applier.onForegroundChange(INSTAGRAM)
        // Refresh the active task snapshot so subsequent decisions see startedAt.
        active = repo.getAllTasks().first().first { it.id == taskId }
        applier.onForegroundChange(SLACK)
        applier.onForegroundChange(INSTAGRAM)

        val task = repo.getAllTasks().first().first { it.id == taskId }
        assertEquals(2, task.contextSwitchCount)
    }

    @Test
    fun `no active task means no repository writes`() = runTest {
        val repo = MemoryTaskRepository()
        val taskId = repo.insertTask(
            Task(title = "Sarah", text = "msg", sourcePackage = SLACK, createdAt = 1_000L)
        )
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER, now = { 5_000L }),
            activeTaskProvider = { null },
            repository = repo
        )

        applier.onForegroundChange(SLACK)
        applier.onForegroundChange(INSTAGRAM)

        val task = repo.getAllTasks().first().first { it.id == taskId }
        assertEquals(null, task.startedAt)
        assertEquals(0, task.contextSwitchCount)
    }

    @Test
    fun `resetForActiveTaskChange clears the coordinator state`() = runTest {
        val repo = MemoryTaskRepository()
        val firstId = repo.insertTask(
            Task(
                title = "Sarah",
                text = "a",
                sourcePackage = SLACK,
                createdAt = 1_000L,
                startedAt = 1_500L
            )
        )
        val secondId = repo.insertTask(
            Task(
                title = "Marcus",
                text = "b",
                sourcePackage = INSTAGRAM,
                createdAt = 2_000L,
                startedAt = 2_500L
            )
        )
        var active: Task? = repo.getAllTasks().first().first { it.id == firstId }
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER),
            activeTaskProvider = { active },
            repository = repo
        )

        // user is on Slack with the first task active
        applier.onForegroundChange(SLACK)

        // active task switches to the Instagram-sourced task; reset the
        // coordinator so the SLACK→INSTAGRAM transition that follows is
        // interpreted as "first foreground sighting" rather than as a
        // context switch on a stale task
        active = repo.getAllTasks().first().first { it.id == secondId }
        applier.resetForActiveTaskChange()
        applier.onForegroundChange(INSTAGRAM)

        val first = repo.getAllTasks().first().first { it.id == firstId }
        val second = repo.getAllTasks().first().first { it.id == secondId }
        assertEquals(0, first.contextSwitchCount)
        assertEquals(0, second.contextSwitchCount)
    }
}
