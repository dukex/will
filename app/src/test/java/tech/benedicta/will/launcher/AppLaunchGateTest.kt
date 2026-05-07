package tech.benedicta.will.launcher

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.benedicta.will.task.Task

class AppLaunchGateTest {

    @Test
    fun `with null active task returns PromptForIntent`() {
        val action = AppLaunchGate.decide(
            activeTask = null,
            packageName = "com.whatsapp",
            appName = "WhatsApp"
        )

        assertEquals(
            AppLaunchAction.PromptForIntent(packageName = "com.whatsapp", appName = "WhatsApp"),
            action
        )
    }

    @Test
    fun `with non-null active task returns LaunchDirectly`() {
        val active = Task(
            title = "Reply Maria",
            text = "",
            sourcePackage = "com.whatsapp",
            createdAt = 0L,
            startedAt = 100L
        )

        val action = AppLaunchGate.decide(
            activeTask = active,
            packageName = "com.android.chrome",
            appName = "Chrome"
        )

        assertEquals(AppLaunchAction.LaunchDirectly(packageName = "com.android.chrome"), action)
    }

    @Test
    fun `LaunchDirectly carries the target packageName, not the active task's source`() {
        val active = Task(
            title = "Email",
            text = "",
            sourcePackage = "com.google.android.gm",
            createdAt = 0L,
            startedAt = 100L
        )

        val action = AppLaunchGate.decide(active, "com.spotify.music", "Spotify")

        assertEquals(AppLaunchAction.LaunchDirectly(packageName = "com.spotify.music"), action)
    }

    @Test
    fun `PromptForIntent carries both packageName and appName`() {
        val action = AppLaunchGate.decide(null, "org.telegram.messenger", "Telegram")

        assertEquals(
            AppLaunchAction.PromptForIntent(
                packageName = "org.telegram.messenger",
                appName = "Telegram"
            ),
            action
        )
    }
}