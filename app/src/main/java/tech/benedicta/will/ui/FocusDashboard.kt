package tech.benedicta.will.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import tech.benedicta.will.task.AttentionRange
import tech.benedicta.will.task.DayOfAttention
import tech.benedicta.will.task.Distraction
import tech.benedicta.will.task.FocusDashboardViewModel
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillAccent
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary

@Composable
fun FocusDashboard(
    viewModel: FocusDashboardViewModel,
    onBack: () -> Unit,
    range: AttentionRange = AttentionRange.WEEK,
    onRangeChange: (AttentionRange) -> Unit = {}
) {
    val metrics = viewModel.metrics
    val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safeDrawing.calculateTopPadding())
    ) {
        ScreenTopBar(
            title = "focus",
            onBack = onBack
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        if (metrics.isEmpty && !viewModel.hasTrend) {
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
                if (!metrics.isEmpty) {
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

                if (viewModel.hasTrend) {
                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 1.dp
                        )
                    }
                    item {
                        SectionHeader(if (range == AttentionRange.WEEK) "last 7 days" else "last 30 days")
                        Column(Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.md)) {
                            Text(
                                text = attentionDuration(viewModel.engagedThisRangeMillis),
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.semantics {
                                    contentDescription =
                                        "time on task over the range: " +
                                            attentionDuration(viewModel.engagedThisRangeMillis)
                                }
                            )
                            Text(
                                text = "time on task",
                                style = MaterialTheme.typography.labelSmall,
                                color = WillTextTertiary
                            )
                            Text(
                                text = "longest unbroken stretch " +
                                    attentionDuration(viewModel.bestStretchMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = WillTextSecondary,
                                modifier = Modifier.padding(top = Spacing.sm)
                            )
                            AttentionTrendChart(
                                days = viewModel.trend,
                                busiestDayMillis = viewModel.busiestDayMillis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Spacing.lg)
                            )
                            RangePicker(
                                selected = range,
                                onSelect = onRangeChange,
                                modifier = Modifier.padding(top = Spacing.lg)
                            )
                        }
                    }

                    if (viewModel.distractions.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 1.dp
                            )
                        }
                        item {
                            SectionHeader("what pulled you away")
                            Column(
                                Modifier.padding(
                                    horizontal = Spacing.xl,
                                    vertical = Spacing.md
                                )
                            ) {
                                viewModel.distractions.forEach { distraction ->
                                    DistractionRow(distraction)
                                }
                                Text(
                                    text = "times a task was open and you went elsewhere",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WillTextTertiary,
                                    modifier = Modifier.padding(top = Spacing.sm)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * One bar per day, so a week reads as a shape rather than a number. Bars are
 * drawn from a common baseline against the busiest day, and a day with no
 * attention keeps its slot: a gap is information, and dropping it would make
 * a fortnight off look like a fortnight of work.
 */
@Composable
private fun AttentionTrendChart(
    days: List<DayOfAttention>,
    busiestDayMillis: Long,
    modifier: Modifier = Modifier
) {
    val chartHeight = 96.dp
    val density = LocalDensity.current
    val labelDays = days.size <= LABELLED_DAY_LIMIT
    val dayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val summary = days.joinToString { day ->
        "${dayFormat.format(Date(day.dayStart))} ${attentionDuration(day.engagedMillis)}"
    }

    Canvas(
        modifier = modifier
            .height(chartHeight)
            .semantics { contentDescription = "time on task per day: $summary" }
    ) {
        val gapPx = with(density) { 4.dp.toPx() }
        val labelRoom = if (labelDays) with(density) { 16.dp.toPx() } else 0f
        val plotHeight = size.height - labelRoom
        val slot = (size.width + gapPx) / days.size
        val barWidth = (slot - gapPx).coerceAtLeast(1f)

        val textPaint = android.graphics.Paint().apply {
            color = WillTextTertiary.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        days.forEachIndexed { index, day ->
            val left = index * slot
            drawRect(
                color = WillBorder,
                topLeft = Offset(left, plotHeight - 1f),
                size = Size(barWidth, 1f)
            )

            val share = if (busiestDayMillis <= 0L) {
                0f
            } else {
                day.engagedMillis.toFloat() / busiestDayMillis.toFloat()
            }
            val barHeight = plotHeight * share.coerceIn(0f, 1f)
            if (barHeight > 0f) {
                drawRect(
                    color = WillAccent,
                    topLeft = Offset(left, plotHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }

            if (labelDays) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        dayFormat.format(Date(day.dayStart)).take(3),
                        left + barWidth / 2f,
                        size.height - 2f,
                        textPaint
                    )
                }
            }
        }
    }
}

/** Closes US-05's date-range filtering: today's figures stay, the window moves. */
@Composable
private fun RangePicker(
    selected: AttentionRange,
    onSelect: (AttentionRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        AttentionRange.entries.forEach { option ->
            val isSelected = option == selected
            Text(
                text = if (option == AttentionRange.WEEK) "7 days" else "30 days",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) WillAccent else WillTextTertiary,
                modifier = Modifier
                    .clickable(enabled = !isSelected) { onSelect(option) }
                    .padding(vertical = Spacing.sm, horizontal = Spacing.md)
                    .semantics {
                        contentDescription = if (isSelected) {
                            "showing ${option.days} days"
                        } else {
                            "show ${option.days} days"
                        }
                    }
            )
            Spacer(Modifier.width(Spacing.xs))
        }
    }
}

/**
 * One app and how often it interrupted. The count is read from the task log,
 * which has carried the app jumped to since US-12 without anything reading it.
 *
 * The app's own name when the phone can still resolve it, and the package name
 * when it cannot, which is what an app uninstalled since looks like.
 */
@Composable
private fun DistractionRow(distraction: Distraction) {
    val context = LocalContext.current
    val label = remember(distraction.packageName) {
        runCatching {
            context.packageManager
                .getApplicationLabel(
                    context.packageManager.getApplicationInfo(distraction.packageName, 0)
                )
                .toString()
        }.getOrDefault(distraction.packageName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Spacing.touchTarget)
            .semantics {
                contentDescription = "$label, ${distraction.interruptions} interruptions"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${distraction.interruptions}",
            style = MaterialTheme.typography.labelSmall,
            color = WillTextSecondary
        )
    }
}

/** Past this, a weekday under every bar is unreadable, so the labels go. */
private const val LABELLED_DAY_LIMIT = 7

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
