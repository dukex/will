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
        val pendingIntent = sbn.notification.contentIntent

        // Conversation update: an existing task already covers this conversation.
        // Update the stored PendingIntent and key mapping — don't create a duplicate task.
        val existingConvTaskId = if (conversationKey != null) keyTracker.resolveConversation(conversationKey) else null
        if (existingConvTaskId != null) {
            keyTracker.track(sbn.key, existingConvTaskId)
            if (pendingIntent != null) NotificationActionStore.put(existingConvTaskId, pendingIntent)
            return
        }

        if (keyTracker.resolve(sbn.key) != null) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
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
            if (pendingIntent != null) NotificationActionStore.put(taskId, pendingIntent)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Only clean up the stale key→task mapping for dedup purposes.
        // Task lifecycle is user-controlled — don't delete tasks here.
        // Conversation tracking is preserved so future notifications don't create duplicates
        // until the user explicitly handles the task (calls clearConversationForTask).
        // PendingIntent is preserved so the tap still works after the notification is dismissed.
        val taskId = keyTracker.resolve(sbn.key) ?: return
        keyTracker.forget(sbn.key)
        android.util.Log.d("Will", "notification removed, task $taskId preserved")
    }

    /** Call when the user handles a task (done, delete) so the next notification
     *  for that conversation creates a fresh task instead of being deduplicated away. */
    fun clearConversationForTask(taskId: Long) {
        keyTracker.forgetConversationForTask(taskId)
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

    fun getPendingIntentForTask(taskId: Long): PendingIntent? {
        val key = keyTracker.notificationKeyForTask(taskId) ?: return null
        return activeNotifications?.firstOrNull { it.key == key }?.notification?.contentIntent
    }

    fun cancelNotificationForTask(taskId: Long) {
        val key = keyTracker.notificationKeyForTask(taskId) ?: return
        cancelNotification(key)
    }

    companion object {
        @Volatile var instance: WillNotificationListenerService? = null
    }
}
