package tech.benedicta.will.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherViewModel(
    private val repository: AppRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _apps = MutableStateFlow<List<LauncherApp>>(emptyList())
    val apps: StateFlow<List<LauncherApp>> = _apps.asStateFlow()

    companion object {
        fun factory(repository: AppRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LauncherViewModel(repository) as T
            }
    }
}
