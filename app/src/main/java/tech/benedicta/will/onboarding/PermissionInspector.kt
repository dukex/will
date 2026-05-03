package tech.benedicta.will.onboarding

/**
 * Snapshot of the three platform grants the onboarding flow cares about.
 * Implementations resolve the values against live system state — they
 * change when the user toggles a setting outside Will, so callers should
 * re-read on resume rather than caching for the lifetime of a screen.
 */
interface PermissionInspector {
    fun isDefaultLauncher(): Boolean
    fun hasOverlayPermission(): Boolean
    fun hasNotificationListener(): Boolean

    /**
     * Whether Will's optional `WillAccessibilityService` is enabled by the
     * user. Used to render a status row in settings; not required for the
     * launcher to work, so it is not part of the onboarding gate.
     */
    fun hasAccessibilityService(): Boolean = false

    fun snapshot(): OnboardingState = OnboardingState(
        isDefaultLauncher = isDefaultLauncher(),
        hasOverlayPermission = hasOverlayPermission(),
        hasNotificationListener = hasNotificationListener()
    )
}
