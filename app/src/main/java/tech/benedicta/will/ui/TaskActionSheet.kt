package tech.benedicta.will.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskStatus
import tech.benedicta.will.task.status
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillDestructive
import tech.benedicta.will.ui.theme.WillTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskActionSheet(
    task: Task,
    onDismiss: () -> Unit,
    onMarkDone: () -> Unit = {},
    onStart: () -> Unit = {},
    onPause: () -> Unit = {},
    onReopen: () -> Unit = {},
    onDelete: () -> Unit = {},
    // Legacy params kept for backward compatibility
    onDone: () -> Unit = {},
    onLater: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = Spacing.lg)
        ) {
            HorizontalDivider(
                color = WillBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.sm)
            )

            val contextLine = when (task.status) {
                TaskStatus.PENDING ->
                    "pending · created ${formatTime(task.createdAt, java.util.Locale.getDefault())} · ${relativeTime(task.createdAt)}"
                TaskStatus.IN_PROGRESS ->
                    "in progress · started ${formatTime(task.startedAt ?: task.createdAt, java.util.Locale.getDefault())} · ${relativeTime(task.startedAt ?: task.createdAt)}"
                TaskStatus.DONE ->
                    "done · completed ${formatTime(task.completedAt ?: task.createdAt, java.util.Locale.getDefault())} · ${relativeTime(task.completedAt ?: task.createdAt)}"
            }

            Text(
                text = contextLine,
                style = MaterialTheme.typography.labelSmall,
                color = WillTextSecondary,
                modifier = Modifier.padding(start = Spacing.xl, end = Spacing.xl, bottom = Spacing.md)
            )

            HorizontalDivider(color = WillBorder, thickness = 1.dp)

            when (task.status) {
                TaskStatus.PENDING -> {
                    ActionRow(
                        label = "start",
                        color = MaterialTheme.colorScheme.onBackground,
                        taskTitle = task.title,
                        onClick = { onStart(); onDismiss() }
                    )
                    ActionRow(
                        label = "mark done",
                        color = MaterialTheme.colorScheme.onBackground,
                        taskTitle = task.title,
                        onClick = { onMarkDone(); onDone(); onDismiss() }
                    )
                }
                TaskStatus.IN_PROGRESS -> {
                    ActionRow(
                        label = "mark done",
                        color = MaterialTheme.colorScheme.onBackground,
                        taskTitle = task.title,
                        onClick = { onMarkDone(); onDone(); onDismiss() }
                    )
                    ActionRow(
                        label = "pause",
                        color = MaterialTheme.colorScheme.onBackground,
                        taskTitle = task.title,
                        onClick = { onPause(); onDismiss() }
                    )
                }
                TaskStatus.DONE -> {
                    ActionRow(
                        label = "reopen",
                        color = MaterialTheme.colorScheme.onBackground,
                        taskTitle = task.title,
                        onClick = { onReopen(); onDismiss() }
                    )
                }
            }

            if (!confirmDelete) {
                ActionRow(
                    label = "delete",
                    color = WillDestructive,
                    taskTitle = task.title,
                    onClick = { confirmDelete = true }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl, vertical = Spacing.md),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "confirm delete?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WillDestructive,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDelete(); onDismiss() }
                    )
                    Spacer(Modifier.padding(horizontal = Spacing.sm))
                    Text(
                        text = "cancel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WillTextSecondary,
                        modifier = Modifier.clickable { confirmDelete = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    taskTitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "$label $taskTitle"
            }
            .padding(horizontal = Spacing.xl, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

private fun relativeTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    return when {
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "just now"
    }
}
