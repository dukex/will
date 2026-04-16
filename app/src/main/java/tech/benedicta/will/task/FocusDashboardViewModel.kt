package tech.benedicta.will.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.math.roundToInt

data class TaskMetrics(
    val focusScore: Int,
    val contextSwitchCount: Int,
    val taskDurations: List<Pair<String, Float>>,
    val isEmpty: Boolean
)

class FocusDashboardViewModel(tasks: List<Task>) : ViewModel() {

    val metrics: TaskMetrics = computeMetrics(tasks)

    private fun computeMetrics(tasks: List<Task>): TaskMetrics {
        val todayTasks = tasks.filter { isToday(it.createdAt) }

        if (todayTasks.isEmpty()) return TaskMetrics(0, 0, emptyList(), true)

        val doneTasks = todayTasks.filter { it.status == TaskStatus.DONE }
        val completedCount = doneTasks.size
        val totalCount = todayTasks.size

        val totalFocusMinutes = doneTasks.sumOf { task ->
            val start = task.startedAt ?: task.createdAt
            val end = task.completedAt ?: System.currentTimeMillis()
            (end - start) / 60_000L
        }.toInt()

        val contextSwitches = todayTasks.sumOf { it.contextSwitchCount }

        val targetMinutes = 240
        val maxExpectedSwitches = 30
        val score = (
            completedCount.toFloat() / maxOf(totalCount, 1) * 50 +
            totalFocusMinutes.toFloat() / maxOf(targetMinutes, 1) * 30 +
            (1f - contextSwitches.toFloat() / maxOf(maxExpectedSwitches, 1)) * 20
        ).coerceIn(0f, 100f).roundToInt()

        val maxDuration = doneTasks.maxOfOrNull { task ->
            val start = task.startedAt ?: task.createdAt
            val end = task.completedAt ?: System.currentTimeMillis()
            end - start
        } ?: 1L

        val taskDurations = doneTasks.map { task ->
            val start = task.startedAt ?: task.createdAt
            val end = task.completedAt ?: System.currentTimeMillis()
            val duration = end - start
            task.title to (duration.toFloat() / maxDuration.toFloat())
        }

        return TaskMetrics(
            focusScore = score,
            contextSwitchCount = contextSwitches,
            taskDurations = taskDurations,
            isEmpty = false
        )
    }

    private fun isToday(millis: Long): Boolean {
        val now = java.util.Calendar.getInstance()
        val then = java.util.Calendar.getInstance().also { it.timeInMillis = millis }
        return now.get(java.util.Calendar.YEAR) == then.get(java.util.Calendar.YEAR) &&
               now.get(java.util.Calendar.DAY_OF_YEAR) == then.get(java.util.Calendar.DAY_OF_YEAR)
    }

    class Factory(private val tasks: List<Task>) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FocusDashboardViewModel(tasks) as T
    }
}
