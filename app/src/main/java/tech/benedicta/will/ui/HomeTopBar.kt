package tech.benedicta.will.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tech.benedicta.will.task.Task
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeTopBar(
    onAddTask: () -> Unit = {},
    onOpenFilter: () -> Unit = {},
    onOpenDashboard: () -> Unit = {},
    // Legacy params kept for backward compatibility — no longer rendered
    activeTask: Task? = null,
    onTaskDone: () -> Unit = {},
    onTaskLater: () -> Unit = {},
    onTaskDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - (now % 60_000L))
            now = System.currentTimeMillis()
        }
    }

    val topPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val dateString = formatDate(now, Locale.getDefault()).lowercase()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding + 8.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateString,
            style = MaterialTheme.typography.bodyMedium,
            color = WillTextSecondary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onAddTask) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "add task",
                tint = WillTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(onClick = onOpenFilter) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "notification filter",
                tint = WillTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(onClick = onOpenDashboard) {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = "focus dashboard",
                tint = WillTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    HorizontalDivider(color = WillBorder, thickness = 1.dp)
}

fun formatTime(millis: Long, locale: Locale): String =
    SimpleDateFormat("HH:mm", locale).format(Date(millis))

fun formatDate(millis: Long, locale: Locale): String =
    SimpleDateFormat("EEE, MMM d", locale).format(Date(millis))
