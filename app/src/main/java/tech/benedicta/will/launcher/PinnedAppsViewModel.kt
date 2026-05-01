package tech.benedicta.will.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.benedicta.will.launcher.PinnedAppsRepository.Companion.MAX_PINNED

class PinnedAppsViewModel(
    private val repository: PinnedAppsRepository,
    installedApps: StateFlow<List<LauncherApp>>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val pinnedApps: StateFlow<List<LauncherApp>> =
        repository.getPinnedPackages()
            .combine(installedApps) { packages, all ->
                packages.mapNotNull { pkg -> all.find { it.packageName == pkg } }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val canPinMore: StateFlow<Boolean> =
        repository.getPinnedPackages()
            .map { it.size < MAX_PINNED }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    suspend fun pin(packageName: String): Boolean = repository.pin(packageName)

    fun unpin(packageName: String) {
        viewModelScope.launch(ioDispatcher) { repository.unpin(packageName) }
    }

    suspend fun isPinned(packageName: String): Boolean = repository.isPinned(packageName)

    companion object {
        fun factory(
            repository: PinnedAppsRepository,
            installedApps: StateFlow<List<LauncherApp>>
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PinnedAppsViewModel(repository, installedApps) as T
            }
    }
}