package tech.benedicta.will.infrastructure

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.benedicta.will.notification.AppFilterRepository
import tech.benedicta.will.notification.AppNotificationFilter

class SharedPrefsAppFilterRepository(private val context: Context) : AppFilterRepository {
    private val prefs = context.getSharedPreferences("app_filters", Context.MODE_PRIVATE)

    override fun getAllFilters(): Flow<List<AppNotificationFilter>> = flow {
        val filters = prefs.all.mapNotNull { (key, value) ->
            val parts = (value as? String)?.split("|") ?: return@mapNotNull null
            AppNotificationFilter(
                packageName = key,
                appName = parts.getOrElse(0) { key },
                isEnabled = parts.getOrElse(1) { "true" } == "true"
            )
        }.sortedWith(
            compareByDescending<AppNotificationFilter> { it.isEnabled }.thenBy { it.appName }
        )
        emit(filters)
    }

    override suspend fun setEnabled(packageName: String, enabled: Boolean) {
        val current = prefs.getString(packageName, null)
        val appName = current?.split("|")?.getOrElse(0) { packageName } ?: packageName
        prefs.edit().putString(packageName, "$appName|$enabled").apply()
    }

    override suspend fun addApp(packageName: String, appName: String) {
        if (!prefs.contains(packageName)) {
            prefs.edit().putString(packageName, "$appName|true").apply()
        }
    }

    override fun isEnabled(packageName: String): Boolean =
        prefs.getString(packageName, null)?.split("|")?.getOrElse(1) { "true" } != "false"
}
