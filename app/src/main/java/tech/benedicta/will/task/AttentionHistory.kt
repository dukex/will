package tech.benedicta.will.task

/** What one day did to the user's attention, read out of the task log. */
data class DayOfAttention(
    val dayStart: Long,
    val engagedMillis: Long = 0L,
    val longestStretchMillis: Long = 0L,
    val contextSwitches: Int = 0,
    val tasksTouched: Int = 0,
    val tasksFinished: Int = 0
)

/** How far back a reading goes. */
enum class AttentionRange(val days: Int) { WEEK(7), MONTH(30) }

/**
 * Reads the task log as a history of attention, one entry per day.
 *
 * This is the question `Task` cannot answer. Its scalars say a task started on
 * Monday and finished on Thursday; they cannot say the user actually touched it
 * for twenty minutes on Monday, not at all on Tuesday, and an hour on
 * Thursday. US-12 made the log append-only so that history survives, and this
 * is the second thing to read it, after [CompletionInference].
 *
 * Two rules worth stating, because they are what make the numbers honest:
 *
 * - **Overlapping work is one span of attention, not two.** Two tasks open
 *   across the same hour cost the user one hour, so the intervals are merged
 *   before anything is measured. Summing them would let a day report more
 *   attention than it contains.
 * - **A stretch that crosses midnight belongs to both days**, in the
 *   proportion it actually fell in each. Attributing it to the day it started
 *   would quietly empty every morning after a late night.
 *
 * Every day in the range comes back, including the empty ones, so a trend has
 * a continuous axis rather than a gap wherever nothing happened.
 *
 * The calendar is injected as [dayStartOf]: the domain layer has no Android
 * dependency, and a day boundary is a timezone question, not a rule about
 * attention. Days are walked with a twelve-hour margin rather than by adding
 * 24 hours, so an hour gained or lost to daylight saving does not shift them.
 */
class AttentionHistory(private val dayStartOf: (Long) -> Long) {

    private data class Span(val from: Long, val until: Long, val taskId: Long)

    fun read(
        events: List<TaskEvent>,
        range: AttentionRange,
        now: Long
    ): List<DayOfAttention> {
        val dayStarts = daysEndingAt(now, range.days)
        val boundaries = dayStarts + nextDayStart(dayStarts.last())
        val windowStart = dayStarts.first()

        // An event dated after `now` is a clock that moved, not something that
        // happened. Trusting it would let one bad timestamp swallow the window.
        val known = events.filter { it.at <= now }
        val spans = engagementSpans(known, now).mapNotNull { it.clampedTo(windowStart, now) }
        val attention = spans.merged()

        val switchesByDay = known
            .filter { it.type == TaskEventType.SWITCHED_AWAY && it.at >= windowStart }
            .groupingBy { dayStartOf(it.at) }
            .eachCount()
        val finishesByDay = known
            .filter { it.type == TaskEventType.COMPLETED && it.at >= windowStart }
            .groupingBy { dayStartOf(it.at) }
            .eachCount()

        return dayStarts.indices.map { index ->
            val from = boundaries[index]
            val until = boundaries[index + 1]
            val withinDay = attention.mapNotNull { it.clampedTo(from, until) }

            DayOfAttention(
                dayStart = from,
                engagedMillis = withinDay.sumOf { it.until - it.from },
                longestStretchMillis = withinDay.maxOfOrNull { it.until - it.from } ?: 0L,
                contextSwitches = switchesByDay[from] ?: 0,
                tasksTouched = spans
                    .filter { it.overlaps(from, until) }
                    .map { it.taskId }
                    .distinct()
                    .size,
                tasksFinished = finishesByDay[from] ?: 0
            )
        }
    }

    /**
     * Every stretch the user was actually on a task: a work-starting event
     * opens one and the task's next event closes it. A stretch still open when
     * the log ends closes at [now], never later, so a task left running
     * overnight cannot claim time that has not passed.
     */
    private fun engagementSpans(events: List<TaskEvent>, now: Long): List<Span> =
        events
            .groupBy { it.taskId }
            .flatMap { (taskId, forTask) ->
                val ordered = forTask.sortedWith(compareBy({ it.at }, { it.id }))
                ordered.mapIndexedNotNull { index, event ->
                    if (!event.type.opensWork()) return@mapIndexedNotNull null
                    val until = ordered.drop(index + 1).firstOrNull { it.at > event.at }?.at ?: now
                    Span(from = event.at, until = minOf(until, now), taskId = taskId)
                }
            }
            .filter { it.until > it.from }

    private fun TaskEventType.opensWork() =
        this == TaskEventType.STARTED || this == TaskEventType.RESUMED

    private fun Span.clampedTo(from: Long, until: Long): Span? {
        val start = maxOf(this.from, from)
        val end = minOf(this.until, until)
        return if (end > start) copy(from = start, until = end) else null
    }

    private fun Span.overlaps(from: Long, until: Long) = this.from < until && this.until > from

    /**
     * The union of the spans. Overlapping and touching spans become one, so a
     * day can never report more attention than it holds.
     */
    private fun List<Span>.merged(): List<Span> {
        if (isEmpty()) return emptyList()
        val ordered = sortedBy { it.from }
        val union = mutableListOf(ordered.first())
        ordered.drop(1).forEach { span ->
            val open = union.last()
            if (span.from <= open.until) {
                union[union.lastIndex] = open.copy(until = maxOf(open.until, span.until))
            } else {
                union.add(span)
            }
        }
        return union
    }

    /** The [count] day starts ending with the day that contains [now], oldest first. */
    private fun daysEndingAt(now: Long, count: Int): List<Long> {
        val days = ArrayDeque<Long>()
        var cursor = dayStartOf(now)
        repeat(count) {
            days.addFirst(cursor)
            cursor = previousDayStart(cursor)
        }
        return days.toList()
    }

    /**
     * Walked with a twelve-hour margin instead of a flat 24 hours, so the hour
     * daylight saving adds or removes cannot land this in the wrong day.
     */
    private fun previousDayStart(dayStart: Long) = dayStartOf(dayStart - HALF_DAY)

    private fun nextDayStart(dayStart: Long) = dayStartOf(dayStart + DAY + HALF_DAY)

    private companion object {
        const val HALF_DAY = 12 * 60 * 60 * 1000L
        const val DAY = 24 * 60 * 60 * 1000L
    }
}
