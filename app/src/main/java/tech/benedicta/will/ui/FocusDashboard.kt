package tech.benedicta.will.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.benedicta.will.task.FocusDashboardViewModel
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillAccent
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary

@Composable
fun FocusDashboard(
    viewModel: FocusDashboardViewModel,
    onBack: () -> Unit
) {
    val metrics = viewModel.metrics
    val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safeDrawing.calculateTopPadding())
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = "back" }
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "focus",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Spacing.sm)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        if (metrics.isEmpty) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "nothing yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "complete tasks to see your focus score",
                        style = MaterialTheme.typography.labelSmall,
                        color = WillTextTertiary,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                item {
                    SectionHeader("today")
                    Column(Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.md)) {
                        Text(
                            text = "${metrics.focusScore}",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.semantics {
                                contentDescription = "focus score: ${metrics.focusScore} out of 100"
                            }
                        )
                        Text(
                            text = "focus score",
                            style = MaterialTheme.typography.labelSmall,
                            color = WillTextTertiary
                        )
                    }
                }

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                }

                item {
                    SectionHeader("time on task")
                    if (metrics.taskDurations.isNotEmpty()) {
                        val taskSummaries = metrics.taskDurations.joinToString { (title, _) -> title }
                        TaskBarChart(
                            taskDurations = metrics.taskDurations,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                                .semantics {
                                    contentDescription = "time on task chart: $taskSummaries"
                                }
                        )
                    }
                }

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                }

                item {
                    SectionHeader("context switches")
                    Column(Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.md)) {
                        Text(
                            text = "${metrics.contextSwitchCount}",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.semantics {
                                contentDescription = "${metrics.contextSwitchCount} app switches today"
                            }
                        )
                        Text(
                            text = "app switches today",
                            style = MaterialTheme.typography.labelSmall,
                            color = WillTextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskBarChart(
    taskDurations: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val barHeight = 24.dp
    val barGap = 4.dp
    val totalHeight = (barHeight + barGap) * taskDurations.size
    val density = LocalDensity.current

    Canvas(
        modifier = modifier.height(totalHeight)
    ) {
        val barHeightPx = with(density) { barHeight.toPx() }
        val gapPx = with(density) { barGap.toPx() }
        val barAreaWidth = size.width * 0.6f
        val labelX = barAreaWidth + with(density) { Spacing.sm.toPx() }

        val textPaint = android.graphics.Paint().apply {
            color = WillTextSecondary.toArgb()
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
        }

        taskDurations.forEachIndexed { index, (title, progress) ->
            val top = index * (barHeightPx + gapPx)
            val barWidth = barAreaWidth * progress.coerceIn(0f, 1f)

            drawRect(
                color = WillBorder,
                topLeft = Offset(0f, top),
                size = Size(barAreaWidth, barHeightPx)
            )

            if (barWidth > 0f) {
                drawRect(
                    color = WillAccent,
                    topLeft = Offset(0f, top),
                    size = Size(barWidth, barHeightPx)
                )
            }

            val truncated = if (title.length > 20) title.take(17) + "..." else title
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    truncated,
                    labelX,
                    top + barHeightPx * 0.7f,
                    textPaint
                )
            }
        }
    }
}
