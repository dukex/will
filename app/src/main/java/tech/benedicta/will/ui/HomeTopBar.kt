package tech.benedicta.will.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeTopBar(modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - (now % 60_000L))
            now = System.currentTimeMillis()
        }
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
        Text(
            text = formatDate(now, Locale.getDefault()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

fun formatTime(millis: Long, locale: Locale): String =
    SimpleDateFormat("HH:mm", locale).format(Date(millis))

fun formatDate(millis: Long, locale: Locale): String =
    SimpleDateFormat("EEE, d MMM", locale).format(Date(millis))
