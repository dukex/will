package tech.benedicta.will.infrastructure

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.launcher.SemanticSearchPreference

class SharedPrefsSemanticSearchPreference(context: Context) : SemanticSearchPreference {

    private val prefs = context.getSharedPreferences("semantic_search", Context.MODE_PRIVATE)
    private val enabled = MutableStateFlow(prefs.getBoolean(KEY, false))

    override fun isEnabled(): Flow<Boolean> = enabled.asStateFlow()

    override suspend fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY, enabled).apply()
        this.enabled.value = enabled
    }

    /** Read without collecting, for the search path that cannot suspend on it. */
    fun enabledNow(): Boolean = enabled.value

    private companion object {
        const val KEY = "enabled"
    }
}
