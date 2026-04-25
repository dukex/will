package tech.benedicta.will.infrastructure.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.launcher.PinnedAppsRepository
import tech.benedicta.will.launcher.PinnedAppsRepository.Companion.MAX_PINNED

class MemoryPinnedAppsRepository : PinnedAppsRepository {
    private val _pinned = MutableStateFlow<List<String>>(emptyList())

    override fun getPinnedPackages(): Flow<List<String>> = _pinned.asStateFlow()

    override suspend fun pin(packageName: String): Boolean {
        val current = _pinned.value
        if (packageName in current) return true
        if (current.size >= MAX_PINNED) return false
        _pinned.value = current + packageName
        return true
    }

    override suspend fun unpin(packageName: String) {
        _pinned.value = _pinned.value.filterNot { it == packageName }
    }

    override suspend fun isPinned(packageName: String): Boolean =
        packageName in _pinned.value
}