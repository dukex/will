package tech.benedicta.will.notification

class NotificationKeyTracker {
    private val keyToTaskId = mutableMapOf<String, Long>()
    private val taskIdToKey = mutableMapOf<Long, String>()
    private val conversationToTaskId = mutableMapOf<String, Long>()

    fun track(notificationKey: String, taskId: Long) {
        keyToTaskId[notificationKey] = taskId
        taskIdToKey[taskId] = notificationKey
    }

    fun resolve(notificationKey: String): Long? = keyToTaskId[notificationKey]

    fun notificationKeyForTask(taskId: Long): String? = taskIdToKey[taskId]

    fun forget(notificationKey: String) {
        val taskId = keyToTaskId.remove(notificationKey)
        if (taskId != null) taskIdToKey.remove(taskId)
    }

    fun trackConversation(conversationKey: String, taskId: Long) {
        conversationToTaskId[conversationKey] = taskId
    }

    fun resolveConversation(conversationKey: String): Long? = conversationToTaskId[conversationKey]

    fun forgetConversation(conversationKey: String) {
        conversationToTaskId.remove(conversationKey)
    }

    /** Clears conversation dedup for a given task — call when the user handles the task
     *  so the next notification for that conversation creates a fresh task. */
    fun forgetConversationForTask(taskId: Long) {
        val entry = conversationToTaskId.entries.firstOrNull { it.value == taskId } ?: return
        conversationToTaskId.remove(entry.key)
    }
}
