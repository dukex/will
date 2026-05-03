package tech.benedicta.will.infrastructure

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import tech.benedicta.will.onboarding.PermissionInspector

class AndroidPermissionInspector(private val context: Context) : PermissionInspector {

    override fun isDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                ?: return resolveLauncherByPackageManager()
            return runCatching { roleManager.isRoleHeld(RoleManager.ROLE_HOME) }
                .getOrDefault(resolveLauncherByPackageManager())
        }
        return resolveLauncherByPackageManager()
    }

    override fun hasOverlayPermission(): Boolean = Settings.canDrawOverlays(context)

    override fun hasNotificationListener(): Boolean =
        NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)

    override fun hasAccessibilityService(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        // Format: "package/full.qualified.ServiceName:another/...". We match
        // the prefix against our package + service class so a partial pkg
        // collision does not produce a false positive.
        val target = "${context.packageName}/" +
            "tech.benedicta.will.lifecycle.WillAccessibilityService"
        return enabled.split(':').any { it == target }
    }

    private fun resolveLauncherByPackageManager(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(intent, 0) ?: return false
        return resolved.activityInfo?.packageName == context.packageName
    }
}
