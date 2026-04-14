package tech.benedicta.will.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.benedicta.will.task.RoomTaskRepository
import tech.benedicta.will.task.WillDatabase

class WillNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val processor = NotificationProcessor()
    private val keyTracker = NotificationKeyTracker()

    private val repository by lazy {
        RoomTaskRepository(WillDatabase.getInstance(applicationContext).taskDao())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val pendingIntent = sbn.notification.contentIntent
        val task = processor.process(title, text, sbn.packageName, sbn.postTime) ?: return

        scope.launch {
            val taskId = repository.insertTask(task)
            keyTracker.track(sbn.key, taskId)
            if (pendingIntent != null) {
                NotificationActionStore.put(taskId, NotificationAction { pendingIntent.send() })
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val taskId = keyTracker.resolve(sbn.key) ?: return
        keyTracker.forget(sbn.key)
        NotificationActionStore.remove(taskId)
        scope.launch { repository.deleteTaskById(taskId) }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
