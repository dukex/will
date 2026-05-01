package tech.benedicta.will.onboarding

import kotlinx.coroutines.flow.Flow

/**
 * Persists whether the first-run onboarding has been cleared. Independent of
 * the actual permission grants, which can change at any time — this flag
 * captures whether the user has *seen* and *completed* the guided flow.
 */
interface OnboardingRepository {
    fun isCompleted(): Flow<Boolean>
    suspend fun markCompleted()
    suspend fun reset()
}
