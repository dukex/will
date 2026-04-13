package tech.benedicta.will.notification

object NotificationActionStore {
    private val actions = mutableMapOf<Long, NotificationAction>()

    fun put(taskId: Long, action: NotificationAction) {
        actions[taskId] = action
    }

    fun get(taskId: Long): NotificationAction? = actions[taskId]

    fun remove(taskId: Long) {
        actions.remove(taskId)
    }

    fun clear() {
        actions.clear()
    }
}
