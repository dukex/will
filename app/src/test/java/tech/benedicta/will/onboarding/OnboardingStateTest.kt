package tech.benedicta.will.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStateTest {

    @Test
    fun `default state starts on the default-launcher step with nothing granted`() {
        val state = OnboardingState()

        assertEquals(OnboardingStep.DefaultLauncher, state.step)
        assertFalse(state.isDefaultLauncher)
        assertFalse(state.hasOverlayPermission)
        assertFalse(state.hasNotificationListener)
        assertFalse(state.isComplete)
    }

    @Test
    fun `next advances DefaultLauncher to Overlay`() {
        val state = OnboardingState(step = OnboardingStep.DefaultLauncher)

        val advanced = state.next()

        assertEquals(OnboardingStep.Overlay, advanced.step)
    }

    @Test
    fun `next advances Overlay to Notifications`() {
        val state = OnboardingState(step = OnboardingStep.Overlay)

        val advanced = state.next()

        assertEquals(OnboardingStep.Notifications, advanced.step)
    }

    @Test
    fun `next advances Notifications to Done`() {
        val state = OnboardingState(step = OnboardingStep.Notifications)

        val advanced = state.next()

        assertEquals(OnboardingStep.Done, advanced.step)
    }

    @Test
    fun `next on Done is idempotent`() {
        val state = OnboardingState(step = OnboardingStep.Done)

        val advanced = state.next()

        assertEquals(OnboardingStep.Done, advanced.step)
    }

    @Test
    fun `only the notifications step can be skipped`() {
        assertFalse(OnboardingState(step = OnboardingStep.DefaultLauncher).canSkip)
        assertFalse(OnboardingState(step = OnboardingStep.Overlay).canSkip)
        assertTrue(OnboardingState(step = OnboardingStep.Notifications).canSkip)
        assertFalse(OnboardingState(step = OnboardingStep.Done).canSkip)
    }

    @Test
    fun `isComplete is true only on Done`() {
        assertFalse(OnboardingState(step = OnboardingStep.DefaultLauncher).isComplete)
        assertFalse(OnboardingState(step = OnboardingStep.Overlay).isComplete)
        assertFalse(OnboardingState(step = OnboardingStep.Notifications).isComplete)
        assertTrue(OnboardingState(step = OnboardingStep.Done).isComplete)
    }

    @Test
    fun `advanceToFirstUngranted lands on the first ungranted required step`() {
        val state = OnboardingState(
            isDefaultLauncher = false,
            hasOverlayPermission = false,
            hasNotificationListener = false
        )

        val advanced = state.advanceToFirstUngranted()

        assertEquals(OnboardingStep.DefaultLauncher, advanced.step)
    }

    @Test
    fun `advanceToFirstUngranted skips default launcher when it is already set`() {
        val state = OnboardingState(
            isDefaultLauncher = true,
            hasOverlayPermission = false,
            hasNotificationListener = false
        )

        val advanced = state.advanceToFirstUngranted()

        assertEquals(OnboardingStep.Overlay, advanced.step)
    }

    @Test
    fun `advanceToFirstUngranted lands on notifications when only the optional grant is missing`() {
        val state = OnboardingState(
            isDefaultLauncher = true,
            hasOverlayPermission = true,
            hasNotificationListener = false
        )

        val advanced = state.advanceToFirstUngranted()

        assertEquals(OnboardingStep.Notifications, advanced.step)
    }

    @Test
    fun `advanceToFirstUngranted lands on Done when everything required is granted regardless of optional`() {
        val state = OnboardingState(
            isDefaultLauncher = true,
            hasOverlayPermission = true,
            hasNotificationListener = true
        )

        val advanced = state.advanceToFirstUngranted()

        assertEquals(OnboardingStep.Done, advanced.step)
    }

    @Test
    fun `default-launcher step is required`() {
        assertTrue(OnboardingStep.DefaultLauncher.required)
    }

    @Test
    fun `overlay step is required`() {
        assertTrue(OnboardingStep.Overlay.required)
    }

    @Test
    fun `notifications step is optional`() {
        assertFalse(OnboardingStep.Notifications.required)
    }
}
