package tech.benedicta.will.notification

class NotificationKeyTracker {
    private val keyToTaskId = mutableMapOf<String, Long>()
    private val conversationToTaskId = mutableMapOf<String, Long>()

    fun track(notificationKey: String, taskId: Long) {
        keyToTaskId[notificationKey] = taskId
    }

    fun resolve(notificationKey: String): Long? = keyToTaskId[notificationKey]

    fun forget(notificationKey: String) {
        keyToTaskId.remove(notificationKey)
    }

    fun trackConversation(conversationKey: String, taskId: Long) {
        conversationToTaskId[conversationKey] = taskId
    }

    fun resolveConversation(conversationKey: String): Long? = conversationToTaskId[conversationKey]

    fun forgetConversation(conversationKey: String) {
        conversationToTaskId.remove(conversationKey)
    }
}
