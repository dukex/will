package tech.benedicta.will.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import tech.benedicta.will.ui.theme.Spacing

/**
 * Shared top bar for the secondary screens (settings, dashboard, done,
 * privacy). Renders a back arrow whose **visible** left edge aligns with
 * [Spacing.screenHorizontal], plus an optional trailing slot for things
 * like a count or icon button.
 *
 * The Material3 [IconButton] is 40dp wide with a 24dp icon centred — that
 * leaves 8dp of internal padding before the visible icon. To make the
 * arrow line up with the rest of the screen content (which sits at
 * `screenHorizontal`), the row's start padding has to be
 * `screenHorizontal - 8dp`.
 *
 * Status-bar insets are NOT applied here — every parent already handles
 * `safeDrawing.calculateTopPadding()` on its outermost Column.
 */
@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String = "back",
    titleStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.screenHorizontal - BackIconInset,
                end = Spacing.screenHorizontal,
                top = Spacing.sm,
                bottom = Spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.semantics { contentDescription = backContentDescription }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = title,
            style = titleStyle,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

/** M3 IconButton width (40dp) − icon size (24dp) split across both sides. */
private val BackIconInset = 8.dp
