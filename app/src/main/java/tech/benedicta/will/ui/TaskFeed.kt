package tech.benedicta.will.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.task.TaskStatus
import tech.benedicta.will.task.status
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillAccent
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskFeed(
    tasks: List<Task>,
    onTap: (Task) -> Unit = {},
    onMarkDone: (Task) -> Unit = {},
    header: (LazyListScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    val activeTasks = tasks.filter { it.status == TaskStatus.IN_PROGRESS }
    val pendingTasks = tasks.filter {
        it.status == TaskStatus.PAUSED || it.status == TaskStatus.PENDING
    }
    val hasTasks = activeTasks.isNotEmpty() || pendingTasks.isNotEmpty()

    if (header == null && !hasTasks) {
        EmptyState(tasks = tasks, modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        if (header != null) header()

        if (!hasTasks) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "no tasks yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WillTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "notifications will appear here",
                        style = MaterialTheme.typography.labelSmall,
                        color = WillTextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
            return@LazyColumn
        }

        if (activeTasks.isNotEmpty()) {
            items(activeTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTap = onTap,
                    onMarkDone = onMarkDone
                )
                HorizontalDivider(color = WillBorder, thickness = 1.dp)
            }
        }

        if (pendingTasks.isNotEmpty()) {
            if (activeTasks.isNotEmpty()) {
                item { SectionHeader("pending") }
                item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            }
            items(pendingTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTap = onTap,
                    onMarkDone = onMarkDone
                )
                HorizontalDivider(color = WillBorder, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun EmptyState(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    val todayDone = tasks.filter {
        it.status == TaskStatus.DONE && isToday(it.completedAt ?: 0L)
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (todayDone.isNotEmpty()) {
                val focusMinutes = todayDone.sumOf { task ->
                    val start = task.startedAt ?: task.createdAt
                    val end = task.completedAt ?: System.currentTimeMillis()
                    (end - start) / 60_000L
                }.toInt()
                val focusLabel = "%02d:%02d focused today".format(focusMinutes / 60, focusMinutes % 60)
                Text(
                    text = "clear",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WillTextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = focusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = WillTextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            } else {
                Text(
                    text = "no tasks yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WillTextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "notifications will appear here",
                    style = MaterialTheme.typography.labelSmall,
                    color = WillTextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onTap: (Task) -> Unit,
    onMarkDone: (Task) -> Unit
) {
    val context = LocalContext.current
    // Null when the task points at no app: the sentinel is storage, not a name
    // the user should read. An uninstalled app still falls back to its package,
    // which is the most honest thing left to show.
    val appLabel = remember(task.sourcePackage) {
        if (task.sourcePackage == TaskOrigin.MANUAL_SOURCE_PACKAGE) {
            null
        } else {
            runCatching {
                val info = context.packageManager.getApplicationInfo(task.sourcePackage, 0)
                context.packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(task.sourcePackage)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap(task) }
            .heightIn(min = Spacing.touchTarget)
            .padding(vertical = Spacing.md, horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DoneCheckbox(
            status = task.status,
            onMarkDone = { onMarkDone(task) },
            taskTitle = task.title,
            modifier = Modifier.padding(end = Spacing.sm)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = taskMetadataLine(
                    appLabel = appLabel,
                    origin = task.origin,
                    time = formatTimestamp(task.createdAt),
                    unreadCount = task.unreadCount
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun DoneCheckbox(
    status: TaskStatus,
    onMarkDone: () -> Unit,
    taskTitle: String,
    modifier: Modifier = Modifier
) {
    val borderColor = when (status) {
        TaskStatus.IN_PROGRESS -> WillAccent
        TaskStatus.PAUSED -> WillTextTertiary
        TaskStatus.PENDING -> WillTextTertiary
        TaskStatus.DONE -> WillTextTertiary
    }

    Box(
        modifier = modifier
            .size(Spacing.touchTarget)
            .clip(CircleShape)
            .clickable(onClick = onMarkDone)
            .semantics {
                role = Role.Checkbox
                contentDescription = "mark done $taskTitle"
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(1.5.dp, borderColor, CircleShape)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun isToday(millis: Long): Boolean {
    val now = java.util.Calendar.getInstance()
    val then = java.util.Calendar.getInstance().also { it.timeInMillis = millis }
    return now.get(java.util.Calendar.YEAR) == then.get(java.util.Calendar.YEAR) &&
            now.get(java.util.Calendar.DAY_OF_YEAR) == then.get(java.util.Calendar.DAY_OF_YEAR)
}