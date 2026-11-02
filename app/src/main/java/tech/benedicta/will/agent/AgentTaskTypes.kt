package tech.benedicta.will.agent

import androidx.appfunctions.AppFunctionSerializable

/** An intention to put on the Will home screen. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CreateTaskParams(
    /** What to do, in the words the user should read on their home screen. */
    val title: String,
    /** Optional detail, shown under the title. */
    val text: String?,
    /**
     * Package name of the app this task is about, so tapping it opens that
     * app. Null when no app applies.
     */
    val sourcePackage: String?
)

/** Identifies one task already on the home screen. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class TaskIdParams(
    /** The task's id, as returned by listOpenTasks or createTask. */
    val id: Long
)

/** A task as it stands on the Will home screen. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AgentTask(
    /** Stable id, used to complete or start this task later. */
    val id: Long,
    /** What the user reads on the home screen. */
    val title: String,
    /** Detail under the title; empty when there is none. */
    val text: String,
    /** One of PENDING, IN_PROGRESS, PAUSED, DONE. */
    val status: String,
    /** Package of the app this task is about; "manual" when none. */
    val sourcePackage: String,
    /** Who put it there: NOTIFICATION, MANUAL, SHARED or AGENT. */
    val origin: String,
    /** When it reached the feed, in milliseconds since the epoch. */
    val createdAt: Long,
    /** When the user first worked on it, or null if they have not. */
    val startedAt: Long?,
    /** How many times the user left this task for an unrelated app. */
    val contextSwitchCount: Int
)

/** The tasks currently on the home screen. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AgentTaskList(
    /** Open tasks, most recent first. */
    val tasks: List<AgentTask>
)
