package tech.benedicta.will.notification

import android.app.PendingIntent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.benedicta.will.WillApplication

class WillNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val processor = NotificationProcessor()

    private val repository by lazy {
        (application as WillApplication).taskRepository
    }
    private val appFilterRepository by lazy {
        (application as WillApplication).appFilterRepository
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (processor.shouldIgnoreFlags(sbn.notification.flags)) return

        val conversationKey = conversationKeyFor(sbn)
        val pendingIntent = sbn.notification.contentIntent

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val task = processor.process(
            title, text, sbn.packageName, sbn.postTime,
            notificationKey = sbn.key,
            conversationKey = conversationKey
        ) ?: return

        val appName = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        }.getOrDefault(sbn.packageName)

        scope.launch {
            appFilterRepository.addApp(sbn.packageName, appName)
            if (!appFilterRepository.isEnabled(sbn.packageName)) return@launch
            val taskId = repository.insertTask(task)
            if (taskId == -1L) return@launch
            if (pendingIntent != null) NotificationActionStore.put(taskId, pendingIntent)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Task lifecycle is user-controlled — nothing to clean up.
    }

    fun clearConversationForTask(taskId: Long) {
        NotificationActionStore.remove(taskId)
    }

    private fun conversationKeyFor(sbn: StatusBarNotification): String? {
        val shortcutId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sbn.notification.shortcutId
        } else null
        return if (shortcutId != null) "${sbn.packageName}:$shortcutId" else null
    }

    override fun onListenerConnected() {
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        job.cancel()
    }

    fun getContentIntentByNotificationKey(notificationKey: String): PendingIntent? =
        activeNotifications?.firstOrNull { it.key == notificationKey }?.notification?.contentIntent

    fun cancelNotificationByKey(notificationKey: String?) {
        if (notificationKey != null) runCatching { cancelNotification(notificationKey) }
    }

    companion object {
        @Volatile var instance: WillNotificationListenerService? = null
    }
}
