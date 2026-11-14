package tech.benedicta.will.infrastructure.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin

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
    val pausedAt: Long? = null,
    val completedAt: Long? = null,
    val contextSwitchCount: Int = 0,
    val notificationKey: String? = null,
    val conversationKey: String? = null,
    val unreadCount: Int = 1,
    val shortcutId: String? = null,
    @ColumnInfo(defaultValue = DEFAULT_ORIGIN)
    val origin: String = DEFAULT_ORIGIN,
    val rawTitle: String? = null
) {
    fun toTask() = Task(
        id = id,
        title = title,
        text = text,
        sourcePackage = sourcePackage,
        createdAt = createdAt,
        startedAt = startedAt,
        pausedAt = pausedAt,
        completedAt = completedAt,
        contextSwitchCount = contextSwitchCount,
        notificationKey = notificationKey,
        conversationKey = conversationKey,
        unreadCount = unreadCount,
        shortcutId = shortcutId,
        origin = TaskOrigin.fromStored(origin),
        rawTitle = rawTitle
    )

    companion object {
        const val DEFAULT_ORIGIN = "NOTIFICATION"
    }
}

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    text = text,
    sourcePackage = sourcePackage,
    createdAt = createdAt,
    startedAt = startedAt,
    pausedAt = pausedAt,
    completedAt = completedAt,
    contextSwitchCount = contextSwitchCount,
    notificationKey = notificationKey,
    conversationKey = conversationKey,
    unreadCount = unreadCount,
    shortcutId = shortcutId,
    origin = origin.name,
    rawTitle = rawTitle
)
