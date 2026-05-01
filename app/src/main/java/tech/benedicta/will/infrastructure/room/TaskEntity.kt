package tech.benedicta.will.infrastructure.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.benedicta.will.task.Task

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["conversationKey"], unique = true)]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val text: String,
    val sourcePackage: String,
    val createdAt: Long,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val contextSwitchCount: Int = 0,
    val isActive: Boolean = false,
    val notificationKey: String? = null,
    val conversationKey: String? = null,
    val unreadCount: Int = 1
) {
    fun toTask() = Task(
        id = id,
        title = title,
        text = text,
        sourcePackage = sourcePackage,
        createdAt = createdAt,
        startedAt = startedAt,
        completedAt = completedAt,
        contextSwitchCount = contextSwitchCount,
        isActive = isActive,
        notificationKey = notificationKey,
        conversationKey = conversationKey,
        unreadCount = unreadCount
    )
}

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    text = text,
    sourcePackage = sourcePackage,
    createdAt = createdAt,
    startedAt = startedAt,
    completedAt = completedAt,
    contextSwitchCount = contextSwitchCount,
    isActive = isActive,
    notificationKey = notificationKey,
    conversationKey = conversationKey,
    unreadCount = unreadCount
)
