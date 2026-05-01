package tech.benedicta.will.notification

import tech.benedicta.will.task.Task

class NotificationProcessor {

    fun process(
        title: String?,
        text: String?,
        sourcePackage: String,
        timestamp: Long,
        notificationKey: String? = null,
        conversationTitle: String? = null
    ): Task? {
        if (sourcePackage.isBlank()) return null
        if (sourcePackage in IGNORED_PACKAGES) return null

        val resolvedTitle = when {
            !title.isNullOrBlank() -> title
            !text.isNullOrBlank() -> text
            else -> return null
        }

        if (isSystemNoise(resolvedTitle)) return null

        val resolvedText = if (!title.isNullOrBlank() && !text.isNullOrBlank()) text else ""
        val keySource = conversationTitle?.takeIf { it.isNotBlank() } ?: resolvedTitle

        return Task(
            title = resolvedTitle,
            text = resolvedText,
            sourcePackage = sourcePackage,
            createdAt = timestamp,
            notificationKey = notificationKey,
            conversationKey = "$sourcePackage:${keySource.trim().lowercase()}"
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

        val IGNORED_PACKAGES: Set<String> = setOf(
            "com.google.android.apps.wellbeing",
            "android",
            "com.android.systemui",
            "com.google.android.deskclock",
            "com.motorola.ccc.ota",
        )

        private val NOISE_PATTERNS: List<Regex> = listOf(
            Regex("^Downloading \\d+ (video|file|image|photo)", RegexOption.IGNORE_CASE),
            Regex("^Charging complete$", RegexOption.IGNORE_CASE),
            Regex("^Update complete$", RegexOption.IGNORE_CASE),
            Regex("^Could not connect to", RegexOption.IGNORE_CASE),
            Regex("has no internet access$", RegexOption.IGNORE_CASE),
            Regex("has limited connectivity$", RegexOption.IGNORE_CASE),
            Regex("^Upcoming alarm$", RegexOption.IGNORE_CASE),
            Regex("^Your bedtime starts at", RegexOption.IGNORE_CASE),
            Regex("^Alarm volume", RegexOption.IGNORE_CASE),
        )

        fun isSystemNoise(title: String): Boolean =
            NOISE_PATTERNS.any { it.containsMatchIn(title) }
    }
}
