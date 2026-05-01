package tech.benedicta.will.notification

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
        if (sbn.packageName in NotificationProcessor.IGNORED_PACKAGES) return

        val appName = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        }.getOrDefault(sbn.packageName)

        scope.launch { appFilterRepository.addApp(sbn.packageName, appName) }

        if (processor.shouldIgnoreFlags(sbn.notification.flags)) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val conversationTitle = extras.getCharSequence("android.conversationTitle")?.toString()
        val shortcutId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sbn.notification.shortcutId
        } else null
        val task = processor.process(
            title, text, sbn.packageName, sbn.postTime,
            notificationKey = sbn.key,
            conversationTitle = conversationTitle,
            shortcutId = shortcutId
        ) ?: return

        scope.launch {
            if (!appFilterRepository.isEnabled(sbn.packageName)) return@launch
            repository.upsertByConversationKey(task)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    override fun onListenerConnected() { instance = this }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        job.cancel()
    }

    companion object {
        @Volatile var instance: WillNotificationListenerService? = null
    }
}
