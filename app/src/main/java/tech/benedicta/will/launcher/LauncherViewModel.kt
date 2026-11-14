package tech.benedicta.will.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val repository: AppRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val search: SearchWithMeaning = SearchWithMeaning(AppMatcher(), SemanticAppSearch.Unavailable)
) : ViewModel() {

    private val _apps = MutableStateFlow<List<LauncherApp>>(emptyList())
    val apps: StateFlow<List<LauncherApp>> = _apps.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val filteredApps: StateFlow<List<LauncherApp>> = _apps.combine(_query) { list, query ->
        search.filter(list, query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setQuery(query: String) { _query.value = query }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(ioDispatcher) {
            _apps.value = repository.getInstalledApps()
        }
    }

    companion object {
        fun factory(
            repository: AppRepository,
            search: SearchWithMeaning = SearchWithMeaning(AppMatcher(), SemanticAppSearch.Unavailable)
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LauncherViewModel(repository, search = search) as T
            }
    }
}
