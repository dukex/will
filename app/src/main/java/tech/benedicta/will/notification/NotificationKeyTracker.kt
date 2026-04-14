package tech.benedicta.will.notification

class NotificationKeyTracker {
    private val keyToTaskId = mutableMapOf<String, Long>()

    fun track(notificationKey: String, taskId: Long) {
        keyToTaskId[notificationKey] = taskId
    }

    fun resolve(notificationKey: String): Long? = keyToTaskId[notificationKey]

    fun forget(notificationKey: String) {
        keyToTaskId.remove(notificationKey)
    }
}
