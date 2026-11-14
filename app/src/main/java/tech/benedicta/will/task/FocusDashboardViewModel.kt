package tech.benedicta.will.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar
import kotlin.math.roundToInt

data class TaskMetrics(
    val focusScore: Int,
    val contextSwitchCount: Int,
    val taskDurations: List<Pair<String, Float>>,
    val isEmpty: Boolean
)

/** Midnight of the day [instant] falls in, in the phone's own timezone. */
fun startOfDay(instant: Long): Long = Calendar.getInstance().run {
    timeInMillis = instant
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    timeInMillis
}

/**
 * Today's figures, plus what the task log says about the days before it.
 *
 * The clock and the calendar are injected rather than read in place, which is
 * what makes any of this testable: the old version called
 * `System.currentTimeMillis()` and `Calendar.getInstance()` inside its own
 * arithmetic, so what it reported depended on the day the test ran.
 *
 * [trend] is [AttentionHistory] over the event log, and it answers the
 * question the `Task` scalars cannot: not when a task began and ended, but
 * which days the user actually spent attention on.
 */
class FocusDashboardViewModel(
    tasks: List<Task>,
    events: List<TaskEvent> = emptyList(),
    range: AttentionRange = AttentionRange.WEEK,
    private val now: () -> Long = System::currentTimeMillis,
    private val dayStartOf: (Long) -> Long = ::startOfDay,
    ownPackage: String = WILL_PACKAGE
) : ViewModel() {

    private val history = AttentionHistory(dayStartOf)

    val metrics: TaskMetrics = computeMetrics(tasks)

    val trend: List<DayOfAttention> = history.read(events, range, now())

    /**
     * The apps that most often pulled the user off a task. Will is left out:
     * coming back to the launcher is how you move between tasks, not an
     * interruption.
     */
    val distractions: List<Distraction> =
        history.distractions(events, range, now(), excluding = setOf(ownPackage))

    /** The fullest day in [trend], so a chart can scale to it rather than to a guess. */
    val busiestDayMillis: Long = trend.maxOfOrNull { it.engagedMillis } ?: 0L

    /** Attention across the whole range, already free of overlap. */
    val engagedThisRangeMillis: Long = trend.sumOf { it.engagedMillis }

    /**
     * The longest the user stayed on one thing without leaving it, anywhere in
     * the range. The total says how much; this says whether any of it was in
     * one piece.
     */
    val bestStretchMillis: Long = trend.maxOfOrNull { it.longestStretchMillis } ?: 0L

    /** Whether there is anything to draw. An empty week is said, not plotted. */
    val hasTrend: Boolean = trend.any { it.engagedMillis > 0L }

    private fun computeMetrics(tasks: List<Task>): TaskMetrics {
        val todayTasks = tasks.filter { isToday(it.createdAt) }

        if (todayTasks.isEmpty()) return TaskMetrics(0, 0, emptyList(), true)

        val doneTasks = todayTasks.filter { it.status == TaskStatus.DONE }
        val completedCount = doneTasks.size
        val totalCount = todayTasks.size

        val totalFocusMinutes = doneTasks.sumOf { it.engagedMillis() / 60_000L }.toInt()
        val contextSwitches = todayTasks.sumOf { it.contextSwitchCount }

        val score = (
            completedCount.toFloat() / maxOf(totalCount, 1) * COMPLETION_WEIGHT +
                totalFocusMinutes.toFloat() / TARGET_MINUTES * TIME_WEIGHT +
                (1f - contextSwitches.toFloat() / EXPECTED_SWITCHES) * CALM_WEIGHT
            ).coerceIn(0f, 100f).roundToInt()

        val longest = doneTasks.maxOfOrNull { it.engagedMillis() } ?: 1L
        val taskDurations = doneTasks.map { task ->
            task.title to (task.engagedMillis().toFloat() / maxOf(longest, 1L).toFloat())
        }

        return TaskMetrics(
            focusScore = score,
            contextSwitchCount = contextSwitches,
            taskDurations = taskDurations,
            isEmpty = false
        )
    }

    private fun Task.engagedMillis(): Long =
        (completedAt ?: now()) - (startedAt ?: createdAt)

    private fun isToday(millis: Long) = dayStartOf(millis) == dayStartOf(now())

    private companion object {
        const val WILL_PACKAGE = "tech.benedicta.will"
        const val COMPLETION_WEIGHT = 50
        const val TIME_WEIGHT = 30
        const val CALM_WEIGHT = 20

        /** A focused working day, against which time on task is scored. */
        const val TARGET_MINUTES = 240

        /** Switches past this and the calm part of the score is spent. */
        const val EXPECTED_SWITCHES = 30
    }

    class Factory(
        private val tasks: List<Task>,
        private val events: List<TaskEvent> = emptyList(),
        private val range: AttentionRange = AttentionRange.WEEK
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FocusDashboardViewModel(tasks, events, range) as T
    }
}
