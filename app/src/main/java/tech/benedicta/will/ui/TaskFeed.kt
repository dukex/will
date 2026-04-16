package tech.benedicta.will.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskStatus
import tech.benedicta.will.task.status
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillDestructive
import tech.benedicta.will.ui.theme.WillSuccess
import tech.benedicta.will.ui.theme.WillTextPrimary
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskFeed(
    tasks: List<Task>,
    onTap: (Task) -> Unit = {},
    onLongPress: (Task) -> Unit = {},
    onSwipeDone: (Task) -> Unit = {},
    onSwipeDelete: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    val activeTasks = tasks.filter { it.status == TaskStatus.IN_PROGRESS }
    val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }
    val todayDone = tasks.filter {
        it.status == TaskStatus.DONE && isToday(it.completedAt ?: 0L)
    }

    if (activeTasks.isEmpty() && pendingTasks.isEmpty()) {
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
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = insets.calculateTopPadding() + 8.dp,
            bottom = insets.calculateBottomPadding() + 8.dp
        )
    ) {
        if (activeTasks.isNotEmpty()) {
            items(activeTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTap = onTap,
                    onLongPress = onLongPress,
                    onSwipeDone = onSwipeDone,
                    onSwipeDelete = onSwipeDelete,
                    modifier = Modifier.animateItem()
                )
                HorizontalDivider(color = WillBorder, thickness = 1.dp)
            }
        }

        if (pendingTasks.isNotEmpty()) {
            item { SectionHeader("pending") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            items(pendingTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTap = onTap,
                    onLongPress = onLongPress,
                    onSwipeDone = onSwipeDone,
                    onSwipeDelete = onSwipeDelete,
                    modifier = Modifier.animateItem()
                )
                HorizontalDivider(color = WillBorder, thickness = 1.dp)
            }
        }

        if (todayDone.isNotEmpty()) {
            item { SectionHeader("done today") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            items(todayDone, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTap = onTap,
                    onLongPress = onLongPress,
                    onSwipeDone = onSwipeDone,
                    onSwipeDelete = onSwipeDelete,
                    modifier = Modifier.animateItem()
                )
                HorizontalDivider(color = WillBorder, thickness = 1.dp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TaskItem(
    task: Task,
    onTap: (Task) -> Unit,
    onLongPress: (Task) -> Unit,
    onSwipeDone: (Task) -> Unit,
    onSwipeDelete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onSwipeDone(task); true }
                SwipeToDismissBoxValue.EndToStart -> { onSwipeDelete(task); true }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeBackground(dismissState) },
        modifier = modifier
    ) {
        val context = LocalContext.current
        val appName = remember(task.sourcePackage) {
            runCatching {
                val info = context.packageManager.getApplicationInfo(task.sourcePackage, 0)
                context.packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(task.sourcePackage)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .combinedClickable(
                    onClick = { onTap(task) },
                    onLongClick = { onLongPress(task) }
                )
                .heightIn(min = Spacing.touchTarget)
                .padding(vertical = Spacing.md, horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskStatusIndicator(
                status = task.status,
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
                    text = "$appName · ${formatTimestamp(task.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(dismissState: androidx.compose.material3.SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val progress = dismissState.progress

    val backgroundColor = when (direction) {
        SwipeToDismissBoxValue.StartToEnd ->
            lerp(Color.Transparent, WillSuccess, progress.coerceIn(0f, 1f))
        SwipeToDismissBoxValue.EndToStart ->
            lerp(Color.Transparent, WillDestructive, progress.coerceIn(0f, 1f))
        else -> Color.Transparent
    }

    val labelAlpha = ((progress - 0.25f) / 0.15f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = Spacing.lg),
        contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd)
            Alignment.CenterEnd else Alignment.CenterStart
    ) {
        when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(labelAlpha)
                ) {
                    Text(
                        "done",
                        style = MaterialTheme.typography.labelSmall,
                        color = WillTextPrimary
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = WillTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            SwipeToDismissBoxValue.EndToStart -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(labelAlpha)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = WillTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        "delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = WillTextPrimary
                    )
                }
            }
            else -> Unit
        }
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
