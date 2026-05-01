package tech.benedicta.will.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionInspectorTest {

    @Test
    fun `snapshot reflects every grant of a fully-onboarded user`() {
        val inspector = FakePermissionInspector(
            defaultLauncher = true,
            overlay = true,
            notificationListener = true
        )

        val snapshot = inspector.snapshot()

        assertTrue(snapshot.isDefaultLauncher)
        assertTrue(snapshot.hasOverlayPermission)
        assertTrue(snapshot.hasNotificationListener)
    }

    @Test
    fun `snapshot reflects the lack of grants for a fresh install`() {
        val inspector = FakePermissionInspector(
            defaultLauncher = false,
            overlay = false,
            notificationListener = false
        )

        val snapshot = inspector.snapshot()

        assertFalse(snapshot.isDefaultLauncher)
        assertFalse(snapshot.hasOverlayPermission)
        assertFalse(snapshot.hasNotificationListener)
    }

    @Test
    fun `snapshot resumes onboarding at the first ungranted step`() {
        val inspector = FakePermissionInspector(
            defaultLauncher = true,
            overlay = false,
            notificationListener = false
        )

        val resumed = inspector.snapshot().advanceToFirstUngranted()

        assertEquals(OnboardingStep.Overlay, resumed.step)
    }
}

private class FakePermissionInspector(
    private val defaultLauncher: Boolean,
    private val overlay: Boolean,
    private val notificationListener: Boolean
) : PermissionInspector {
    override fun isDefaultLauncher(): Boolean = defaultLauncher
    override fun hasOverlayPermission(): Boolean = overlay
    override fun hasNotificationListener(): Boolean = notificationListener
}
