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

    private val repository by lazy {
        RoomTaskRepository(WillDatabase.getInstance(applicationContext).taskDao())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val sourcePackage = sbn.packageName
        val timestamp = sbn.postTime

        val pendingIntent = sbn.notification.contentIntent
        val task = processor.process(title, text, sourcePackage, timestamp) ?: return

        scope.launch {
            val taskId = repository.insertTask(task)
            if (pendingIntent != null) {
                NotificationActionStore.put(taskId, NotificationAction { pendingIntent.send() })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
