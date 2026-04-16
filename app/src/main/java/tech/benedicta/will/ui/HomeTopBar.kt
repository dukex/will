package tech.benedicta.will.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tech.benedicta.will.task.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeTopBar(
    activeTask: Task? = null,
    onTaskDone: () -> Unit = {},
    onTaskLater: () -> Unit = {},
    onTaskDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var battery by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - (now % 60_000L))
            now = System.currentTimeMillis()
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                battery = batteryPercentage(level, scale)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initial = context.registerReceiver(receiver, filter)
        if (initial != null) {
            val level = initial.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = initial.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            battery = batteryPercentage(level, scale)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    val topPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding + 16.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
    ) {
        Text(
            text = formatTime(now, Locale.getDefault()),
            style = MaterialTheme.typography.displaySmall
        )
        Row(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = formatDate(now, Locale.getDefault()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "  |  $battery%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (activeTask != null) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeTask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onTaskDone)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Later",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onTaskLater)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable(onClick = onTaskDelete)
                )
            }
        }
    }
}

fun batteryPercentage(level: Int, scale: Int): Int =
    if (scale == 0) 0 else level * 100 / scale

fun formatTime(millis: Long, locale: Locale): String =
    SimpleDateFormat("HH:mm", locale).format(Date(millis))

fun formatDate(millis: Long, locale: Locale): String =
    SimpleDateFormat("EEE, d MMM", locale).format(Date(millis))
