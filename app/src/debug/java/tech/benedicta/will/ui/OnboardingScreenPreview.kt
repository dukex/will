package tech.benedicta.will.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.benedicta.will.infrastructure.memory.MemoryOnboardingRepository
import tech.benedicta.will.onboarding.OnboardingViewModel
import tech.benedicta.will.onboarding.PermissionInspector
import tech.benedicta.will.ui.theme.WillTheme

private class StaticPermissionInspector(
    private val default: Boolean,
    private val overlay: Boolean,
    private val listener: Boolean
) : PermissionInspector {
    override fun isDefaultLauncher(): Boolean = default
    override fun hasOverlayPermission(): Boolean = overlay
    override fun hasNotificationListener(): Boolean = listener
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "step 1 — default launcher")
@Composable
fun OnboardingStepDefaultLauncherPreview() {
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(
            MemoryOnboardingRepository(),
            StaticPermissionInspector(default = false, overlay = false, listener = false)
        )
    )
    WillTheme { OnboardingScreen(viewModel, {}, {}, {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "step 2 — overlay")
@Composable
fun OnboardingStepOverlayPreview() {
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(
            MemoryOnboardingRepository(),
            StaticPermissionInspector(default = true, overlay = false, listener = false)
        )
    )
    WillTheme { OnboardingScreen(viewModel, {}, {}, {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "step 3 — notifications (optional)")
@Composable
fun OnboardingStepNotificationsPreview() {
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(
            MemoryOnboardingRepository(),
            StaticPermissionInspector(default = true, overlay = true, listener = false)
        )
    )
    WillTheme { OnboardingScreen(viewModel, {}, {}, {}) }
}
