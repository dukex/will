package tech.benedicta.will.notification

import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.task.TaskRepository

/**
 * The model's share of the notification work, done while Will is open.
 *
 * ML Kit's GenAI APIs only run while the app is the top foreground app. A
 * call from anywhere else comes back `BACKGROUND_USE_BLOCKED`, and a
 * foreground service does not get around it, which the official overview
 * states plainly. `WillNotificationListenerService` is a background service,
 * so asking the model as a notification arrives could never have worked on a
 * real phone whatever the prompt said.
 *
 * The split that falls out of it is a better one anyway. Everything that
 * needs no model still happens the moment a notification lands: the task is
 * created, the category is read, the pattern list and what the user taught
 * Will both run. This catches up afterwards, from the only place the model
 * can be reached.
 *
 * It only ever touches tasks that came from notifications. A task the user
 * typed is kept exactly as written, which `01-IDEA.md` promises, and one an
 * agent or another app created was not Will's to rewrite.
 */
class ModelReviewPass(
    private val tasks: TaskRepository,
    private val triage: SemanticNoiseReader,
    private val reader: SemanticIntentionReader,
    private val log: ModelTriageLog,
    private val phrase: (IntentionKind, String) -> String,
    private val triageEnabled: () -> Boolean,
    private val readingEnabled: () -> Boolean,
    private val now: () -> Long = System::currentTimeMillis
) {

    /**
     * Reviews everything that arrived after [since] and returns the watermark
     * the next pass should start from. Taken before the work so a
     * notification landing mid-pass is caught next time rather than missed.
     */
    suspend fun review(all: List<Task>, since: Long): Long {
        val watermark = now()

        all.filter { it.isWorthReviewing(since) }.forEach { task ->
            if (hidden(task)) return@forEach
            read(task)
        }
        return watermark
    }

    private fun Task.isWorthReviewing(since: Long): Boolean =
        origin == TaskOrigin.NOTIFICATION &&
            completedAt == null &&
            createdAt > since

    /** True when the task was judged noise and removed. */
    private suspend fun hidden(task: Task): Boolean {
        if (!triageEnabled()) return false
        if (log.isProtected(task.sourcePackage, task.rawTitle ?: task.title)) return false

        val judged = task.rawTitle ?: task.title
        val noise = runCatching { triage.isNoise(judged, task.text) }.getOrDefault(false)
        if (!noise) return false

        // Listed before it is removed, so there is no moment in which
        // something is hidden and cannot be found.
        log.recordDropped(task.sourcePackage, judged, now())
        runCatching { tasks.deleteTask(task) }
        return true
    }

    private suspend fun read(task: Task) {
        if (!readingEnabled()) return
        // A title the category already derived is answered; asking again
        // would spend inference to overwrite a better answer with a guess.
        if (task.rawTitle != null) return

        val read = runCatching { reader.read(task.title, task.text) }.getOrNull() ?: return
        runCatching {
            tasks.updateTask(
                task.copy(title = phrase(read.kind, read.subject), rawTitle = task.title)
            )
        }
    }
}
