package tech.benedicta.will.notification

import kotlinx.coroutines.flow.Flow

/**
 * Whether the user has asked Will to read notifications with the on-device
 * model.
 *
 * Separate from the search preference on purpose. Searching by meaning sends
 * the model a word the user typed and a list of app names. This sends it what
 * a notification said, which is a different thing to agree to, and agreeing
 * to one is not agreeing to the other.
 *
 * Off until they say otherwise.
 */
interface DerivedIntentionPreference {
    fun isEnabled(): Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
}
