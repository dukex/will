package tech.benedicta.will.infrastructure

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.onboarding.OnboardingRepository

class SharedPrefsOnboardingRepository(context: Context) : OnboardingRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _completed = MutableStateFlow(prefs.getBoolean(KEY_COMPLETED, false))

    override fun isCompleted(): Flow<Boolean> = _completed.asStateFlow()

    override suspend fun markCompleted() {
        prefs.edit { putBoolean(KEY_COMPLETED, true) }
        _completed.value = true
    }

    override suspend fun reset() {
        prefs.edit { putBoolean(KEY_COMPLETED, false) }
        _completed.value = false
    }

    private companion object {
        const val PREFS_NAME = "onboarding"
        const val KEY_COMPLETED = "completed"
    }
}
