package tech.benedicta.will.ui

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.benedicta.will.launcher.LauncherApp
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillTextTertiary

fun LazyListScope.pinnedAppsItems(
    pinnedApps: List<LauncherApp>,
    onUnpin: (LauncherApp) -> Unit
) {
    if (pinnedApps.isEmpty()) return

    items(pinnedApps, key = { "pinned:${it.packageName}" }) { app ->
        PinnedAppItem(app = app, onUnpin = onUnpin)
        HorizontalDivider(color = WillBorder, thickness = 1.dp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedAppItem(
    app: LauncherApp,
    onUnpin: (LauncherApp) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    val intent = context.packageManager
                        .getLaunchIntentForPackage(app.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ?.let { context.startActivity(it) }
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnpin(app)
                }
            )
            .heightIn(min = Spacing.touchTarget)
            .padding(vertical = Spacing.md, horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.touchTarget)
                .padding(end = Spacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WillTextTertiary)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}