package tech.benedicta.will.ui

/**
 * How long attention lasted, for the one place it is read: a label under a
 * chart. Pure, so the rounding is tested rather than eyeballed.
 *
 * Seconds round down. A stretch shorter than a minute is shown as nothing,
 * because rounding it up to "1m" would report attention the user never spent.
 */
fun attentionDuration(millis: Long): String {
    val totalMinutes = if (millis <= 0L) 0L else millis / 60_000L
    if (totalMinutes <= 0L) return NOTHING

    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0L -> "${minutes}m"
        minutes == 0L -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

/** An em dash reads as "no measurement", where "0m" reads as a measured zero. */
private const val NOTHING = "—"
