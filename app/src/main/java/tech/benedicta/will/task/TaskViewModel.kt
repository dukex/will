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
    private val events: TaskEventRecorder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeTask: StateFlow<Task?> = repository.getAllTasks()
        .map { list -> list.firstOrNull { it.isActive } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setActiveTask(task: Task) {
        viewModelScope.launch(dispatcher) {
            val previous = activeTask.value
            repository.setActiveTask(task.id)
            if (previous?.id == task.id) return@launch
            previous?.let { events.paused(it.id) }
            events.activated(task)
        }
    }

    fun handleOverlayAction(action: String, taskId: Long) {
        val task = tasks.value.find { it.id == taskId } ?: return
        viewModelScope.launch(dispatcher) {
            when (action) {
                ACTION_DONE -> {
                    repository.updateTask(
                        task.copy(completedAt = System.currentTimeMillis(), conversationKey = null)
                    )
                    events.completed(task.id)
                }
                ACTION_DELETE -> {
                    repository.deleteTask(task)
                    events.forgot(task.id)
                }
                // "later" leaves the task open and only drops it from active,
                // which clearActiveTask below turns into a pause.
                else -> if (task.isActive) events.paused(task.id)
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
        viewModelScope.launch(dispatcher) {
            val id = repository.insertTask(task)
            events.created(id)
        }
    }

    fun addTaskAndStart(task: Task) {
        viewModelScope.launch(dispatcher) {
            val id = repository.insertTask(task)
            events.created(id)
            repository.setActiveTask(id)
            events.started(id, System.currentTimeMillis())
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.clearConversationKey(task.id)
            repository.deleteTask(task)
            events.forgot(task.id)
        }
    }

    fun markDone(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.clearConversationKey(task.id)
            repository.updateTask(
                task.copy(completedAt = System.currentTimeMillis(), conversationKey = null)
            )
            events.completed(task.id)
        }
    }

    fun reopenTask(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.updateTask(task.copy(completedAt = null, pausedAt = task.startedAt))
            events.reopened(task.id)
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch(dispatcher) {
            repository.deleteAllTasks()
            events.forgotEverything()
        }
    }

    class Factory(
        private val repository: TaskRepository,
        private val events: TaskEventRecorder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TaskViewModel(repository, events) as T
    }
}
