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
import tech.benedicta.will.launcher.SemanticSearchStatus
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
    semanticSearchStatus: SemanticSearchStatus = SemanticSearchStatus.OFF,
    onDownloadSemanticModel: () -> Unit = {},
    derivedIntentionsEnabled: Boolean = false,
    onDerivedIntentionsChange: (Boolean) -> Unit = {},
    modelOnThisPhone: Boolean = false,
    modelTriageEnabled: Boolean = false,
    onModelTriageChange: (Boolean) -> Unit = {},
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
                    val statusLabel = when (semanticSearchStatus) {
                        SemanticSearchStatus.OFF -> "off"
                        SemanticSearchStatus.ON -> "on"
                        SemanticSearchStatus.NEEDS_DOWNLOAD -> "download"
                        SemanticSearchStatus.DOWNLOADING -> "downloading"
                        SemanticSearchStatus.UNSUPPORTED -> "unsupported"
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = semanticSearchStatus.canToggle) {
                                if (semanticSearchStatus.canStartDownload) {
                                    onDownloadSemanticModel()
                                } else {
                                    onSemanticSearchChange(!semanticSearchEnabled)
                                }
                            }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                            .semantics {
                                role = Role.Switch
                                contentDescription = when (semanticSearchStatus) {
                                    SemanticSearchStatus.NEEDS_DOWNLOAD ->
                                        "search by meaning needs its model; tap to download it"
                                    SemanticSearchStatus.DOWNLOADING ->
                                        "search by meaning is downloading its model"
                                    SemanticSearchStatus.UNSUPPORTED ->
                                        "search by meaning is not supported on this phone"
                                    SemanticSearchStatus.ON -> "search by meaning is on; tap to turn it off"
                                    SemanticSearchStatus.OFF -> "search by meaning is off; tap to turn it on"
                                }
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
                                text = when (semanticSearchStatus) {
                                    SemanticSearchStatus.ON ->
                                        "on — finds apps by what they are for, using Google's " +
                                            "on-device model. only when the name search finds nothing"
                                    SemanticSearchStatus.NEEDS_DOWNLOAD ->
                                        "the model is not on this phone yet. tap to download it, " +
                                            "which uses your connection once"
                                    SemanticSearchStatus.DOWNLOADING ->
                                        "downloading the model. search keeps working by name " +
                                            "while it arrives"
                                    SemanticSearchStatus.UNSUPPORTED ->
                                        "this phone cannot run the on-device model. search by " +
                                            "name works as it always has"
                                    SemanticSearchStatus.OFF ->
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
                            color = if (semanticSearchStatus.canStartDownload ||
                                semanticSearchStatus == SemanticSearchStatus.ON
                            ) WillAccent else WillTextTertiary
                        )
                    }
                }
                item {
                    // Its own switch, never folded into the one above:
                    // agreeing to send the model a search term is not
                    // agreeing to send it what a notification said.
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = modelOnThisPhone) {
                                onDerivedIntentionsChange(!derivedIntentionsEnabled)
                            }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                            .semantics {
                                role = Role.Switch
                                contentDescription = when {
                                    !modelOnThisPhone ->
                                        "reading notifications needs the on-device model, " +
                                            "which is not on this phone"
                                    derivedIntentionsEnabled ->
                                        "reading notifications is on; tap to turn it off"
                                    else -> "reading notifications is off; tap to turn it on"
                                }
                            },
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "read what a notification asks for",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WillTextTertiary
                            )
                            Text(
                                text = when {
                                    !modelOnThisPhone ->
                                        "needs the on-device model, which is not on this phone. " +
                                            "notifications arrive with the title they were sent with"
                                    derivedIntentionsEnabled ->
                                        "on — a notification only your words can explain, like " +
                                            "\"confirme sua presença\", becomes what it is asking " +
                                            "for. the model reads it on your phone and the " +
                                            "original title is kept"
                                    else ->
                                        "off — a message or a missed call already becomes an " +
                                            "intention without any model. turning this on lets " +
                                            "the on-device model read the rest. it never hides a " +
                                            "notification"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = WillTextTertiary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Text(
                            text = when {
                                !modelOnThisPhone -> "unsupported"
                                derivedIntentionsEnabled -> "on"
                                else -> "off"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (derivedIntentionsEnabled) WillAccent else WillTextTertiary
                        )
                    }
                }
                item {
                    // The third switch, and the only one that decides whether
                    // something reaches the user at all. Letting either of the
                    // other two carry it would be deciding that for them.
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = modelOnThisPhone) {
                                onModelTriageChange(!modelTriageEnabled)
                            }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                            .semantics {
                                role = Role.Switch
                                contentDescription = when {
                                    !modelOnThisPhone ->
                                        "hiding housekeeping needs the on-device model, " +
                                            "which is not on this phone"
                                    modelTriageEnabled ->
                                        "hiding housekeeping is on; tap to turn it off"
                                    else -> "hiding housekeeping is off; tap to turn it on"
                                }
                            },
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "hide housekeeping notifications",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WillTextTertiary
                            )
                            Text(
                                text = when {
                                    !modelOnThisPhone ->
                                        "needs the on-device model, which is not on this phone. " +
                                            "the built-in list still catches the usual chatter"
                                    modelTriageEnabled ->
                                        "on — the model reads what the built-in list missed and " +
                                            "keeps it off your feed. everything it hides is " +
                                            "listed in data & privacy, with a way back. when it " +
                                            "is unsure, it keeps"
                                    else ->
                                        "off — the built-in list and what you have thrown away " +
                                            "already filter your feed. turning this on lets the " +
                                            "on-device model hide what they missed. this is the " +
                                            "only switch that can keep something from you"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = WillTextTertiary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Text(
                            text = when {
                                !modelOnThisPhone -> "unsupported"
                                modelTriageEnabled -> "on"
                                else -> "off"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (modelTriageEnabled) WillAccent else WillTextTertiary
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
