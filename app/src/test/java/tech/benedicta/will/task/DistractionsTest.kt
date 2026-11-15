package tech.benedicta.will.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val HOUR = 60 * 60_000L
private const val DAY = 24 * HOUR

/**
 * `SWITCHED_AWAY` has carried the app the user jumped to since US-12, and
 * nothing ever read it. Will has been recording what pulls people away from
 * their own tasks and never telling them.
 */
class DistractionsTest {

    private val history = AttentionHistory(dayStartOf = { it / DAY * DAY })
    private val today = 10 * DAY + 15 * HOUR

    private fun switch(taskId: Long, at: Long, to: String?, id: Long = at) =
        TaskEvent(
            id = id,
            taskId = taskId,
            type = TaskEventType.SWITCHED_AWAY,
            at = at,
            foregroundPackage = to
        )

    private fun read(events: List<TaskEvent>, now: Long = today, limit: Int = 3) =
        history.distractions(events, AttentionRange.WEEK, now, limit, excluding = setOf(WILL))

    @Test
    fun `nothing in the log means nothing to report`() {
        assertTrue(read(emptyList()).isEmpty())
    }

    @Test
    fun `the app that pulled you away most comes first`() {
        val events = listOf(
            switch(1, 10 * DAY + 9 * HOUR, "com.instagram.android", id = 1),
            switch(1, 10 * DAY + 10 * HOUR, "com.instagram.android", id = 2),
            switch(1, 10 * DAY + 11 * HOUR, "com.instagram.android", id = 3),
            switch(2, 10 * DAY + 12 * HOUR, "com.twitter", id = 4)
        )

        val pulls = read(events)

        assertEquals("com.instagram.android", pulls.first().packageName)
        assertEquals(3, pulls.first().interruptions)
        assertEquals(listOf("com.instagram.android", "com.twitter"), pulls.map { it.packageName })
    }

    @Test
    fun `switches are counted across every task, because the interruption is the same`() {
        val events = listOf(
            switch(1, 10 * DAY + 9 * HOUR, "com.instagram.android", id = 1),
            switch(2, 10 * DAY + 10 * HOUR, "com.instagram.android", id = 2),
            switch(3, 10 * DAY + 11 * HOUR, "com.instagram.android", id = 3)
        )

        assertEquals(3, read(events).single().interruptions)
    }

    @Test
    fun `Will itself is never a distraction, because it is where you go between things`() {
        val events = listOf(
            switch(1, 10 * DAY + 9 * HOUR, WILL, id = 1),
            switch(1, 10 * DAY + 10 * HOUR, WILL, id = 2),
            switch(1, 10 * DAY + 11 * HOUR, "com.twitter", id = 3)
        )

        assertEquals(listOf("com.twitter"), read(events).map { it.packageName })
    }

    @Test
    fun `only switches count, not every event in the log`() {
        val events = listOf(
            TaskEvent(id = 1, taskId = 1, type = TaskEventType.STARTED, at = 10 * DAY + 9 * HOUR),
            TaskEvent(id = 2, taskId = 1, type = TaskEventType.COMPLETED, at = 10 * DAY + 10 * HOUR),
            switch(1, 10 * DAY + 11 * HOUR, "com.twitter", id = 3)
        )

        assertEquals(1, read(events).single().interruptions)
    }

    @Test
    fun `a switch with no app recorded is skipped rather than grouped under nothing`() {
        val events = listOf(
            switch(1, 10 * DAY + 9 * HOUR, null, id = 1),
            switch(1, 10 * DAY + 10 * HOUR, "com.twitter", id = 2)
        )

        assertEquals(listOf("com.twitter"), read(events).map { it.packageName })
    }

    @Test
    fun `switches outside the range are not this week's problem`() {
        val events = listOf(
            switch(1, 1 * DAY + 9 * HOUR, "com.instagram.android", id = 1),
            switch(1, 10 * DAY + 9 * HOUR, "com.twitter", id = 2)
        )

        assertEquals(listOf("com.twitter"), read(events).map { it.packageName })
    }

    @Test
    fun `a switch dated in the future is ignored rather than trusted`() {
        val events = listOf(switch(1, 30 * DAY, "com.instagram.android", id = 1))

        assertTrue(read(events).isEmpty())
    }

    @Test
    fun `only the worst few are reported, because a list of twenty helps nobody`() {
        val events = (1..10).map { index ->
            switch(1, 10 * DAY + index * HOUR, "com.app$index", id = index.toLong())
        }

        assertEquals(3, read(events).size)
    }

    @Test
    fun `apps that pulled you away the same number of times keep a stable order`() {
        val events = listOf(
            switch(1, 10 * DAY + 9 * HOUR, "com.bbb", id = 1),
            switch(1, 10 * DAY + 10 * HOUR, "com.aaa", id = 2)
        )

        assertEquals(listOf("com.aaa", "com.bbb"), read(events).map { it.packageName })
    }

    @Test
    fun `a month reaches further back than a week`() {
        val events = listOf(switch(1, 1 * DAY + 9 * HOUR, "com.instagram.android", id = 1))

        val overAMonth = history.distractions(
            events, AttentionRange.MONTH, today, limit = 3, excluding = setOf(WILL)
        )

        assertEquals(1, overAMonth.size)
    }

    private companion object {
        const val WILL = "tech.benedicta.will"
    }
}
