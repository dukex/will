package tech.benedicta.will.infrastructure.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.onboarding.OnboardingRepository

class MemoryOnboardingRepository(initial: Boolean = false) : OnboardingRepository {
    private val _completed = MutableStateFlow(initial)

    override fun isCompleted(): Flow<Boolean> = _completed.asStateFlow()

    override suspend fun markCompleted() {
        _completed.value = true
    }

    override suspend fun reset() {
        _completed.value = false
    }
}
