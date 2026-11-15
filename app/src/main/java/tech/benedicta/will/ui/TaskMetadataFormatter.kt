package tech.benedicta.will.ui

import tech.benedicta.will.task.TaskOrigin

/**
 * Builds the one metadata line under a task title.
 *
 * Segments are joined with a middle dot in a fixed order, so the line stays
 * scannable down the feed: where it came from, how it got here, when, how much
 * is waiting. A segment with nothing to say is dropped rather than rendered
 * empty, which is why [appLabel] is nullable and the origin is usually silent.
 *
 * Pure so it can be tested without Android. [time] arrives already formatted,
 * which keeps locale handling at the call site.
 */
fun taskMetadataLine(
    appLabel: String?,
    origin: TaskOrigin,
    time: String,
    unreadCount: Int
): String = listOfNotNull(
    appLabel?.trim()?.takeIf { it.isNotEmpty() },
    origin.feedLabel(),
    time,
    unreadCount.takeIf { it > 1 }?.let { "$it new" }
).joinToString(" · ")

/**
 * How the origin reads in the feed, or null when it should stay quiet.
 *
 * A notification the user already saw and a task they typed are the ordinary
 * ways a task appears, and naming them would add a word to every row without
 * telling the user anything. [TaskOrigin.SHARED] and [TaskOrigin.AGENT] are
 * the cases they did not author in Will, and those are worth a word.
 */
private fun TaskOrigin.feedLabel(): String? = when (this) {
    TaskOrigin.NOTIFICATION, TaskOrigin.MANUAL -> null
    TaskOrigin.SHARED -> "shared"
    TaskOrigin.AGENT -> "from agent"
}
