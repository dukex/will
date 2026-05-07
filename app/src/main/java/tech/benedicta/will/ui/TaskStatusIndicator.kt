package tech.benedicta.will.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import tech.benedicta.will.task.TaskStatus
import tech.benedicta.will.ui.theme.WillAccent
import tech.benedicta.will.ui.theme.WillDestructive
import tech.benedicta.will.ui.theme.WillSuccess
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary

@Composable
fun TaskStatusIndicator(status: TaskStatus, modifier: Modifier = Modifier) {
    val targetColor = when (status) {
        TaskStatus.PENDING -> WillTextTertiary
        TaskStatus.IN_PROGRESS -> WillAccent
        TaskStatus.PAUSED -> WillTextSecondary
        TaskStatus.DONE -> WillSuccess
    }
    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 100),
        label = "task_status_color"
    )
    Canvas(
        modifier = modifier
            .size(8.dp)
            .semantics { hideFromAccessibility() }
    ) {
        drawCircle(color = color)
    }
}
