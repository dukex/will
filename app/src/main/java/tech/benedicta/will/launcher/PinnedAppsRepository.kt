package tech.benedicta.will.launcher

import kotlinx.coroutines.flow.Flow

interface PinnedAppsRepository {
    fun getPinnedPackages(): Flow<List<String>>
    suspend fun pin(packageName: String): Boolean
    suspend fun unpin(packageName: String)
    suspend fun isPinned(packageName: String): Boolean

    companion object {
        const val MAX_PINNED = 5
    }
}