package tech.benedicta.will.notification

import kotlinx.coroutines.flow.Flow

interface AppFilterRepository {
    fun getAllFilters(): Flow<List<AppNotificationFilter>>
    suspend fun setEnabled(packageName: String, enabled: Boolean)
    suspend fun addApp(packageName: String, appName: String)
    fun isEnabled(packageName: String): Boolean
}
