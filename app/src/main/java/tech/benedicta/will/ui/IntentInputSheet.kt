package tech.benedicta.will.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tech.benedicta.will.ui.theme.Spacing
import tech.benedicta.will.ui.theme.WillAccent
import tech.benedicta.will.ui.theme.WillBorder
import tech.benedicta.will.ui.theme.WillSurface2
import tech.benedicta.will.ui.theme.WillTextPrimary
import tech.benedicta.will.ui.theme.WillTextSecondary
import tech.benedicta.will.ui.theme.WillTextTertiary

sealed class NlpState {
    object Idle : NlpState()
    object Processing : NlpState()
    data class Suggestion(val text: String) : NlpState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentInputSheet(
    onDismiss: () -> Unit,
    onAddTask: (title: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf("") }
    var nlpState by remember { mutableStateOf<NlpState>(NlpState.Idle) }
    // MVP stub — replace with full SpeechRecognizer implementation in US-03
    var isListening by remember { mutableStateOf(false) }

    val micTint by animateColorAsState(
        targetValue = when {
            isListening -> WillAccent
            nlpState == NlpState.Processing -> WillTextTertiary
            else -> WillTextSecondary
        },
        animationSpec = tween(100),
        label = "mic_tint"
    )

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(text) {
        if (text.isBlank()) {
            nlpState = NlpState.Idle
            return@LaunchedEffect
        }
        nlpState = NlpState.Processing
        delay(300)
        val suggestion = extractNlpSuggestion(text)
        nlpState = if (suggestion != null) NlpState.Suggestion(suggestion) else NlpState.Idle
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
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .drawBehind {
                            drawLine(
                                color = WillBorder,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                )
                IconButton(
                    onClick = { isListening = !isListening }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = if (isListening) "listening, tap to stop" else "start voice input",
                        tint = micTint
                    )
                }
            }

            val nlpText = when {
                nlpState == NlpState.Processing -> "processing..."
                nlpState is NlpState.Suggestion -> "→ ${(nlpState as NlpState.Suggestion).text}"
                else -> ""
            }

            AnimatedVisibility(visible = nlpText.isNotEmpty()) {
                Text(
                    text = nlpText,
                    style = MaterialTheme.typography.labelSmall,
                    color = WillTextSecondary,
                    modifier = Modifier
                        .padding(top = Spacing.sm)
                        .semantics {
                            contentDescription = if (nlpText.isNotEmpty()) "suggested action: $nlpText" else ""
                        }
                )
            }

            HorizontalDivider(
                color = WillBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(top = Spacing.lg)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = text.isNotBlank()) { onAddTask(text) }
                    .alpha(if (text.isBlank()) 0.3f else 1f)
                    .padding(horizontal = Spacing.xl, vertical = 14.dp)
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

            HorizontalDivider(color = WillBorder, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(horizontal = Spacing.xl, vertical = 14.dp)
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

private fun extractNlpSuggestion(text: String): String? {
    val lower = text.lowercase()
    return when {
        lower.contains("slack") || lower.contains("reply") ->
            "open slack · ${text.take(30)}"
        lower.contains("whatsapp") || lower.contains("message") ->
            "open whatsapp · ${text.take(30)}"
        lower.contains("github") || lower.contains("pr") || lower.contains("pull request") ->
            "open github · ${text.take(30)}"
        lower.contains("email") || lower.contains("gmail") ->
            "open gmail · ${text.take(30)}"
        lower.contains("meet") || lower.contains("call") ->
            "open google meet · ${text.take(30)}"
        else -> null
    }
}
