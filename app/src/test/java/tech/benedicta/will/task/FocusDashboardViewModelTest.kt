package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/**
 * The dashboard used to read the clock and the calendar itself, which is why
 * none of it had tests. Both are injected now, so what it reports is a rule
 * rather than whatever today happens to be.
 */
class FocusDashboardViewModelTest {

    private val today = 10 * DAY + 14 * HOUR

    private fun dashboard(
        tasks: List<Task> = emptyList(),
        events: List<TaskEvent> = emptyList(),
        range: AttentionRange = AttentionRange.WEEK,
        now: Long = today
    ) = FocusDashboardViewModel(
        tasks = tasks,
        events = events,
        range = range,
        now = { now },
        dayStartOf = { instant -> instant / DAY * DAY }
    )

    private fun task(
        id: Long,
        createdAt: Long,
        startedAt: Long? = null,
        completedAt: Long? = null,
        switches: Int = 0,
        title: String = "task $id"
    ) = Task(
        id = id,
        title = title,
        text = "",
        sourcePackage = "com.whatsapp",
        createdAt = createdAt,
        startedAt = startedAt,
        completedAt = completedAt,
        contextSwitchCount = switches
    )

    // --- today's figures, which used to be untestable ---

    @Test
    fun `with nothing today the dashboard says so instead of showing a zero score`() {
        val metrics = dashboard(tasks = listOf(task(1, createdAt = 2 * DAY))).metrics

        assertTrue(metrics.isEmpty)
    }

    @Test
    fun `a task created today makes the dashboard non-empty`() {
        val metrics = dashboard(tasks = listOf(task(1, createdAt = today - HOUR))).metrics

        assertFalse(metrics.isEmpty)
    }

    @Test
    fun `switches are totalled across today's tasks`() {
        val metrics = dashboard(
            tasks = listOf(
                task(1, createdAt = today - HOUR, switches = 3),
                task(2, createdAt = today - 2 * HOUR, switches = 4),
                task(3, createdAt = 2 * DAY, switches = 99)
            )
        ).metrics

        assertEquals("yesterday's switches are not today's", 7, metrics.contextSwitchCount)
    }

    @Test
    fun `the score stays inside nought to a hundred however bad the day was`() {
        val metrics = dashboard(
            tasks = (1L..40L).map { task(it, createdAt = today - HOUR, switches = 50) }
        ).metrics

        assertTrue("got ${metrics.focusScore}", metrics.focusScore in 0..100)
    }

    @Test
    fun `finishing everything scores better than finishing nothing`() {
        val finished = dashboard(
            tasks = listOf(
                task(1, createdAt = today - 2 * HOUR, startedAt = today - 2 * HOUR, completedAt = today - HOUR)
            )
        ).metrics
        val untouched = dashboard(tasks = listOf(task(1, createdAt = today - 2 * HOUR))).metrics

        assertTrue(
            "${finished.focusScore} should beat ${untouched.focusScore}",
            finished.focusScore > untouched.focusScore
        )
    }

    // --- the trend, which is what the log unlocks ---

    @Test
    fun `the trend covers the whole week even before there is a week of history`() {
        val trend = dashboard().trend

        assertEquals(7, trend.size)
        assertTrue(trend.all { it.engagedMillis == 0L })
    }

    @Test
    fun `a month can be asked for instead of a week`() {
        assertEquals(30, dashboard(range = AttentionRange.MONTH).trend.size)
    }

    @Test
    fun `the trend reads attention out of the log, day by day`() {
        val events = listOf(
            TaskEvent(id = 1, taskId = 1, type = TaskEventType.STARTED, at = 8 * DAY + 9 * HOUR),
            TaskEvent(id = 2, taskId = 1, type = TaskEventType.COMPLETED, at = 8 * DAY + 10 * HOUR),
            TaskEvent(id = 3, taskId = 2, type = TaskEventType.STARTED, at = 10 * DAY + 9 * HOUR),
            TaskEvent(id = 4, taskId = 2, type = TaskEventType.COMPLETED, at = 10 * DAY + 9 * HOUR + 30 * MINUTE)
        )

        val trend = dashboard(events = events).trend

        assertEquals(HOUR, trend.single { it.dayStart == 8 * DAY }.engagedMillis)
        assertEquals(0L, trend.single { it.dayStart == 9 * DAY }.engagedMillis)
        assertEquals(30 * MINUTE, trend.single { it.dayStart == 10 * DAY }.engagedMillis)
    }

    @Test
    fun `the busiest day is reported so the chart can scale to it`() {
        val events = listOf(
            TaskEvent(id = 1, taskId = 1, type = TaskEventType.STARTED, at = 8 * DAY + 9 * HOUR),
            TaskEvent(id = 2, taskId = 1, type = TaskEventType.COMPLETED, at = 8 * DAY + 11 * HOUR),
            TaskEvent(id = 3, taskId = 2, type = TaskEventType.STARTED, at = 10 * DAY + 9 * HOUR),
            TaskEvent(id = 4, taskId = 2, type = TaskEventType.COMPLETED, at = 10 * DAY + 10 * HOUR)
        )

        assertEquals(2 * HOUR, dashboard(events = events).busiestDayMillis)
    }

    @Test
    fun `the busiest day of an empty week is zero rather than an error`() {
        assertEquals(0L, dashboard().busiestDayMillis)
    }

    @Test
    fun `a week with no log at all has no trend worth drawing`() {
        assertFalse(dashboard().hasTrend)
    }

    @Test
    fun `a week with any attention in it has a trend worth drawing`() {
        val events = listOf(
            TaskEvent(id = 1, taskId = 1, type = TaskEventType.STARTED, at = 10 * DAY + 9 * HOUR),
            TaskEvent(id = 2, taskId = 1, type = TaskEventType.COMPLETED, at = 10 * DAY + 10 * HOUR)
        )

        assertTrue(dashboard(events = events).hasTrend)
    }

    @Test
    fun `the week totals what the days hold, without double counting overlap`() {
        val events = listOf(
            TaskEvent(id = 1, taskId = 1, type = TaskEventType.STARTED, at = 10 * DAY + 9 * HOUR),
            TaskEvent(id = 2, taskId = 2, type = TaskEventType.STARTED, at = 10 * DAY + 9 * HOUR),
            TaskEvent(id = 3, taskId = 1, type = TaskEventType.COMPLETED, at = 10 * DAY + 10 * HOUR),
            TaskEvent(id = 4, taskId = 2, type = TaskEventType.COMPLETED, at = 10 * DAY + 10 * HOUR)
        )

        val dashboard = dashboard(events = events)

        assertEquals(
            "two tasks across the same hour is one hour of the user's attention",
            HOUR,
            dashboard.engagedThisRangeMillis
        )
    }
}
