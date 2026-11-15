package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE

class CompletionInferenceTest {

    private val inference = CompletionInference()

    private fun task(
        id: Long = 1L,
        startedAt: Long? = null,
        completedAt: Long? = null
    ) = Task(
        id = id,
        title = "Sarah",
        text = "can you review the PR?",
        sourcePackage = "com.slack",
        createdAt = 0L,
        startedAt = startedAt,
        completedAt = completedAt
    )

    private fun event(type: TaskEventType, at: Long, taskId: Long = 1L, pkg: String? = null) =
        TaskEvent(taskId = taskId, type = type, at = at, foregroundPackage = pkg)

    /** Opened the app, stayed a few minutes, left, and nothing since. */
    private fun handledThenQuiet(taskId: Long = 1L) = listOf(
        event(TaskEventType.CREATED, 0L, taskId),
        event(TaskEventType.STARTED, 1 * MINUTE, taskId),
        event(TaskEventType.SWITCHED_AWAY, 4 * MINUTE, taskId, "tech.benedicta.will")
    )

    @Test
    fun `a task worked on and then left alone reads as probably finished`() {
        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = handledThenQuiet(),
            now = 40 * MINUTE
        )

        assertEquals(1L, suggestion?.taskId)
    }

    @Test
    fun `a task nobody ever started is never suggested, however long it sits`() {
        val suggestion = inference.suggestionFor(
            task = task(startedAt = null),
            events = listOf(event(TaskEventType.CREATED, 0L)),
            now = 10 * HOUR
        )

        assertNull("an untouched notification is not finished, it is ignored", suggestion)
    }

    @Test
    fun `a task still being worked on is left alone`() {
        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = handledThenQuiet(),
            now = 6 * MINUTE
        )

        assertNull("still inside the quiet period", suggestion)
    }

    @Test
    fun `a glance too short to have done anything does not count as engagement`() {
        val events = listOf(
            event(TaskEventType.CREATED, 0L),
            event(TaskEventType.STARTED, 1 * MINUTE),
            event(TaskEventType.SWITCHED_AWAY, 1 * MINUTE + 5_000L, pkg = "com.instagram.android")
        )

        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = events,
            now = 40 * MINUTE
        )

        assertNull("five seconds in the app is not handling it", suggestion)
    }

    @Test
    fun `a task already finished is never suggested again`() {
        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE, completedAt = 5 * MINUTE),
            events = handledThenQuiet() + event(TaskEventType.COMPLETED, 5 * MINUTE),
            now = 40 * MINUTE
        )

        assertNull(suggestion)
    }

    @Test
    fun `a task reopened after being finished can be suggested again once it goes quiet`() {
        val events = handledThenQuiet() +
            event(TaskEventType.COMPLETED, 5 * MINUTE) +
            event(TaskEventType.REOPENED, 6 * MINUTE) +
            event(TaskEventType.STARTED, 7 * MINUTE) +
            event(TaskEventType.SWITCHED_AWAY, 10 * MINUTE, pkg = "tech.benedicta.will")

        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE, completedAt = null),
            events = events,
            now = 45 * MINUTE
        )

        assertEquals(1L, suggestion?.taskId)
    }

    @Test
    fun `activity restarts the quiet period`() {
        val events = handledThenQuiet() + event(TaskEventType.RESUMED, 39 * MINUTE)

        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = events,
            now = 45 * MINUTE
        )

        assertNull("the user came back six minutes ago", suggestion)
    }

    @Test
    fun `a task quiet for longer than the stale window is left alone`() {
        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = handledThenQuiet(),
            now = 30 * HOUR
        )

        assertNull("too old to be worth asking about", suggestion)
    }

    @Test
    fun `a task with no events at all is never suggested`() {
        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = emptyList(),
            now = 40 * MINUTE
        )

        assertNull(suggestion)
    }

    @Test
    fun `events belonging to another task do not count`() {
        val suggestion = inference.suggestionFor(
            task = task(id = 1L, startedAt = 1 * MINUTE),
            events = handledThenQuiet(taskId = 2L),
            now = 40 * MINUTE
        )

        assertNull(suggestion)
    }

    @Test
    fun `the suggestion is stamped with the moment it was made`() {
        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = handledThenQuiet(),
            now = 40 * MINUTE
        )

        assertEquals(40 * MINUTE, suggestion?.suggestedAt)
    }

    @Test
    fun `staying in the source app with no switch still counts once it goes quiet`() {
        val events = listOf(
            event(TaskEventType.CREATED, 0L),
            event(TaskEventType.STARTED, 1 * MINUTE)
        )

        val suggestion = inference.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = events,
            now = 40 * MINUTE
        )

        assertEquals(1L, suggestion?.taskId)
    }

    // --- the batch form the feed uses ---

    @Test
    fun `the batch form picks out only the tasks that look finished`() {
        val finished = task(id = 1L, startedAt = 1 * MINUTE)
        val untouched = task(id = 2L, startedAt = null)
        val events = handledThenQuiet(taskId = 1L) + event(TaskEventType.CREATED, 0L, taskId = 2L)

        val suggested = inference.suggestedTaskIds(
            tasks = listOf(finished, untouched),
            events = events,
            now = 40 * MINUTE
        )

        assertEquals(setOf(1L), suggested)
    }

    @Test
    fun `the batch form is empty when nothing qualifies`() {
        val suggested = inference.suggestedTaskIds(
            tasks = listOf(task(id = 1L, startedAt = null)),
            events = listOf(event(TaskEventType.CREATED, 0L)),
            now = 40 * MINUTE
        )

        assertTrue(suggested.isEmpty())
    }

    @Test
    fun `thresholds are configurable so the rule can be tuned without rewriting it`() {
        val impatient = CompletionInference(quietPeriod = 2 * MINUTE)

        val suggestion = impatient.suggestionFor(
            task = task(startedAt = 1 * MINUTE),
            events = handledThenQuiet(),
            now = 7 * MINUTE
        )

        assertEquals(1L, suggestion?.taskId)
    }
}
