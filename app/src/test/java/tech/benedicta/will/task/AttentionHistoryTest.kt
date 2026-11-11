package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val SECOND = 1_000L
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/**
 * The log is the only thing that can answer these questions. `Task` carries
 * `startedAt` and `completedAt`, so it can say a task began on Monday and
 * finished on Thursday; it cannot say the user actually touched it for twenty
 * minutes on Monday, not at all on Tuesday, and an hour on Thursday. That is
 * what the event log is for, and until now nothing read it this way.
 */
class AttentionHistoryTest {

    /** Fixed-length days, so the rule is tested rather than the calendar. */
    private val history = AttentionHistory(dayStartOf = { instant -> instant / DAY * DAY })

    private fun event(
        taskId: Long,
        type: TaskEventType,
        at: Long,
        foregroundPackage: String? = null,
        id: Long = at
    ) = TaskEvent(id = id, taskId = taskId, type = type, at = at, foregroundPackage = foregroundPackage)

    private fun read(events: List<TaskEvent>, now: Long, range: AttentionRange = AttentionRange.WEEK) =
        history.read(events, range, now)

    // --- the shape of the answer ---

    @Test
    fun `a week is always seven days, even with nothing in the log`() {
        val days = read(emptyList(), now = 10 * DAY + 9 * HOUR)

        assertEquals(7, days.size)
        assertTrue("an empty day is zero, not missing", days.all { it.engagedMillis == 0L })
    }

    @Test
    fun `the days come back oldest first and end with today`() {
        val days = read(emptyList(), now = 10 * DAY + 9 * HOUR)

        assertEquals((4L..10L).map { it * DAY }, days.map { it.dayStart })
    }

    @Test
    fun `a month is thirty days`() {
        val days = read(emptyList(), now = 40 * DAY, range = AttentionRange.MONTH)

        assertEquals(30, days.size)
    }

    // --- engagement, which is the part only the log knows ---

    @Test
    fun `time between starting and leaving a task is time on task`() {
        val events = listOf(
            event(1, TaskEventType.CREATED, 10 * DAY),
            event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR),
            event(1, TaskEventType.COMPLETED, 10 * DAY + 9 * HOUR + 20 * MINUTE)
        )

        val today = read(events, now = 10 * DAY + 12 * HOUR).last()

