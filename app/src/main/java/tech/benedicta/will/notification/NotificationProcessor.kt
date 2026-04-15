package tech.benedicta.will.notification

import tech.benedicta.will.task.Task

class NotificationProcessor {

    fun process(
        title: String?,
        text: String?,
        sourcePackage: String,
        timestamp: Long
    ): Task? {
        if (sourcePackage.isBlank()) return null
        if (sourcePackage in IGNORED_PACKAGES) return null

        val resolvedTitle = when {
            !title.isNullOrBlank() -> title
            !text.isNullOrBlank() -> text
            else -> return null
        }
        val resolvedText = if (!title.isNullOrBlank() && !text.isNullOrBlank()) text else ""

        return Task(
            title = resolvedTitle,
            text = resolvedText,
            sourcePackage = sourcePackage,
            createdAt = timestamp
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
            "com.google.android.apps.wellbeing",  // Digital Wellbeing
        )
    }
}
