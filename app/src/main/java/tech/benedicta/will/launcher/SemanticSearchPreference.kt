package tech.benedicta.will.launcher

import kotlinx.coroutines.flow.Flow

/**
 * Whether the user has turned on searching by meaning.
 *
 * Off until they say otherwise. Turning it on brings Google's on-device model
 * into the picture, which is a change to what the launcher carries, so it is
 * theirs to make rather than a default.
 */
interface SemanticSearchPreference {
    fun isEnabled(): Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
}
