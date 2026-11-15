package tech.benedicta.will.notification

/** A kind and the words from the notification it applies to. */
data class ReadIntention(val kind: IntentionKind, val subject: String)

/**
 * Asks the on-device model what a notification is asking for, and reads the
 * answer.
 *
 * `IntentionExtractor` covers what the system itself declares: a message
 * category means reply, a missed call means call back. It cannot read
 * "Confirme sua presença" as something to confirm, because no category says
 * so. That is the gap `docs/99-TASKS.md` kept open under US-15, and a prompt
 * closes it without the labelled data a trained classifier needed.
 *
 * The answer space is narrow on purpose, the same way the app search's is.
 * There the model answers with numbers from a list Will offered, so it cannot
 * name an app nobody has installed. Here it picks a kind from a closed list
 * and a subject that has to appear in the notification, so it cannot invent a
 * person, a sum or an appointment that was never mentioned. Will writes the
 * final words itself from a string resource, so the phrasing follows the
 * phone's language rather than the model's.
 *
 * Nothing here can hide a notification. The worst a wrong answer does is give
 * a task a title the user disagrees with, and `Task.rawTitle` still holds what
 * arrived.
 */
object IntentionPrompt {

    /**
     * Longer than this and the model has summarised the notification rather
     * than named what it is about.
     */
    const val MAX_SUBJECT_LENGTH = 40

    fun build(title: String, text: String): String? {
        val heading = title.trim()
        val body = text.trim()
        if (heading.isEmpty() && body.isEmpty()) return null

        val kinds = IntentionKind.entries.joinToString("\n") { "- ${it.name}: ${it.describe()}" }

        return """
            A phone notification arrived.

            Title: $heading
            Body: $body

            Is it asking the person to do something? If so, say which of these
            it is asking for:

            $kinds

            Answer with the kind, a vertical bar, and the words from the
            notification it is about, copied exactly. For example:
            CONFIRM | the Friday meeting

            If it is not asking for any of those, answer NONE.
        """.trimIndent()
    }

    private fun IntentionKind.describe(): String = when (this) {
        IntentionKind.REPLY -> "write back to someone"
        IntentionKind.CALL_BACK -> "return a call"
        IntentionKind.CONFIRM -> "confirm or decline something"
        IntentionKind.PAY -> "pay something"
        IntentionKind.ATTEND -> "be somewhere at a time"
    }

    /**
     * The answer, or null whenever it is not unambiguous. Null is the safe
     * outcome: the task keeps the title the notification arrived with, which
     * is what every device without the model does.
     */
    fun parse(answer: String, title: String, text: String): ReadIntention? {
        val parts = answer.split('|', limit = 2)
        if (parts.size < 2) return null

        val named = IntentionKind.entries.filter { it.isNamedIn(parts[0]) }
        val kind = named.singleOrNull() ?: return null

        val subject = parts[1].trim().trimEnd('.', ',')
        if (subject.isEmpty() || subject.length > MAX_SUBJECT_LENGTH) return null

        val said = "${title.fold()} ${text.fold()}"
        if (!said.contains(subject.fold())) return null

        return ReadIntention(kind = kind, subject = subject)
    }

    private fun IntentionKind.isNamedIn(answer: String): Boolean =
        Regex("""\b${name}\b""", RegexOption.IGNORE_CASE).containsMatchIn(answer)

    /**
     * The model may normalise accents or case, and refusing its answer for
     * that would throw away a correct reading.
     */
    private fun String.fold(): String =
        trim().lowercase().map { ACCENTS[it] ?: it }.joinToString("")

    private val ACCENTS = mapOf(
        'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a',
        'é' to 'e', 'ê' to 'e',
        'í' to 'i',
        'ó' to 'o', 'õ' to 'o', 'ô' to 'o',
        'ú' to 'u',
        'ç' to 'c'
    )
}
