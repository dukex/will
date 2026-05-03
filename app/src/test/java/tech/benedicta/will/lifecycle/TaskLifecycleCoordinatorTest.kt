package tech.benedicta.will.lifecycle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.task.Task

private const val LAUNCHER = "tech.benedicta.will"
private const val SLACK = "com.slack"
private const val INSTAGRAM = "com.instagram.android"
private const val SPOTIFY = "com.spotify.music"

private fun task(
    id: Long = 1L,
    sourcePackage: String = SLACK,
    startedAt: Long? = null
) = Task(
    id = id,
    title = "msg",
    text = "body",
    sourcePackage = sourcePackage,
    createdAt = 1_000L,
    startedAt = startedAt
)

class TaskLifecycleCoordinatorTest {

    @Test
    fun `no active task means no events`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)

        val events = coordinator.onForegroundChange(SLACK, activeTask = null)

        assertTrue(events.isEmpty())
    }

    @Test
    fun `entering the active task source app for the first time emits StartTask`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER, now = { 5_000L })
        val active = task(startedAt = null)

        val events = coordinator.onForegroundChange(SLACK, activeTask = active)

        assertEquals(
            listOf(LifecycleEvent.StartTask(taskId = 1L, startedAt = 5_000L)),
            events
        )
    }

    @Test
    fun `entering the active task source app when already started emits nothing`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER, now = { 5_000L })
        val active = task(startedAt = 4_000L)

        val events = coordinator.onForegroundChange(SLACK, activeTask = active)

        assertTrue(events.isEmpty())
    }

    @Test
    fun `switching from active source to a non-launcher app increments context switch`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = 4_000L)

        coordinator.onForegroundChange(SLACK, activeTask = active)
        val events = coordinator.onForegroundChange(INSTAGRAM, activeTask = active)

        assertEquals(
            listOf(LifecycleEvent.IncrementContextSwitch(taskId = 1L)),
            events
        )
    }

    @Test
    fun `returning to launcher is not a context switch`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = 4_000L)

        coordinator.onForegroundChange(SLACK, activeTask = active)
        val events = coordinator.onForegroundChange(LAUNCHER, activeTask = active)

        assertTrue(events.isEmpty())
    }

    @Test
    fun `Slack to Will to Slack is not a context switch`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = 4_000L)

        coordinator.onForegroundChange(SLACK, activeTask = active)
        coordinator.onForegroundChange(LAUNCHER, activeTask = active)
        val events = coordinator.onForegroundChange(SLACK, activeTask = active)

        assertTrue(events.isEmpty())
    }

    @Test
    fun `Slack to Will to Instagram counts one context switch`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = 4_000L)

        coordinator.onForegroundChange(SLACK, activeTask = active)
        coordinator.onForegroundChange(LAUNCHER, activeTask = active)
        val events = coordinator.onForegroundChange(INSTAGRAM, activeTask = active)

        assertEquals(
            listOf(LifecycleEvent.IncrementContextSwitch(taskId = 1L)),
            events
        )
    }

    @Test
    fun `each new non-source non-launcher app counts as a separate context switch`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = 4_000L)

        coordinator.onForegroundChange(SLACK, activeTask = active)
        val first = coordinator.onForegroundChange(INSTAGRAM, activeTask = active)
        val second = coordinator.onForegroundChange(SPOTIFY, activeTask = active)

        assertEquals(listOf(LifecycleEvent.IncrementContextSwitch(taskId = 1L)), first)
        assertEquals(listOf(LifecycleEvent.IncrementContextSwitch(taskId = 1L)), second)
    }

    @Test
    fun `duplicate foreground events for the same package emit no events`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = 4_000L)

        coordinator.onForegroundChange(SLACK, activeTask = active)
        val duplicate = coordinator.onForegroundChange(SLACK, activeTask = active)
        coordinator.onForegroundChange(INSTAGRAM, activeTask = active)
        val instagramDuplicate = coordinator.onForegroundChange(INSTAGRAM, activeTask = active)

        assertTrue(duplicate.isEmpty())
        assertTrue(instagramDuplicate.isEmpty())
    }

    @Test
    fun `arriving at launcher first then opening source still emits StartTask`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER, now = { 9_000L })
        val active = task(startedAt = null)

        coordinator.onForegroundChange(LAUNCHER, activeTask = active)
        val events = coordinator.onForegroundChange(SLACK, activeTask = active)

        assertEquals(
            listOf(LifecycleEvent.StartTask(taskId = 1L, startedAt = 9_000L)),
            events
        )
    }

    @Test
    fun `direct jump from launcher to a non-source non-launcher app is not a context switch`() {
        // The user has not engaged with the active task yet; jumping straight to
        // an unrelated app should not be counted as a switch *away* from the
        // task, because the task was never started.
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = null)

        coordinator.onForegroundChange(LAUNCHER, activeTask = active)
        val events = coordinator.onForegroundChange(INSTAGRAM, activeTask = active)

        assertTrue(events.isEmpty())
    }

    @Test
    fun `reset clears the last-seen foreground so the next non-source app is not a switch`() {
        val coordinator = TaskLifecycleCoordinator(launcherPackage = LAUNCHER)
        val active = task(startedAt = 4_000L)

        coordinator.onForegroundChange(SLACK, activeTask = active)
        coordinator.reset()
        val events = coordinator.onForegroundChange(INSTAGRAM, activeTask = active)

        assertTrue(events.isEmpty())
    }
}
