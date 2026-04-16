package tech.benedicta.will.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillDestructive
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary

private data class PermissionItem(val name: String, val rationale: String)

private val permissionsList = listOf(
    PermissionItem("notification listener", "to capture notifications as tasks"),
    PermissionItem("accessibility service", "to detect when tasks are completed"),
    PermissionItem("microphone (optional)", "for voice task input"),
    PermissionItem("draw over other apps", "for the active task overlay"),
)

@Composable
fun PrivacySettings(
    onBack: () -> Unit,
    onDeleteAllTasks: () -> Unit
) {
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
                modifier = Modifier.semantics { contentDescription = "back to notifications" }
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "data & privacy",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Spacing.sm)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        LazyColumn(Modifier.fillMaxSize()) {
            item { SectionHeader("what will stores") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item { DataRow("task title and source app") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item { DataRow("created, started, and completed times") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item { DataRow("app switch count per task") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item { DataRow("notification filter preferences") }

            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item { SectionHeader("where it stays") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item {
                Text(
                    text = "on this device only. no accounts, no cloud sync, no analytics, no third-party data sharing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WillTextSecondary,
                    modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.md)
                )
            }

            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item { SectionHeader("permissions used") }
            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }

            items(permissionsList) { permission ->
                PermissionRow(name = permission.name, rationale = permission.rationale)
                HorizontalDivider(color = WillBorder, thickness = 1.dp)
            }

            item { HorizontalDivider(color = WillBorder, thickness = 1.dp) }
            item { DeleteHistoryRow(onDeleteAllTasks) }
        }
    }
}

@Composable
private fun DataRow(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
    )
}

@Composable
private fun PermissionRow(name: String, rationale: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = rationale,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun DeleteHistoryRow(onDeleteAllTasks: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showCleared by remember { mutableStateOf(false) }

    LaunchedEffect(showCleared) {
        if (showCleared) {
            delay(1500)
            showCleared = false
            confirmDelete = false
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
            .clickable {
                when {
                    showCleared -> Unit
                    confirmDelete -> {
                        onDeleteAllTasks()
                        showCleared = true
                    }
                    else -> confirmDelete = true
                }
            }
            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            showCleared -> Text(
                text = "history cleared",
                style = MaterialTheme.typography.bodyMedium,
                color = WillTextSecondary
            )
            confirmDelete -> {
                Text(
                    text = "confirm? this cannot be undone",
                    style = MaterialTheme.typography.bodySmall,
                    color = WillDestructive,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "cancel",
                    style = MaterialTheme.typography.bodySmall,
                    color = WillTextTertiary,
                    modifier = Modifier
                        .clickable { confirmDelete = false }
                        .padding(start = Spacing.lg)
                        .semantics { contentDescription = "cancel delete" }
                )
            }
            else -> Text(
                text = "delete all task history",
                style = MaterialTheme.typography.bodyMedium,
                color = WillDestructive,
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "delete all task history"
                }
            )
        }
    }
}
