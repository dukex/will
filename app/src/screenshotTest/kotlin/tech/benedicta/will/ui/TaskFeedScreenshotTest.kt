package tech.benedicta.will.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.ui.theme.WillTheme

private const val MINUTE = 60_000L

private fun sampleTask(
    id: Long,
    title: String,
    sourcePackage: String,
    origin: TaskOrigin,
    minutesAgo: Long,
    unreadCount: Int = 1,
    startedAt: Long? = null
) = Task(
    id = id,
    title = title,
    text = "",
    sourcePackage = sourcePackage,
    createdAt = 1_700_000_000_000L - minutesAgo * MINUTE,
    startedAt = startedAt,
    unreadCount = unreadCount,
    origin = origin
)

/**
 * One row per origin, so the metadata line can be read side by side: a
 * notification, a typed task, a share, and something an agent wrote.
 */
@PreviewTest
@Preview(name = "feed origins", widthDp = 412, heightDp = 420, showBackground = true)
@Composable
private fun TaskFeedOriginsPreview() {
    WillTheme {
        TaskFeed(
            tasks = listOf(
                sampleTask(
                    id = 1,
                    title = "Sarah: can you review the PR today",
                    sourcePackage = "com.slack",
                    origin = TaskOrigin.NOTIFICATION,
                    minutesAgo = 4,
                    unreadCount = 3
                ),
                sampleTask(
                    id = 2,
                    title = "pagar o boleto da luz",
                    sourcePackage = TaskOrigin.MANUAL_SOURCE_PACKAGE,
                    origin = TaskOrigin.MANUAL,
                    minutesAgo = 26
                ),
                sampleTask(
                    id = 3,
                    title = "contract draft to read before friday",
                    sourcePackage = "com.whatsapp",
                    origin = TaskOrigin.SHARED,
                    minutesAgo = 71
                ),
                sampleTask(
                    id = 4,
                    title = "renew the passport, appointment opens monday",
                    sourcePackage = TaskOrigin.MANUAL_SOURCE_PACKAGE,
                    origin = TaskOrigin.AGENT,
                    minutesAgo = 140
                )
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    }
}

/** The same four rows with one of them running, to check the active state. */
@PreviewTest
@Preview(name = "feed origins with active task", widthDp = 412, heightDp = 420, showBackground = true)
@Composable
private fun TaskFeedActivePreview() {
    WillTheme {
        TaskFeed(
            tasks = listOf(
                sampleTask(
                    id = 1,
                    title = "renew the passport, appointment opens monday",
                    sourcePackage = TaskOrigin.MANUAL_SOURCE_PACKAGE,
                    origin = TaskOrigin.AGENT,
                    minutesAgo = 3,
                    startedAt = 1_700_000_000_000L
                ),
                sampleTask(
                    id = 2,
                    title = "contract draft to read before friday",
                    sourcePackage = "com.whatsapp",
                    origin = TaskOrigin.SHARED,
                    minutesAgo = 40
                )
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    }
}
