package tech.benedicta.will.infrastructure

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.launcher.PinnedAppsRepository
import tech.benedicta.will.launcher.PinnedAppsRepository.Companion.MAX_PINNED

class SharedPrefsPinnedAppsRepository(context: Context) : PinnedAppsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _pinned = MutableStateFlow(readAll())

    override fun getPinnedPackages(): Flow<List<String>> = _pinned.asStateFlow()

    override suspend fun pin(packageName: String): Boolean {
        val current = _pinned.value
        if (packageName in current) return true
        if (current.size >= MAX_PINNED) return false
        val updated = current + packageName
        persist(updated)
        _pinned.value = updated
        return true
    }

    override suspend fun unpin(packageName: String) {
        val updated = _pinned.value.filterNot { it == packageName }
        persist(updated)
        _pinned.value = updated
    }

    override suspend fun isPinned(packageName: String): Boolean =
        packageName in _pinned.value

    private fun readAll(): List<String> =
        prefs.getString(KEY_PINNED, null)
            ?.split(SEPARATOR)
            ?.filter { it.isNotEmpty() }
            ?.take(MAX_PINNED)
            ?: emptyList()

    private fun persist(list: List<String>) {
        prefs.edit().putString(KEY_PINNED, list.joinToString(SEPARATOR)).apply()
    }

    private companion object {
        const val PREFS_NAME = "pinned_apps"
        const val KEY_PINNED = "pinned_packages"
        const val SEPARATOR = "|"
    }
}