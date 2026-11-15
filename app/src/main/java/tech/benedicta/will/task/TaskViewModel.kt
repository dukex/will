package tech.benedicta.will.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.benedicta.will.notification.LearnedNoiseRepository

class TaskViewModel(
    private val repository: TaskRepository,
    private val events: TaskEventRecorder,
    private val eventLog: TaskEventRepository,
    private val learnedNoise: LearnedNoiseRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val inference: CompletionInference = CompletionInference(),
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeTask: StateFlow<Task?> = repository.getAllTasks()
        .map { list -> list.firstOrNull { it.isActive } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Tasks the log suggests are already finished. A hint for the feed, never
     * an action: completing is still the user's tap.
     *
     * A task becomes suggestable purely by time passing, with no event to wake
     * a flow, so this re-reads on a slow tick. [SharingStarted.WhileSubscribed]
     * stops the tick when nothing is looking at the feed.
     */
    val completionSuggestions: StateFlow<Set<Long>> = combine(
        repository.getAllTasks(),
        eventLog.eventsSince(inference.historyWindow(now())),
        ticker()
    ) { tasks, events, instant ->
        inference.suggestedTaskIds(tasks, events, instant)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /**
     * Throwing away a notification you never finished says it was not worth a
     * row. Only notifications teach this: a task you typed or an app sent in
     * is not something Will should learn to hide.
     */
    private suspend fun Task.teachDismissed() {
        if (origin != TaskOrigin.NOTIFICATION || completedAt != null) return
        learnedNoise.recordDismissed(sourcePackage, rawTitle ?: title)
    }

    /** Acting on one proves it mattered, whatever was learned before. */
    private suspend fun Task.teachKept() {
        if (origin != TaskOrigin.NOTIFICATION) return
        learnedNoise.recordKept(sourcePackage, rawTitle ?: title)
    }

    private fun ticker() = flow {
        while (true) {
            emit(now())
            delay(SUGGESTION_REFRESH_MILLIS)
        }
    }

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
                    task.teachKept()
                }
                ACTION_DELETE -> {
                    repository.deleteTask(task)
                    events.forgot(task.id)
                    task.teachDismissed()
                }
                // "later" leaves the task open and only drops it from active,
                // which clearActiveTask below turns into a pause.
                else -> if (task.isActive) events.paused(task.id)
            }
            repository.clearActiveTask()
        }
    }

    companion object {
        private const val SUGGESTION_REFRESH_MILLIS = 60_000L

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
            task.teachDismissed()
        }
    }

    fun markDone(task: Task) {
        viewModelScope.launch(dispatcher) {
            repository.clearConversationKey(task.id)
            repository.updateTask(
                task.copy(completedAt = System.currentTimeMillis(), conversationKey = null)
            )
            events.completed(task.id)
            task.teachKept()
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
            learnedNoise.forgetEverything()
        }
    }

    class Factory(
        private val repository: TaskRepository,
        private val events: TaskEventRecorder,
        private val eventLog: TaskEventRepository,
        private val learnedNoise: LearnedNoiseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TaskViewModel(repository, events, eventLog, learnedNoise) as T
    }
}
