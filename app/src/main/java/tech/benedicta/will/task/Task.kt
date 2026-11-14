package tech.benedicta.will.task

data class Task(
    val id: Long = 0L,
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
    val origin: TaskOrigin = TaskOrigin.NOTIFICATION,
    /**
     * The notification's own title, when [title] was derived from it. Null
     * when the title is exactly what arrived, which is the usual case.
     */
    val rawTitle: String? = null
)
