package tech.benedicta.will.lifecycle

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryTaskEventRepository
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskEventRecorder
import tech.benedicta.will.task.TaskEventType

private const val LAUNCHER = "tech.benedicta.will"
private const val SLACK = "com.slack"
private const val INSTAGRAM = "com.instagram.android"
private const val TWITTER = "com.twitter.android"

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
            repository = repo,
            events = TaskEventRecorder(MemoryTaskEventRepository())
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
            repository = repo,
            events = TaskEventRecorder(MemoryTaskEventRepository())
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
            repository = repo,
            events = TaskEventRecorder(MemoryTaskEventRepository())
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
            repository = repo,
            events = TaskEventRecorder(MemoryTaskEventRepository())
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


    @Test
    fun `entering the source app writes a STARTED event stamped with the same time`() = runTest {
        val repo = MemoryTaskRepository()
        val log = MemoryTaskEventRepository()
        val taskId = repo.insertTask(
            Task(title = "Sarah", text = "msg", sourcePackage = SLACK, createdAt = 1_000L)
        )
        var active: Task? = repo.getAllTasks().first().first { it.id == taskId }
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER, now = { 5_000L }),
            activeTaskProvider = { active },
            repository = repo,
            events = TaskEventRecorder(log)
        )

        applier.onForegroundChange(SLACK)

        val event = log.eventsForTask(taskId).first().single()
        assertEquals(TaskEventType.STARTED, event.type)
        assertEquals(5_000L, event.at)
        assertEquals(repo.getAllTasks().first().first { it.id == taskId }.startedAt, event.at)
    }

    @Test
    fun `every context switch is logged with the package the user jumped to`() = runTest {
        val repo = MemoryTaskRepository()
        val log = MemoryTaskEventRepository()
        val taskId = repo.insertTask(
            Task(
                title = "Sarah",
                text = "msg",
                sourcePackage = SLACK,
                createdAt = 1_000L,
                startedAt = 5_000L
            )
        )
        val active: Task? = repo.getAllTasks().first().first { it.id == taskId }
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER),
            activeTaskProvider = { active },
            repository = repo,
            events = TaskEventRecorder(log)
        )

        applier.onForegroundChange(SLACK)
        applier.onForegroundChange(INSTAGRAM)
        applier.onForegroundChange(SLACK)
        applier.onForegroundChange(TWITTER)

        val switches = log.eventsForTask(taskId).first()
            .filter { it.type == TaskEventType.SWITCHED_AWAY }
        assertEquals(listOf(INSTAGRAM, TWITTER), switches.map { it.foregroundPackage })
    }

    @Test
    fun `returning to the launcher is not logged as a switch`() = runTest {
        val repo = MemoryTaskRepository()
        val log = MemoryTaskEventRepository()
        val taskId = repo.insertTask(
            Task(
                title = "Sarah",
                text = "msg",
                sourcePackage = SLACK,
                createdAt = 1_000L,
                startedAt = 5_000L
            )
        )
        val active: Task? = repo.getAllTasks().first().first { it.id == taskId }
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER),
            activeTaskProvider = { active },
            repository = repo,
            events = TaskEventRecorder(log)
        )

        applier.onForegroundChange(SLACK)
        applier.onForegroundChange(LAUNCHER)

        assertTrue(log.eventsForTask(taskId).first().isEmpty())
    }

    @Test
    fun `no active task writes nothing to the log`() = runTest {
        val repo = MemoryTaskRepository()
        val log = MemoryTaskEventRepository()
        repo.insertTask(Task(title = "Sarah", text = "m", sourcePackage = SLACK, createdAt = 1_000L))
        val applier = TaskLifecycleApplier(
            coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER),
            activeTaskProvider = { null },
            repository = repo,
            events = TaskEventRecorder(log)
        )

        applier.onForegroundChange(SLACK)
        applier.onForegroundChange(INSTAGRAM)

        assertTrue(log.eventsSince(0L).first().isEmpty())
    }
}
