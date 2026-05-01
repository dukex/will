package tech.benedicta.will.task

data class Task(
    val id: Long = 0L,
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
)
