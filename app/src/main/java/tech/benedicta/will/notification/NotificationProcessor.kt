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

    companion object {
        val IGNORED_PACKAGES: Set<String> = setOf(
            "com.google.android.apps.wellbeing",  // Digital Wellbeing
        )
    }
}
