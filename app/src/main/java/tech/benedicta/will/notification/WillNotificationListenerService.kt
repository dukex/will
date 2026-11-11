package tech.benedicta.will.notification

import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.benedicta.will.R
import tech.benedicta.will.WillApplication

class WillNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val processor = NotificationProcessor()
    private val toTask by lazy {
        NotificationToTask(
            processor = processor,
            reader = (application as WillApplication).intentionReader,
            phrase = { kind, subject ->
                when (kind) {
                    IntentionKind.REPLY -> getString(R.string.derived_reply_to, subject)
                    IntentionKind.CALL_BACK -> getString(R.string.derived_call_back, subject)
                    IntentionKind.CONFIRM -> getString(R.string.derived_confirm, subject)
                    IntentionKind.PAY -> getString(R.string.derived_pay, subject)
                    IntentionKind.ATTEND -> getString(R.string.derived_attend, subject)
                }
            }
        )
    }

    private val repository by lazy {
        (application as WillApplication).taskRepository
    }
    private val appFilterRepository by lazy {
        (application as WillApplication).appFilterRepository
    }
    private val taskEvents by lazy {
        (application as WillApplication).taskEventRecorder
    }
    private val classifier by lazy {
        (application as WillApplication).intentionClassifier
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
        val category = sbn.notification.category
        val sourcePackage = sbn.packageName
        val timestamp = sbn.postTime
        val notificationKey = sbn.key

        // Everything above reads `sbn`, which is only valid on this callback
        // thread. From here on only the copies are used, because reading a
        // notification's words with the model has to be allowed to wait and
        // this thread cannot.
        scope.launch {
            if (!appFilterRepository.isEnabled(sourcePackage)) return@launch

            val task = toTask.convert(
                category = category,
                title = title,
                text = text,
                sourcePackage = sourcePackage,
                timestamp = timestamp,
                notificationKey = notificationKey,
                conversationTitle = conversationTitle,
                shortcutId = shortcutId,
                // Triage judges what the notification actually said, not the
                // title Will derived from it: the noise patterns are written
                // against vendor wording, and at this point nothing has been
                // derived yet.
                keep = { plain ->
                    classifier.isIntention(
                        plain.rawTitle ?: plain.title,
                        plain.text,
                        plain.sourcePackage
                    )
                }
            ) ?: return@launch
            val upsert = repository.upsertByConversationKey(task)
            // A further message on a conversation that already has an open task
            // bumps that task; only a genuinely new one is a creation.
            if (upsert.created) taskEvents.created(upsert.id)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
