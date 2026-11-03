package tech.benedicta.will.infrastructure.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.benedicta.will.task.TaskEvent
import tech.benedicta.will.task.TaskEventType

@Entity(
    tableName = "task_events",
    indices = [Index(value = ["taskId"]), Index(value = ["at"])]
)
data class TaskEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long,
    val type: String,
    val at: Long,
    val foregroundPackage: String? = null
) {
    /**
     * Null when [type] is not a type this build knows. The caller drops the
     * row rather than guessing: see [TaskEventType.fromStored].
     */
    fun toTaskEvent(): TaskEvent? {
        val parsedType = TaskEventType.fromStored(type) ?: return null
        return TaskEvent(
            id = id,
            taskId = taskId,
            type = parsedType,
            at = at,
            foregroundPackage = foregroundPackage
        )
    }
}

fun TaskEvent.toEntity() = TaskEventEntity(
    id = id,
    taskId = taskId,
    type = type.name,
    at = at,
    foregroundPackage = foregroundPackage
)
