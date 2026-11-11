package tech.benedicta.will.notification

import tech.benedicta.will.task.TaskOrigin

/**
 * Reads what a notification is asking for: from the system's own category
 * when it says, and from the words when only a model can tell.
 *
 * The same shape as `SearchWithMeaning` in the launcher. The answer that
 * needs no model comes first, on every device, and the model is asked only
 * the question it alone can answer. `IntentionExtractor` knows a message
 * means reply and a missed call means call back, because Android declares
 * those. Nothing declares that "Confirme sua presença" is something to
 * confirm.
 *
 * What the model cannot do bounds the damage. It cannot drop a notification:
 * when it declines, errors or is simply absent, the task is created with the
 * title that arrived, which is exactly today's behaviour. It cannot change
 * which conversation a task belongs to, because the conversation key is built
 * from the original title before any of this runs. And it cannot touch a task
 * the user typed, which `01-IDEA.md` promises is kept as written.
 */
class ReadIntentionWithMeaning(
    private val extractor: IntentionExtractor,
    private val semantic: SemanticIntentionReader = SemanticIntentionReader.Unavailable
) {

    suspend fun read(
        category: String?,
        title: String,
        text: String,
        sourcePackage: String
    ): ExtractedIntention? {
        extractor.extract(category, title, text, sourcePackage)?.let { return it }

        if (sourcePackage == TaskOrigin.MANUAL_SOURCE_PACKAGE) return null
        if (title.isBlank() && text.isBlank()) return null

        val read = runCatching { semantic.read(title, text) }.getOrNull() ?: return null
        return ExtractedIntention(kind = read.kind, subject = read.subject, rawTitle = title)
    }
}
