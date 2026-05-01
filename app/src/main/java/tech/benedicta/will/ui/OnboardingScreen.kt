package tech.benedicta.will.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.benedicta.will.onboarding.OnboardingState
import tech.benedicta.will.onboarding.OnboardingStep
import tech.benedicta.will.onboarding.OnboardingViewModel
import tech.benedicta.will.ui.theme.Spacing

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onSetDefaultLauncher: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantNotificationListener: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // The host (MainActivity) observes state.isComplete and the persisted flag
    // separately and re-renders to WillScreen when either flips. No callback is
    // required here, which keeps the composable side-effect-free.
    if (state.isComplete) return

    val copy = stepCopy(state.step)
    val action: () -> Unit = when (state.step) {
        OnboardingStep.DefaultLauncher -> onSetDefaultLauncher
        OnboardingStep.Overlay -> onGrantOverlay
        OnboardingStep.Notifications -> onGrantNotificationListener
        OnboardingStep.Done -> { -> }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xxl),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StepIndicator(state)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Text(
                    text = copy.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = copy.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                PrimaryAction(
                    label = copy.actionLabel,
                    onClick = action
                )
                if (state.canSkip) {
                    SkipAction(onClick = viewModel::skip)
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(state: OnboardingState) {
    val total = OnboardingStep.values().count { it != OnboardingStep.Done }
    val index = when (state.step) {
        OnboardingStep.DefaultLauncher -> 1
        OnboardingStep.Overlay -> 2
        OnboardingStep.Notifications -> 3
        OnboardingStep.Done -> total
    }
    Text(
        text = "step $index of $total",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PrimaryAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
        )
    }
}

@Composable
private fun SkipAction(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            .background(Color.Transparent)
    ) {
        Text(
            text = "skip for now",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private data class StepCopy(val title: String, val body: String, val actionLabel: String)

private fun stepCopy(step: OnboardingStep): StepCopy = when (step) {
    OnboardingStep.DefaultLauncher -> StepCopy(
        title = "make Will your home",
        body = "Will replaces the home screen with a task feed. set it as default so the home button opens Will instead of the stock launcher.",
        actionLabel = "set as default"
    )
    OnboardingStep.Overlay -> StepCopy(
        title = "allow the active-task overlay",
        body = "when you tap a task, Will shows a small floating bubble on top of the source app so you can mark it done, snooze it, or delete it without leaving the app you opened.",
        actionLabel = "open overlay settings"
    )
    OnboardingStep.Notifications -> StepCopy(
        title = "let Will read your notifications",
        body = "this is the core feature: every notification becomes an actionable task in your feed. it is optional — Will still works as a launcher without it, you just lose the auto-task pipeline.",
        actionLabel = "open listener settings"
    )
    OnboardingStep.Done -> StepCopy(
        title = "ready",
        body = "",
        actionLabel = "continue"
    )
}
