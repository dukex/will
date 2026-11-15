package tech.benedicta.will.provider

import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin

/**
 * Everything about a task that may reach an external provider.
 *
 * It is deliberately this small. Adding a field here is a decision to send
 * something new off the device, which is why the test asserts the field list
 * rather than only the values.
 */
data class ProviderPayload(
    val title: String,
    val isCompleted: Boolean
)

/**
 * What may leave the device for a task provider, and what may not.
 *
 * `01-IDEA.md` promises that tracking data is processed on-device and never
 * leaves it. That promise is about what Will *observed*: which app the user
 * switched to, how long a task was open, how many times they were pulled
 * away, and what a notification said. It was never about a task the user
 * typed and then asked Will to sync.
 *
 * So the line is authorship, not storage. A typed, shared or agent-written
 * task is the user's own words and syncing it is the feature they asked for.
 * A task Will built out of someone's WhatsApp message is not theirs to send,
 * and it carries `text` and `rawTitle`, which are the message itself.
 *
 * Syncing those would need its own opt-in, under the rule that one opt-in
 * does not carry another, and even then the message body would stay here.
 * Until that exists, this returns null for them.
 */
object ProviderSharing {

    fun payloadFor(task: Task): ProviderPayload? {
        if (!task.origin.isAuthoredByUser()) return null
        val title = task.title.trim()
        if (title.isEmpty()) return null
        return ProviderPayload(title = title, isCompleted = task.completedAt != null)
    }

    private fun TaskOrigin.isAuthoredByUser(): Boolean = when (this) {
        TaskOrigin.MANUAL, TaskOrigin.SHARED, TaskOrigin.AGENT -> true
        TaskOrigin.NOTIFICATION -> false
    }
}
