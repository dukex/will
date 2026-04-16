package tech.benedicta.will.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeTask: StateFlow<Task?> = repository.getAllTasks()
        .map { list -> list.firstOrNull { it.isActive } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setActiveTask(task: Task) {
        viewModelScope.launch(dispatcher) { repository.setActiveTask(task.id) }
    }

    fun clearActiveTask() {
        viewModelScope.launch(dispatcher) { repository.clearActiveTask() }
    }

    fun addTask(task: Task) {
        viewModelScope.launch(dispatcher) { repository.insertTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(dispatcher) { repository.deleteTask(task) }
    }

    fun markDone(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.updateTask(
                task.copy(completedAt = System.currentTimeMillis(), isActive = false)
            )
        }
    }

    fun startTask(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.updateTask(
                task.copy(isActive = true, startedAt = task.startedAt ?: System.currentTimeMillis())
            )
        }
    }

    fun pauseTask(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.updateTask(task.copy(isActive = false))
        }
    }

    fun reopenTask(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.updateTask(task.copy(completedAt = null, isActive = false))
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch(dispatcher) { repository.deleteAllTasks() }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TaskViewModel(repository) as T
    }
}
