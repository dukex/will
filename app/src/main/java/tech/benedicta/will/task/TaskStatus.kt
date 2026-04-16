package tech.benedicta.will.task

enum class TaskStatus { PENDING, IN_PROGRESS, DONE }

val Task.status: TaskStatus
    get() = when {
        completedAt != null -> TaskStatus.DONE
        isActive || startedAt != null -> TaskStatus.IN_PROGRESS
        else -> TaskStatus.PENDING
    }
