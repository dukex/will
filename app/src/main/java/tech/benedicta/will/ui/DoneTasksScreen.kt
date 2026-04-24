package tech.benedicta.will.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.benedicta.will.task.Task
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillDestructive
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DoneTasksScreen(
    doneTasks: List<Task>,
    onBack: () -> Unit,
    onDelete: (Task) -> Unit,
    onReopen: (Task) -> Unit
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = insets.calculateTopPadding() + 8.dp, start = 8.dp, end = Spacing.xl, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "back",
                    tint = WillTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "done",
                style = MaterialTheme.typography.bodyMedium,
                color = WillTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${doneTasks.size}",
                style = MaterialTheme.typography.labelSmall,
                color = WillTextTertiary
            )
        }

        HorizontalDivider(color = WillBorder, thickness = 1.dp)

        if (doneTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "no completed tasks",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WillTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = insets.calculateBottomPadding() + 8.dp)
            ) {
                items(doneTasks, key = { it.id }) { task ->
                    DoneTaskItem(
                        task = task,
                        onDelete = onDelete,
                        onReopen = onReopen
                    )
                    HorizontalDivider(color = WillBorder, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun DoneTaskItem(
    task: Task,
    onDelete: (Task) -> Unit,
    onReopen: (Task) -> Unit
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
            .clickable { onReopen(task) }
            .heightIn(min = Spacing.touchTarget)
            .padding(vertical = Spacing.md, horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = WillTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$appName · ${formatDoneTimestamp(task.completedAt ?: task.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = WillTextTertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        IconButton(onClick = { onDelete(task) }) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "delete ${task.title}",
                tint = WillDestructive,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatDoneTimestamp(timestamp: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))