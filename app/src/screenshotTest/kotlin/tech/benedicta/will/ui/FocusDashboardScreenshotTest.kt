package tech.benedicta.will.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import tech.benedicta.will.task.AttentionRange
import tech.benedicta.will.task.FocusDashboardViewModel
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskEvent
import tech.benedicta.will.task.TaskEventType
import tech.benedicta.will.ui.theme.WillTheme

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/** A Wednesday afternoon, so the weekday labels under the bars are readable. */
private const val NOW = 20_000 * DAY + 15 * HOUR

private fun fixedDayStart(instant: Long) = instant / DAY * DAY

private fun worked(taskId: Long, dayOffset: Long, startHour: Long, minutes: Long) = listOf(
    TaskEvent(
        id = taskId * 10,
        taskId = taskId,
        type = TaskEventType.STARTED,
        at = (20_000 - dayOffset) * DAY + startHour * HOUR
    ),
    TaskEvent(
        id = taskId * 10 + 1,
        taskId = taskId,
        type = TaskEventType.COMPLETED,
        at = (20_000 - dayOffset) * DAY + startHour * HOUR + minutes * MINUTE
    )
)

private fun sampleTask(id: Long, title: String, startedAt: Long?, completedAt: Long?) = Task(
    id = id,
    title = title,
    text = "",
    sourcePackage = "com.slack",
    createdAt = NOW - 6 * HOUR,
    startedAt = startedAt,
    completedAt = completedAt,
    contextSwitchCount = 4
)

private fun dashboard(
    tasks: List<Task>,
    events: List<TaskEvent>,
    range: AttentionRange = AttentionRange.WEEK
) = FocusDashboardViewModel(
    tasks = tasks,
    events = events,
    range = range,
    now = { NOW },
    dayStartOf = ::fixedDayStart
)

private fun pulledAway(to: String, dayOffset: Long, times: Int, seed: Long) =
    (0 until times).map { index ->
        TaskEvent(
            id = seed + index,
            taskId = 1,
            type = TaskEventType.SWITCHED_AWAY,
            at = (20_000 - dayOffset) * DAY + (9 + index) * HOUR,
            foregroundPackage = to
        )
    }

private val aWeekOfWork = worked(1, dayOffset = 6, startHour = 9, minutes = 95) +
    worked(2, dayOffset = 5, startHour = 14, minutes = 40) +
    worked(3, dayOffset = 3, startHour = 10, minutes = 150) +
    worked(4, dayOffset = 2, startHour = 16, minutes = 25) +
    worked(5, dayOffset = 0, startHour = 9, minutes = 70) +
    pulledAway("com.instagram.android", dayOffset = 5, times = 6, seed = 1_000) +
    pulledAway("com.whatsapp", dayOffset = 3, times = 4, seed = 2_000) +
    pulledAway("com.twitter.android", dayOffset = 1, times = 2, seed = 3_000)

private val today = listOf(
    sampleTask(1, "responder Maria", startedAt = NOW - 5 * HOUR, completedAt = NOW - 4 * HOUR),
    sampleTask(2, "pagar o boleto da luz", startedAt = NOW - 2 * HOUR, completedAt = null)
)

/** The whole screen: today's figures above, the week the log remembers below. */
@PreviewTest
@Preview(name = "focus week", widthDp = 412, heightDp = 900, showBackground = true)
@Composable
private fun FocusDashboardWeekPreview() {
    WillTheme {
        FocusDashboard(
            viewModel = dashboard(today, aWeekOfWork),
            onBack = {},
            range = AttentionRange.WEEK
        )
    }
}

/** Thirty days drops the weekday labels, because thirty of them cannot be read. */
@PreviewTest
@Preview(name = "focus month", widthDp = 412, heightDp = 900, showBackground = true)
@Composable
private fun FocusDashboardMonthPreview() {
    WillTheme {
        FocusDashboard(
            viewModel = dashboard(today, aWeekOfWork, AttentionRange.MONTH),
            onBack = {},
            range = AttentionRange.MONTH
        )
    }
}

/**
 * A quiet today with a week behind it. The whole screen used to live inside
 * the branch that renders when something happened today, so this state showed
 * "nothing yet" over a full week of history.
 */
@PreviewTest
@Preview(name = "focus quiet today", widthDp = 412, heightDp = 620, showBackground = true)
@Composable
private fun FocusDashboardQuietTodayPreview() {
    WillTheme {
        FocusDashboard(
            viewModel = dashboard(emptyList(), aWeekOfWork),
            onBack = {},
            range = AttentionRange.WEEK
        )
    }
}

/** Nothing at all: no today, no history. */
@PreviewTest
@Preview(name = "focus empty", widthDp = 412, heightDp = 420, showBackground = true)
@Composable
private fun FocusDashboardEmptyPreview() {
    WillTheme {
        FocusDashboard(
            viewModel = dashboard(emptyList(), emptyList()),
            onBack = {},
            range = AttentionRange.WEEK
        )
    }
}
