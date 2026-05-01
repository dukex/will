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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onRerunOnboarding: () -> Unit = {},
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
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = "back to home" }
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "settings",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Spacing.sm)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        if (filters.isEmpty()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "no apps yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "apps will appear here once they send notifications",
                        style = MaterialTheme.typography.labelSmall,
                        color = WillTextTertiary,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                item {
                    SectionHeader("apps that create tasks")
                    HorizontalDivider(color = WillBorder, thickness = 1.dp)
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
