package tech.benedicta.will.agent

import kotlinx.coroutines.flow.first
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskEventRecorder
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.task.TaskRepository
import tech.benedicta.will.task.TaskStatus
import tech.benedicta.will.task.status

/** The title an agent sent had no content. */
class BlankTaskTitle : IllegalArgumentException("title must not be blank")

/** No task on the home screen has the id an agent asked for. */
class UnknownTask(val taskId: Long) : NoSuchElementException("no task with id $taskId")

/**
 * What an on-device agent can do with the Will home screen, as plain Kotlin.
 *
 * [WillAppFunctionService] is the annotated surface the platform calls; this
 * holds the behaviour, free of AppFunctions types, so it can be tested without
 * Android and so the rule that keeps framework types out of the task layer
 * still holds. The service translates these failures into the errors an agent
 * understands.
 *
 * Everything runs on the device: Will makes no network calls and stores
 * nothing outside the local database.
 */
class WillTaskFunctions(
    private val tasks: TaskRepository,
    private val events: TaskEventRecorder
) {

    suspend fun createTask(createTaskParams: CreateTaskParams): AgentTask {
        val title = createTaskParams.title.trim()
        if (title.isEmpty()) throw BlankTaskTitle()

        val task = Task(
            title = title,
            text = createTaskParams.text?.trim().orEmpty(),
            sourcePackage = createTaskParams.sourcePackage?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: TaskOrigin.MANUAL_SOURCE_PACKAGE,
            createdAt = System.currentTimeMillis(),
            origin = TaskOrigin.AGENT
        )
        val id = tasks.insertTask(task)
        events.created(id)
        return task.copy(id = id).toAgentTask()
    }

    suspend fun listOpenTasks(): AgentTaskList =
        AgentTaskList(
            tasks = tasks.getAllTasks().first()
                .filter { it.status != TaskStatus.DONE }
                .sortedByDescending { it.createdAt }
                .map { it.toAgentTask() }
        )

    suspend fun completeTask(taskIdParams: TaskIdParams): AgentTask {
        val task = findTask(taskIdParams.id)
        val completed = task.copy(
            completedAt = System.currentTimeMillis(),
            conversationKey = null
        )
        tasks.clearConversationKey(task.id)
        tasks.updateTask(completed)
        events.completed(task.id)
        return completed.toAgentTask()
    }

    suspend fun getTask(taskIdParams: TaskIdParams): AgentTask =
        findTask(taskIdParams.id).toAgentTask()

    private suspend fun findTask(id: Long): Task =
        tasks.getAllTasks().first().firstOrNull { it.id == id }
            ?: throw UnknownTask(id)
}

internal fun Task.toAgentTask() = AgentTask(
    id = id,
    title = title,
    text = text,
    status = status.name,
    sourcePackage = sourcePackage,
    origin = origin.name,
    createdAt = createdAt,
    startedAt = startedAt,
    contextSwitchCount = contextSwitchCount
)
