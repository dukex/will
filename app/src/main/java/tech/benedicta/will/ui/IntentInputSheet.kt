package tech.benedicta.will.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillAccent
import tech.benedicta.will.ui.theme.WillSurface2
import tech.benedicta.will.ui.theme.WillTextPrimary
import tech.benedicta.will.ui.theme.WillTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentInputSheet(
    onDismiss: () -> Unit,
    onAddTask: (title: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WillSurface2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(top = Spacing.lg)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = WillTextPrimary),
                cursorBrush = SolidColor(WillAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            ) { innerTextField ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = "what do you need to do?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WillTextTertiary
                        )
                    }
                    innerTextField()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl)
                    .clickable(enabled = text.isNotBlank()) { onAddTask(text) }
                    .alpha(if (text.isBlank()) 0.3f else 1f)
                    .padding(vertical = 14.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "add task $text"
                    }
            ) {
                Text(
                    text = "add task",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WillAccent
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(vertical = 14.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "cancel"
                    }
            ) {
                Text(
                    text = "cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WillTextTertiary
                )
            }
        }
    }
}
