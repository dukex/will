package tech.benedicta.will.task

/**
 * Where a task came from.
 *
 * Persisted by name so the stored value survives any reordering of the
 * constants. Before this existed, `sourcePackage` carried the distinction
 * through a single magic value, [MANUAL_SOURCE_PACKAGE].
 */
enum class TaskOrigin {
    /** Intercepted from a posted notification. */
    NOTIFICATION,

    /** Typed by the user in Will. */
    MANUAL,

    /** Sent in from another app, through the share sheet or a `will://` link. */
    SHARED,

    /** Written by an agent calling Will, not by the user. */
    AGENT;

    companion object {
        const val MANUAL_SOURCE_PACKAGE = "manual"

        /**
         * Reads a persisted origin. An unrecognised value reads as
         * [NOTIFICATION] rather than throwing, because the feed has to render
         * even when a row was written by a build that knew more origins than
         * this one.
         */
        fun fromStored(stored: String?): TaskOrigin =
            entries.firstOrNull { it.name == stored } ?: NOTIFICATION

        /**
         * The rule the version 7 to 8 migration applies to rows written before
         * the column existed.
         */
        fun forLegacySourcePackage(sourcePackage: String): TaskOrigin =
            if (sourcePackage == MANUAL_SOURCE_PACKAGE) MANUAL else NOTIFICATION
    }
}

/**
 * True when the task reached the feed without the user typing it and without a
 * notification they already saw. Those tasks have to be visibly marked: the
 * user never asked for them in Will itself.
 */
val Task.isAuthoredElsewhere: Boolean
    get() = origin == TaskOrigin.SHARED || origin == TaskOrigin.AGENT
