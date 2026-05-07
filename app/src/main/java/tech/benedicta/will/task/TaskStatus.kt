package tech.benedicta.will.task

enum class TaskStatus { PENDING, IN_PROGRESS, PAUSED, DONE }

val Task.status: TaskStatus
    get() = when {
        completedAt != null -> TaskStatus.DONE
        startedAt != null && pausedAt == null -> TaskStatus.IN_PROGRESS
        startedAt != null -> TaskStatus.PAUSED
        else -> TaskStatus.PENDING
    }

val Task.isActive: Boolean
    get() = status == TaskStatus.IN_PROGRESS
