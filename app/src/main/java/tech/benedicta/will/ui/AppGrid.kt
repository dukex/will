package tech.benedicta.will.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import tech.benedicta.will.launcher.LauncherApp
import tech.benedicta.will.task.Task
import tech.benedicta.will.ui.theme.Spacing

@Composable
fun AppGrid(
    apps: List<LauncherApp>,
    query: String,
    onQueryChange: (String) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit = { },
    onDismiss: () -> Unit = {},
    onLongPressApp: (LauncherApp) -> Unit = {},
    matchingTasks: List<Task> = emptyList(),
    onTapTask: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = insets.calculateTopPadding() + 8.dp,
            bottom = insets.calculateBottomPadding() + 16.dp,
            start = insets.calculateStartPadding(layoutDirection) + 24.dp,
            end = insets.calculateEndPadding(layoutDirection) + 24.dp,
        )
    ) {
        item {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        // One search box over both: what you still have to do comes before the
        // apps, because a task is more specific than the app it lives in.
        if (matchingTasks.isNotEmpty()) {
            item { SectionHeader("tasks") }
            items(matchingTasks, key = { "task-${it.id}" }) { task ->
                TaskSearchResult(
                    task = task,
                    onClick = {
                        onQueryChange("")
                        onDismiss()
                        onTapTask(task)
                    }
                )
            }
            item { SectionHeader("apps") }
        }
        items(apps, key = { it.packageName }) { app ->
            AppItem(
                app = app,
                onClick = {
                    onQueryChange("")
                    onDismiss()
                    onLaunchApp(app)
                },
                onLongPress = { onLongPressApp(app) }
            )
        }
    }
}

@Composable
private fun TaskSearchResult(task: Task, onClick: () -> Unit) {
    Text(
        text = task.title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .heightIn(min = Spacing.touchTarget)
            .padding(vertical = Spacing.md)
            .semantics {
                role = Role.Button
                contentDescription = "open task ${task.title}"
            }
    )
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier = modifier) {
        if (query.isEmpty()) {
            Text(
                text = "Search apps",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItem(app: LauncherApp, onClick: () -> Unit, onLongPress: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Text(
        text = app.name,
        style = MaterialTheme.typography.titleMedium,
        // Stated rather than inherited: every other text in the launcher names
        // its colour, and on true black an inherited default disappears.
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            )
            .padding(vertical = 12.dp)
    )
}