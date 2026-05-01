package tech.benedicta.will.onboarding

/**
 * Discrete steps in the first-run onboarding. Order is the user-facing order;
 * required steps gate completion, optional steps may be skipped.
 */
enum class OnboardingStep(val required: Boolean) {
    DefaultLauncher(required = true),
    Overlay(required = true),
    Notifications(required = false),
    Done(required = false)
}

/**
 * Pure state for the onboarding flow. Holds the current step plus the most
 * recently observed permission grants. Kept Android-free so the state machine
 * can be exercised in unit tests without robolectric or instrumentation.
 */
data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.DefaultLauncher,
    val isDefaultLauncher: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasNotificationListener: Boolean = false
) {
    val canSkip: Boolean
        get() = !step.required && step != OnboardingStep.Done

    val isComplete: Boolean
        get() = step == OnboardingStep.Done

    fun next(): OnboardingState = when (step) {
        OnboardingStep.DefaultLauncher -> copy(step = OnboardingStep.Overlay)
        OnboardingStep.Overlay -> copy(step = OnboardingStep.Notifications)
        OnboardingStep.Notifications -> copy(step = OnboardingStep.Done)
        OnboardingStep.Done -> this
    }

    /**
     * Resolve the first step that still needs the user's attention. Required
     * steps gate progression; optional steps land here only if every required
     * one is already satisfied. If everything required is granted, returns
     * [OnboardingStep.Done] — the caller should treat that as "skip onboarding".
     */
    fun advanceToFirstUngranted(): OnboardingState = when {
        !isDefaultLauncher -> copy(step = OnboardingStep.DefaultLauncher)
        !hasOverlayPermission -> copy(step = OnboardingStep.Overlay)
        !hasNotificationListener -> copy(step = OnboardingStep.Notifications)
        else -> copy(step = OnboardingStep.Done)
    }
}
