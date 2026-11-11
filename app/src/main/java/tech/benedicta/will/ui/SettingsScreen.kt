package tech.benedicta.will.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.benedicta.will.notification.AppFilterViewModel
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillAccent
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillTextPrimary
import tech.benedicta.will.ui.theme.WillTextTertiary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onRerunOnboarding: () -> Unit = {},
    accessibilityServiceGranted: Boolean = false,
    onOpenAccessibilitySettings: () -> Unit = {},
    semanticSearchEnabled: Boolean = false,
    onSemanticSearchChange: (Boolean) -> Unit = {},
    viewModel: AppFilterViewModel
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safeDrawing.calculateTopPadding())
    ) {
        ScreenTopBar(
            title = "settings",
            onBack = onBack,
            backContentDescription = "back to home"
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        LazyColumn(Modifier.weight(1f)) {
            item {
                SectionHeader("apps that create tasks")
                HorizontalDivider(color = WillBorder, thickness = 1.dp)
            }
            if (filters.isEmpty()) {
                item {
                    Text(
                        text = "apps will appear here once they send notifications",
                        style = MaterialTheme.typography.labelSmall,
                        color = WillTextTertiary,
                        modifier = Modifier.padding(
                            horizontal = Spacing.xl,
                            vertical = Spacing.md
                        )
                    )
                    HorizontalDivider(color = WillBorder, thickness = 1.dp)
                }
            }
                items(filters, key = { it.packageName }) { filter ->
                    AppToggleRow(
                        appName = filter.appName,
                        packageName = filter.packageName,
                        isEnabled = filter.isEnabled,
                        onToggle = { viewModel.toggle(filter.packageName, it) }
                    )
                    HorizontalDivider(color = WillBorder, thickness = 1.dp)
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPrivacy() }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                            .semantics {
                                role = Role.Button
                                contentDescription = "data and privacy"
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "data & privacy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WillTextTertiary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = WillTextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onRerunOnboarding() }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                            .semantics {
                                role = Role.Button
                                contentDescription = "re-run onboarding"
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "re-run onboarding",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WillTextTertiary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = WillTextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                item {
                    val statusLabel = if (accessibilityServiceGranted) "on" else "off"
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAccessibilitySettings() }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                            .semantics {
                                role = Role.Button
                                contentDescription =
                                    "lifecycle tracking is $statusLabel; open system settings"
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "lifecycle tracking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WillTextTertiary
                            )
                            Text(
                                text = if (accessibilityServiceGranted) {
                                    "on — measures time-on-task and context switches"
                                } else {
                                    "off — turn on under Accessibility for accurate metrics"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = WillTextTertiary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = WillTextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                item {
                    val statusLabel = if (semanticSearchEnabled) "on" else "off"
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSemanticSearchChange(!semanticSearchEnabled) }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                            .semantics {
                                role = Role.Switch
                                contentDescription =
                                    "search by meaning is $statusLabel; tap to turn it " +
                                    if (semanticSearchEnabled) "off" else "on"
                            },
                        // Top, not centre: the explanation runs to three lines
                        // and the status belongs beside the name it describes.
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "search by meaning",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WillTextTertiary
                            )
                            Text(
                                text = if (semanticSearchEnabled) {
                                    "on — finds apps by what they are for, using Google's " +
                                        "on-device model. only when the name search finds nothing"
                                } else {
                                    "off — turning it on uses Google's on-device model, which " +
                                        "ships with its own libraries. nothing is sent anywhere"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = WillTextTertiary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (semanticSearchEnabled) WillAccent else WillTextTertiary
                        )
                    }
                }
        }
    }
}

@Composable
private fun AppToggleRow(
    appName: String,
    packageName: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
            .clickable { onToggle(!isEnabled) }
            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
            .semantics(mergeDescendants = true) {
                contentDescription = "$appName. task creation ${if (isEnabled) "on" else "off"}"
                customActions = listOf(
                    CustomAccessibilityAction(if (isEnabled) "disable task creation" else "enable task creation") {
                        onToggle(!isEnabled)
                        true
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = null,
            modifier = Modifier.semantics { hideFromAccessibility() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = WillTextPrimary,
                checkedTrackColor = WillAccent,
                uncheckedThumbColor = WillTextTertiary,
                uncheckedTrackColor = Color.Transparent,
                uncheckedBorderColor = WillBorder
            )
        )
    }
}
