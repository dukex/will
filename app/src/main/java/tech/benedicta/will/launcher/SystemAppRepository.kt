package tech.benedicta.will.launcher

import android.content.Intent
import android.content.pm.ApplicationInfo
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
            .map {
                LauncherApp(
                    packageName = it.activityInfo.packageName,
                    name = it.loadLabel(packageManager).toString(),
                    category = it.activityInfo.applicationInfo.readCategory()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name }
    }

    /** The category Android attaches to an app, absent before API 26. */
    private fun ApplicationInfo.readCategory(): AppCategory {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return AppCategory.UNKNOWN
        return when (category) {
            ApplicationInfo.CATEGORY_GAME -> AppCategory.GAME
            ApplicationInfo.CATEGORY_AUDIO -> AppCategory.AUDIO
            ApplicationInfo.CATEGORY_VIDEO -> AppCategory.VIDEO
            ApplicationInfo.CATEGORY_IMAGE -> AppCategory.IMAGE
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_NEWS -> AppCategory.NEWS
            ApplicationInfo.CATEGORY_MAPS -> AppCategory.MAPS
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            ApplicationInfo.CATEGORY_ACCESSIBILITY -> AppCategory.ACCESSIBILITY
            else -> AppCategory.UNKNOWN
        }
    }
}
