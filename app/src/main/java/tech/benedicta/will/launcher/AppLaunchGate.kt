package tech.benedicta.will.launcher

import tech.benedicta.will.task.Task

sealed interface AppLaunchAction {
    data class LaunchDirectly(val packageName: String) : AppLaunchAction
    data class PromptForIntent(val packageName: String, val appName: String) : AppLaunchAction
}

object AppLaunchGate {
    fun decide(activeTask: Task?, packageName: String, appName: String): AppLaunchAction =
        if (activeTask != null) {
            AppLaunchAction.LaunchDirectly(packageName)
        } else {
            AppLaunchAction.PromptForIntent(packageName, appName)
        }
}