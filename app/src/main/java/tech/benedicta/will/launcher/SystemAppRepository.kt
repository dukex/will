package tech.benedicta.will.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class SystemAppRepository(private val packageManager: PackageManager) : AppRepository {
    override fun getInstalledApps(): List<LauncherApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        return activities
            .map { LauncherApp(it.activityInfo.packageName, it.loadLabel(packageManager).toString()) }
            .sortedBy { it.name }
    }
}
