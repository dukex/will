package tech.benedicta.will.notification

import tech.benedicta.will.task.Task

/**
 * The whole conversion from a posted notification to a [Task]: the structural
 * mapping, then reading a conversation as the thing it is asking for.
 *
 * It exists so the path is testable. The notification service is an Android
 * component that cannot be unit tested, so everything it decides lives here
 * and the service is left holding only what it alone can do: read the
 * notification's fields and turn an [IntentionKind] into words with a string
 * resource.
 *
 * [phrase] is that last step, injected so this stays free of Android and the
 * wording still follows the phone's language.
 *
 * [keep] is triage, run between the structural mapping and the model so the
 * order is part of the tested path rather than something the service has to
 * remember.
 *
 * `suspend` because [reader] may have to wait on the on-device model. It only
 * ever does so for a notification whose category said nothing, and when it
 * answers with nothing the task is created exactly as it was before the model
 * existed.
 */
class NotificationToTask(
    private val processor: NotificationProcessor,
    private val reader: ReadIntentionWithMeaning,
    private val phrase: (IntentionKind, String) -> String
) {

    suspend fun convert(
        category: String?,
        title: String?,
        text: String?,
        sourcePackage: String,
        timestamp: Long,
        notificationKey: String? = null,
        conversationTitle: String? = null,
        shortcutId: String? = null,
        keep: suspend (Task) -> Boolean = { true }
    ): Task? {
        val plain = processor.process(
            title = title,
            text = text,
            sourcePackage = sourcePackage,
            timestamp = timestamp,
            notificationKey = notificationKey,
            conversationTitle = conversationTitle,
            shortcutId = shortcutId
        ) ?: return null

        // Triage before the model, never after. A notification the user is
        // never going to see is not worth waiting on inference for, and its
        // words have no business reaching the model at all.
        if (!keep(plain)) return null

        val intention = reader.read(
            category = category,
            title = plain.title,
            text = plain.text,
            sourcePackage = plain.sourcePackage
        ) ?: return plain

        // The conversation key is left as the processor built it, from the
        // original title: deriving the title must not split one conversation
        // into two tasks.
        return plain.copy(
            title = phrase(intention.kind, intention.subject),
            rawTitle = intention.rawTitle
        )
    }
}
