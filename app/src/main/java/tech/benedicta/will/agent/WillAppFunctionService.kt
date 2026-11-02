package tech.benedicta.will.agent

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import tech.benedicta.will.WillApplication

/**
 * The surface an on-device agent or assistant calls to work with the Will home
 * screen. Google's framing for this is an on-device MCP server: Will publishes
 * typed, documented functions, the platform indexes them, and a caller holding
 * the system's EXECUTE_APP_FUNCTIONS permission can execute them.
 *
 * Will only answers. It never calls out, and nothing here reaches the network.
 *
 * The KDoc on each function is what the agent reads to decide whether to call
 * it, so it is written for that reader rather than for a maintainer. The
 * behaviour lives in [WillTaskFunctions], which is where the tests are.
 *
 * Needs Android 16, where the platform indexes app functions. On anything
 * older the service is simply never bound.
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "GeneratedWillAppFunctionService",
    appFunctionXmlFileName = "will_app_functions"
)
abstract class WillAppFunctionService : AppFunctionService() {

    private val functions: WillTaskFunctions by lazy {
        val app = applicationContext as WillApplication
        WillTaskFunctions(tasks = app.taskRepository, events = app.taskEventRecorder)
    }

    /**
     * Puts an intention on the user's home screen as a task.
     *
     * Use this to hand work back to the user: something you cannot finish, a
     * decision only they can make, or a reminder they asked for. The task is
     * marked as written by an agent, so the user can see it was not theirs.
     *
     * @param createTaskParams what to put on the home screen.
     * @return the task as it now stands, including the id needed to change it.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun createTask(
        createTaskParams: CreateTaskParams
    ): AgentTask = reporting { functions.createTask(createTaskParams) }

    /**
     * Lists the tasks the user still has open, most recent first.
     *
     * Use this to find out what the user is already holding before adding to
     * it, or to pick the task a follow-up belongs to. Finished tasks are left
     * out.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listOpenTasks(): AgentTaskList = reporting { functions.listOpenTasks() }

    /**
     * Reads one task by id.
     *
     * Use this to check the state of a task you created earlier, including
     * whether the user has started it and how often they were pulled away from
     * it.
     *
     * @param taskIdParams the task to read.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getTask(
        taskIdParams: TaskIdParams
    ): AgentTask = reporting { functions.getTask(taskIdParams) }

    /**
     * Marks a task as done, which moves it off the home screen.
     *
     * Only call this for work that is genuinely finished. Completing a task
     * the user has not finished takes it out of their sight.
     *
     * @param taskIdParams the task to complete.
     * @return the task as it now stands.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun completeTask(
        taskIdParams: TaskIdParams
    ): AgentTask = reporting { functions.completeTask(taskIdParams) }

    /**
     * Turns the core's failures into the errors an agent can act on. The
     * AppFunctions exception types cannot be constructed off-device, which is
     * why the core throws plain Kotlin ones and the translation lives here.
     */
    private inline fun <T> reporting(block: () -> T): T =
        try {
            block()
        } catch (blank: BlankTaskTitle) {
            throw AppFunctionInvalidArgumentException(blank.message.orEmpty())
        } catch (unknown: UnknownTask) {
            throw AppFunctionElementNotFoundException(unknown.message.orEmpty())
        }
}
