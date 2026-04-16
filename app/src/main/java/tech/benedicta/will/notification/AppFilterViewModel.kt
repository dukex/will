package tech.benedicta.will.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppFilterViewModel(
    private val repository: AppFilterRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val filters: StateFlow<List<AppNotificationFilter>> = repository.getAllFilters()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggle(packageName: String, enabled: Boolean) {
        viewModelScope.launch(dispatcher) { repository.setEnabled(packageName, enabled) }
    }

    class Factory(private val repository: AppFilterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AppFilterViewModel(repository) as T
    }
}
