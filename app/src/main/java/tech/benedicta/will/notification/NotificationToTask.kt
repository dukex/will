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
 */
class NotificationToTask(
    private val processor: NotificationProcessor,
    private val extractor: IntentionExtractor,
    private val phrase: (IntentionKind, String) -> String
) {

    fun convert(
        category: String?,
        title: String?,
        text: String?,
        sourcePackage: String,
        timestamp: Long,
        notificationKey: String? = null,
        conversationTitle: String? = null,
        shortcutId: String? = null
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

        val intention = extractor.extract(
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
