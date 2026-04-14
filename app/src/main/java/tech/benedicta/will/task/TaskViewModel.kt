package tech.benedicta.will.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _activeTask = MutableStateFlow<Task?>(null)
    val activeTask: StateFlow<Task?> = _activeTask.asStateFlow()

    fun setActiveTask(task: Task) { _activeTask.value = task }
    fun clearActiveTask() { _activeTask.value = null }

    val tasks: StateFlow<List<Task>> = repository.getAllTasks()
        .map { list -> list.filter { it.completedAt == null } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addTask(task: Task) {
        viewModelScope.launch(dispatcher) { repository.insertTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(dispatcher) { repository.deleteTask(task) }
    }

    fun markDone(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.updateTask(task.copy(completedAt = System.currentTimeMillis()))
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TaskViewModel(repository) as T
    }
}
