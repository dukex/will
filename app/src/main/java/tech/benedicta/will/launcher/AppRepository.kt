package tech.benedicta.will.launcher

interface AppRepository {
    fun getInstalledApps(): List<LauncherApp>
}
