package tech.benedicta.will.notification

import tech.benedicta.will.task.Task

/**
 * Structural mapping from a posted notification to a [Task]: what is blank,
 * what the conversation key is, which flags mean "not a user-facing
 * notification at all". Whether the content is worth the user's attention is
 * [IntentionClassifier]'s judgement, not this one's.
 */
class NotificationProcessor {

    fun process(
        title: String?,
        text: String?,
        sourcePackage: String,
        timestamp: Long,
        notificationKey: String? = null,
        conversationTitle: String? = null,
        shortcutId: String? = null
    ): Task? {
        if (sourcePackage.isBlank()) return null
        if (sourcePackage in IGNORED_PACKAGES) return null

        val resolvedTitle = when {
            !title.isNullOrBlank() -> title
            !text.isNullOrBlank() -> text
            else -> return null
        }

        val resolvedText = if (!title.isNullOrBlank() && !text.isNullOrBlank()) text else ""
        val keySource = conversationTitle?.takeIf { it.isNotBlank() } ?: resolvedTitle
        val normalizedKey = MESSAGE_COUNT_SUFFIX.replace(keySource, "").trim().lowercase()

        return Task(
            title = resolvedTitle,
            text = resolvedText,
            sourcePackage = sourcePackage,
            createdAt = timestamp,
            notificationKey = notificationKey,
            conversationKey = "$sourcePackage:$normalizedKey",
            shortcutId = shortcutId
        )
    }

    fun shouldIgnoreFlags(flags: Int): Boolean =
        flags and FLAG_GROUP_SUMMARY != 0 ||
        flags and FLAG_FOREGROUND_SERVICE != 0 ||
        flags and FLAG_ONGOING_EVENT != 0

    companion object {
        private const val FLAG_GROUP_SUMMARY = 0x00000200
        private const val FLAG_FOREGROUND_SERVICE = 0x00000040
        private const val FLAG_ONGOING_EVENT = 0x00000002

        private val MESSAGE_COUNT_SUFFIX =
            Regex("\\s*\\(\\d+ messages?\\).*$", RegexOption.IGNORE_CASE)

        val IGNORED_PACKAGES: Set<String> = setOf(
            "com.google.android.apps.wellbeing",
            "android",
            "com.android.systemui",
            "com.google.android.deskclock",
            "com.motorola.ccc.ota",
        )
    }
}
