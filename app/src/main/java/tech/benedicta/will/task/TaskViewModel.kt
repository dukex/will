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

    fun handleOverlayAction(action: String, taskId: Long) {
        val task = tasks.value.find { it.id == taskId } ?: return
        viewModelScope.launch(dispatcher) {
            repository.clearConversationKey(taskId)
            when (action) {
                ACTION_DONE -> repository.updateTask(
                    task.copy(completedAt = System.currentTimeMillis(), isActive = false, conversationKey = null)
                )
                ACTION_DELETE -> repository.deleteTask(task)
            }
            repository.clearActiveTask()
        }
    }

    companion object {
        const val ACTION_DONE = "tech.benedicta.will.TASK_DONE"
        const val ACTION_DELETE = "tech.benedicta.will.TASK_DELETE"
        const val ACTION_LATER = "tech.benedicta.will.TASK_LATER"
    }

    fun addTask(task: Task) {
        viewModelScope.launch(dispatcher) { repository.insertTask(task) }
    }

    fun addTaskAndStart(task: Task) {
        viewModelScope.launch(dispatcher) {
            val now = System.currentTimeMillis()
            val id = repository.insertTask(
                task.copy(isActive = true, startedAt = task.startedAt ?: now)
            )
            repository.setActiveTask(id)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.clearConversationKey(task.id)
            repository.deleteTask(task)
        }
    }

    fun markDone(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.clearConversationKey(task.id)
            repository.updateTask(
                task.copy(completedAt = System.currentTimeMillis(), isActive = false, conversationKey = null)
            )
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
