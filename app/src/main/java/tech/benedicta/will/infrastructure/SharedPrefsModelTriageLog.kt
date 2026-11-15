package tech.benedicta.will.infrastructure

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.notification.LearnedNoiseRepository.Companion.keyOf
import tech.benedicta.will.notification.ModelDrop
import tech.benedicta.will.notification.ModelTriageLog
import tech.benedicta.will.notification.ModelTriagePreference

/**
 * What the model hid, kept where the user can find it.
 *
 * One prefs file holds both halves, told apart by their key prefix: a
 * `drop:` entry is something hidden and listed, a `keep:` entry is the user
 * having said that shape is not noise. A `drop:` value is `at|package|title`,
 * split with a limit so a title containing the separator survives.
 */
class SharedPrefsModelTriageLog(context: Context) : ModelTriageLog {

    private val prefs = context.getSharedPreferences("model_triage", Context.MODE_PRIVATE)

    override suspend fun recordDropped(sourcePackage: String, title: String, at: Long) {
        val key = keyOf(sourcePackage, title) ?: return
        if (prefs.contains(KEPT + key)) return

        evictIfFull()
        prefs.edit().putString(DROPPED + key, "$at|$sourcePackage|${title.trim()}").apply()
    }

    override suspend fun dropped(): List<ModelDrop> =
        prefs.all
            .filterKeys { it.startsWith(DROPPED) }
            .values
            .mapNotNull { (it as? String)?.toDrop() }
            .sortedByDescending { it.at }

    override suspend fun showAgain(sourcePackage: String, title: String) {
        val key = keyOf(sourcePackage, title) ?: return
        prefs.edit()
            .remove(DROPPED + key)
            .putBoolean(KEPT + key, true)
            .apply()
    }

    override suspend fun isProtected(sourcePackage: String, title: String): Boolean =
        keyOf(sourcePackage, title)?.let { prefs.contains(KEPT + it) } == true

    override suspend fun forgetEverything() {
        prefs.edit().clear().apply()
    }

    /** The oldest entry goes, because the newest mistake is the one worth seeing. */
    private fun evictIfFull() {
        val entries = prefs.all.filterKeys { it.startsWith(DROPPED) }
        if (entries.size < ModelTriageLog.MAX_REMEMBERED) return
        val oldest = entries
            .mapNotNull { entry -> (entry.value as? String)?.toDrop()?.let { entry.key to it } }
            .minByOrNull { it.second.at }
            ?: return
        prefs.edit().remove(oldest.first).apply()
    }

    private fun String.toDrop(): ModelDrop? {
        val parts = split("|", limit = 3)
        if (parts.size < 3) return null
        val at = parts[0].toLongOrNull() ?: return null
        return ModelDrop(sourcePackage = parts[1], title = parts[2], at = at)
    }

    private companion object {
        const val DROPPED = "drop:"
        const val KEPT = "keep:"
    }
}

/** Its own file again, because hiding is a different decision from renaming. */
class SharedPrefsModelTriagePreference(context: Context) : ModelTriagePreference {

    private val prefs = context.getSharedPreferences("model_triage_pref", Context.MODE_PRIVATE)
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
