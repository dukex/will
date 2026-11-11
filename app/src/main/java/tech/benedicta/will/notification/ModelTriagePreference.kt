package tech.benedicta.will.notification

import kotlinx.coroutines.flow.Flow

/**
 * Whether the user has asked the on-device model to hide housekeeping
 * notifications.
 *
 * The third model switch, and the one with the sharpest consequence. Search
 * by meaning changes what a search finds; reading a notification changes a
 * title; this one decides whether something reaches the user at all. Letting
 * any of the other two carry it would be deciding that for them.
 *
 * Off until they say otherwise.
 */
interface ModelTriagePreference {
    fun isEnabled(): Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
}
