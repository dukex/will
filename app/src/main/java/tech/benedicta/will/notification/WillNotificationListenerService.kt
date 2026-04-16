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
    private val keyTracker = NotificationKeyTracker()

    private val repository by lazy {
        (application as WillApplication).taskRepository
    }
    private val appFilterRepository by lazy {
        (application as WillApplication).appFilterRepository
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (processor.shouldIgnoreFlags(sbn.notification.flags)) return

        val conversationKey = conversationKeyFor(sbn)
        if (conversationKey != null && keyTracker.resolveConversation(conversationKey) != null) return

        if (keyTracker.resolve(sbn.key) != null) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val pendingIntent = sbn.notification.contentIntent
        val task = processor.process(title, text, sbn.packageName, sbn.postTime) ?: return

        val appName = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        }.getOrDefault(sbn.packageName)

        scope.launch {
            appFilterRepository.addApp(sbn.packageName, appName)
            if (!appFilterRepository.isEnabled(sbn.packageName)) return@launch
            val taskId = repository.insertTask(task)
            keyTracker.track(sbn.key, taskId)
            if (conversationKey != null) keyTracker.trackConversation(conversationKey, taskId)
            if (pendingIntent != null) {
                NotificationActionStore.put(taskId, pendingIntent)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val taskId = keyTracker.resolve(sbn.key) ?: return
        keyTracker.forget(sbn.key)
        conversationKeyFor(sbn)?.let { keyTracker.forgetConversation(it) }
        NotificationActionStore.remove(taskId)
        scope.launch { repository.deleteTaskById(taskId) }
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

    fun getPendingIntentForTask(taskId: Long): PendingIntent? {
        val key = keyTracker.notificationKeyForTask(taskId)
        val activeKeys = activeNotifications?.map { it.key }
        android.util.Log.d("WillTap", "getPendingIntent taskId=$taskId key=$key activeKeys=$activeKeys")
        if (key == null) return null
        return activeNotifications?.firstOrNull { it.key == key }?.notification?.contentIntent
    }

    fun cancelNotificationForTask(taskId: Long) {
        val key = keyTracker.notificationKeyForTask(taskId) ?: return
        android.util.Log.d("WillTap", "cancelling notification key=$key")
        cancelNotification(key)
    }

    companion object {
        @Volatile var instance: WillNotificationListenerService? = null
    }
}