        assertEquals(20 * MINUTE, today.engagedMillis)
    }

    @Test
    fun `the gap between pausing and resuming is not time on task`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR),
            event(1, TaskEventType.PAUSED, 10 * DAY + 9 * HOUR + 10 * MINUTE),
            event(1, TaskEventType.RESUMED, 10 * DAY + 11 * HOUR),
            event(1, TaskEventType.COMPLETED, 10 * DAY + 11 * HOUR + 5 * MINUTE)
        )

        val today = read(events, now = 10 * DAY + 12 * HOUR).last()

        assertEquals("ten minutes plus five, not the two hours away", 15 * MINUTE, today.engagedMillis)
    }

    @Test
    fun `a task left open is counted up to now, not forever`() {
        val events = listOf(event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR))

        val today = read(events, now = 10 * DAY + 9 * HOUR + 30 * MINUTE).last()

        assertEquals(30 * MINUTE, today.engagedMillis)
    }

    @Test
    fun `work that crosses midnight is split between the two days`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 10 * DAY + 23 * HOUR + 30 * MINUTE),
            event(1, TaskEventType.COMPLETED, 11 * DAY + 30 * MINUTE)
        )

        val days = read(events, now = 11 * DAY + 2 * HOUR)
        val before = days.single { it.dayStart == 10 * DAY }
        val after = days.single { it.dayStart == 11 * DAY }

        assertEquals(30 * MINUTE, before.engagedMillis)
        assertEquals(30 * MINUTE, after.engagedMillis)
    }

    @Test
    fun `a stretch spanning a whole day gives that day its full twenty four hours`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 9 * DAY + 12 * HOUR),
            event(1, TaskEventType.COMPLETED, 11 * DAY + 12 * HOUR)
        )

        val days = read(events, now = 11 * DAY + 13 * HOUR)

        assertEquals(12 * HOUR, days.single { it.dayStart == 9 * DAY }.engagedMillis)
        assertEquals(DAY, days.single { it.dayStart == 10 * DAY }.engagedMillis)
        assertEquals(12 * HOUR, days.single { it.dayStart == 11 * DAY }.engagedMillis)
    }

    @Test
    fun `the longest unbroken stretch is reported, not just the total`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR),
            event(1, TaskEventType.PAUSED, 10 * DAY + 9 * HOUR + 5 * MINUTE),
            event(1, TaskEventType.RESUMED, 10 * DAY + 10 * HOUR),
            event(1, TaskEventType.PAUSED, 10 * DAY + 10 * HOUR + 40 * MINUTE),
            event(1, TaskEventType.RESUMED, 10 * DAY + 14 * HOUR),
            event(1, TaskEventType.COMPLETED, 10 * DAY + 14 * HOUR + 10 * MINUTE)
        )

        val today = read(events, now = 10 * DAY + 15 * HOUR).last()

        assertEquals(55 * MINUTE, today.engagedMillis)
        assertEquals("fifty-five minutes of work, but never more than forty at once",
            40 * MINUTE, today.longestStretchMillis)
    }

    @Test
    fun `two tasks worked on at once do not inflate the day beyond itself`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR, id = 1),
            event(2, TaskEventType.STARTED, 10 * DAY + 9 * HOUR, id = 2),
            event(1, TaskEventType.COMPLETED, 10 * DAY + 10 * HOUR, id = 3),
            event(2, TaskEventType.COMPLETED, 10 * DAY + 10 * HOUR, id = 4)
        )

        val today = read(events, now = 10 * DAY + 11 * HOUR).last()

        assertEquals(
            "overlapping work is one hour of the user's attention, not two",
            HOUR,
            today.engagedMillis
        )
    }

    // --- switches, tasks touched, tasks finished ---

    @Test
    fun `switches are counted on the day they happened`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 9 * DAY + 9 * HOUR),
            event(1, TaskEventType.SWITCHED_AWAY, 9 * DAY + 10 * HOUR, foregroundPackage = "com.instagram.android"),
            event(1, TaskEventType.SWITCHED_AWAY, 10 * DAY + 10 * HOUR, foregroundPackage = "com.instagram.android")
        )

        val days = read(events, now = 10 * DAY + 11 * HOUR)

        assertEquals(1, days.single { it.dayStart == 9 * DAY }.contextSwitches)
        assertEquals(1, days.single { it.dayStart == 10 * DAY }.contextSwitches)
    }

    @Test
    fun `a task touched twice in one day counts once`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR),
            event(1, TaskEventType.PAUSED, 10 * DAY + 9 * HOUR + 5 * MINUTE),
            event(1, TaskEventType.RESUMED, 10 * DAY + 15 * HOUR),
            event(1, TaskEventType.PAUSED, 10 * DAY + 15 * HOUR + 5 * MINUTE)
        )

        val today = read(events, now = 10 * DAY + 16 * HOUR).last()

        assertEquals(1, today.tasksTouched)
    }

    @Test
    fun `finishing a task counts on the day it was finished`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 9 * DAY + 23 * HOUR),
            event(1, TaskEventType.COMPLETED, 10 * DAY + HOUR)
        )

        val days = read(events, now = 10 * DAY + 2 * HOUR)

        assertEquals(0, days.single { it.dayStart == 9 * DAY }.tasksFinished)
        assertEquals(1, days.single { it.dayStart == 10 * DAY }.tasksFinished)
    }

    @Test
    fun `reopening and finishing again counts both finishes, because both happened`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR),
            event(1, TaskEventType.COMPLETED, 10 * DAY + 10 * HOUR),
            event(1, TaskEventType.REOPENED, 10 * DAY + 11 * HOUR),
            event(1, TaskEventType.COMPLETED, 10 * DAY + 12 * HOUR)
        )

        val today = read(events, now = 10 * DAY + 13 * HOUR).last()

        assertEquals(2, today.tasksFinished)
    }

    // --- the range is a window, not a filter on everything ---

    @Test
    fun `events older than the range are left out`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 1 * DAY + 9 * HOUR),
            event(1, TaskEventType.COMPLETED, 1 * DAY + 10 * HOUR)
        )

        val days = read(events, now = 10 * DAY + 9 * HOUR)

        assertTrue("day one is outside the week", days.all { it.engagedMillis == 0L })
        assertTrue(days.all { it.tasksFinished == 0 })
    }

    @Test
    fun `work that began before the range still counts inside it`() {
        val events = listOf(
            event(1, TaskEventType.STARTED, 3 * DAY + 20 * HOUR),
            event(1, TaskEventType.COMPLETED, 4 * DAY + 4 * HOUR)
        )

        val days = read(events, now = 10 * DAY + 9 * HOUR)

        assertEquals(
            "the four hours that fall inside the window are the user's, whenever the task began",
            4 * HOUR,
            days.single { it.dayStart == 4 * DAY }.engagedMillis
        )
    }

    @Test
    fun `an event in the future is ignored rather than trusted`() {
        val events = listOf(event(1, TaskEventType.STARTED, 20 * DAY))

        val days = read(events, now = 10 * DAY + 9 * HOUR)

        assertTrue(days.all { it.engagedMillis == 0L })
    }

    // --- reading the log out of order must not change the answer ---

    @Test
    fun `the answer does not depend on the order the events arrive in`() {
        val events = listOf(
            event(1, TaskEventType.COMPLETED, 10 * DAY + 10 * HOUR, id = 2),
            event(1, TaskEventType.STARTED, 10 * DAY + 9 * HOUR, id = 1)
        )

        val today = read(events, now = 10 * DAY + 11 * HOUR).last()

        assertEquals(HOUR, today.engagedMillis)
    }

    @Test
    fun `a task created and never opened contributes nothing`() {
        val events = listOf(event(1, TaskEventType.CREATED, 10 * DAY + 9 * HOUR))

        val today = read(events, now = 10 * DAY + 18 * HOUR).last()

        assertEquals(0L, today.engagedMillis)
        assertEquals(0, today.tasksTouched)
    }
}
