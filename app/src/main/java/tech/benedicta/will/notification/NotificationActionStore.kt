package tech.benedicta.will.notification

import android.app.PendingIntent

object NotificationActionStore {
    private val intents = mutableMapOf<Long, PendingIntent>()

    fun put(taskId: Long, intent: PendingIntent) {
        intents[taskId] = intent
    }

    fun get(taskId: Long): PendingIntent? = intents[taskId]

    fun remove(taskId: Long) {
        intents.remove(taskId)
    }
}
