package tech.benedicta.will.infrastructure

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.notification.DerivedIntentionPreference

/**
 * Its own prefs file rather than a second key in `semantic_search`: agreeing
 * to send the model a search term is not agreeing to send it what a
 * notification said, and one switch must never silently carry the other.
 */
class SharedPrefsDerivedIntentionPreference(context: Context) : DerivedIntentionPreference {

    private val prefs = context.getSharedPreferences("derived_intentions", Context.MODE_PRIVATE)
    private val enabled = MutableStateFlow(prefs.getBoolean(KEY, false))

    override fun isEnabled(): Flow<Boolean> = enabled.asStateFlow()

    override suspend fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY, enabled).apply()
        this.enabled.value = enabled
    }

    /** Read without collecting, for the listener path that cannot suspend on it. */
    fun enabledNow(): Boolean = enabled.value

    private companion object {
        const val KEY = "enabled"
    }
}
