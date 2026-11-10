package tech.benedicta.will.notification

/** What the notification is asking the user to do. */
enum class IntentionKind { REPLY, CALL_BACK }

/**
 * A notification read as something to do, with the title it was read from so
 * the derivation can be undone or shown.
 */
data class ExtractedIntention(
    val kind: IntentionKind,
    val subject: String,
    val rawTitle: String
)

/**
 * Turns a conversation notification into the thing it is actually asking for.
 *
 * `01-IDEA.md` wants a message alert to become "Responder Contato" rather than
 * a copy of the alert, and this is that step: a WhatsApp notification titled
 * "Maria" becomes replying to Maria, a missed call becomes calling back.
 *
 * It reads the notification's own category, which the system already attaches,
 * and nothing else. There is no language model here and no guessing at what a
 * sentence means: a notification that is not a conversation is left exactly as
 * it arrived, which is most of them.
 *
 * It never touches what the user typed. `01-IDEA.md` promises a typed
 * intention is kept "exatamente como o usuário a escreveu", and that path does
 * not come through here.
 *
 * The caller turns [IntentionKind] into words with a string resource, so the
 * phrasing follows the phone's language and this stays testable without
 * Android. [ExtractedIntention.rawTitle] keeps the original, so the feed can
 * show what the notification really said and the derivation is reversible.
 */
class IntentionExtractor {

    fun extract(
        category: String?,
        title: String,
        text: String,
        sourcePackage: String
    ): ExtractedIntention? {
        val kind = when (category) {
            CATEGORY_MESSAGE, CATEGORY_EMAIL -> IntentionKind.REPLY
            CATEGORY_MISSED_CALL -> IntentionKind.CALL_BACK
            else -> return null
        }

        val subject = title.subjectOrNull() ?: return null
        return ExtractedIntention(kind = kind, subject = subject, rawTitle = title)
    }

    /**
     * Who the conversation is with, or null when the title is not a name.
     *
     * Some apps write "Sender: message" into the title, so anything after a
     * colon is the message rather than the sender. A title longer than
     * [MAXIMUM_SUBJECT_LENGTH] is the message itself, which means this
     * notification does not carry a sender to reply to.
     */
    private fun String.subjectOrNull(): String? {
        val withoutCount = MESSAGE_COUNT_SUFFIX.replace(this, "").trim()
        val beforeColon = withoutCount.substringBefore(':').trim()
        if (beforeColon.isEmpty()) return null
        if (beforeColon.length > MAXIMUM_SUBJECT_LENGTH) return null
        if (beforeColon.none { it.isLetter() }) return null
        return beforeColon
    }

    companion object {
        const val CATEGORY_MESSAGE = "msg"
        const val CATEGORY_EMAIL = "email"
        const val CATEGORY_MISSED_CALL = "missed_call"
        const val CATEGORY_EVENT = "event"

        /**
         * Longer than this and the title is the message, not the person who
         * sent it. Names and group names sit well inside it.
         */
        private const val MAXIMUM_SUBJECT_LENGTH = 40

        private val MESSAGE_COUNT_SUFFIX =
            Regex("\\s*\\(\\d+ (messages?|mensagens?|mensagem)\\).*$", RegexOption.IGNORE_CASE)
    }
}
