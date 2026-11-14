package tech.benedicta.will.infrastructure

import android.content.Context
import tech.benedicta.will.notification.LearnedNoiseRepository
import tech.benedicta.will.notification.LearnedNoiseRepository.Companion.DISMISSALS_BEFORE_SILENCED
import tech.benedicta.will.notification.LearnedNoiseRepository.Companion.keyOf
import tech.benedicta.will.notification.SilencedNotification

/**
 * What the user has taught Will about noise, kept in SharedPreferences
 * alongside the app filters, because it is the same kind of small per-app
 * preference and never grows past [MAX_REMEMBERED] entries.
 *
 * Each entry is `count|package|title`: the count decides, the package and
 * title are there so the user can be shown what was silenced.
 */
class SharedPrefsLearnedNoiseRepository(context: Context) : LearnedNoiseRepository {

    private val prefs = context.getSharedPreferences("learned_noise", Context.MODE_PRIVATE)

    override suspend fun recordDismissed(sourcePackage: String, title: String) {
        val key = keyOf(sourcePackage, title) ?: return
        val existing = prefs.getString(key, null)?.toEntry()
        val next = Entry(
            dismissals = (existing?.dismissals ?: 0) + 1,
            sourcePackage = sourcePackage,
            title = existing?.title ?: title.trim()
        )
        if (existing == null) evictIfFull()
        prefs.edit().putString(key, next.serialise()).apply()
    }

    override suspend fun recordKept(sourcePackage: String, title: String) {
        keyOf(sourcePackage, title)?.let { prefs.edit().remove(it).apply() }
    }

    override suspend fun isLearnedNoise(sourcePackage: String, title: String): Boolean {
        val key = keyOf(sourcePackage, title) ?: return false
        val entry = prefs.getString(key, null)?.toEntry() ?: return false
        return entry.dismissals >= DISMISSALS_BEFORE_SILENCED
    }

    override suspend fun silenced(): List<SilencedNotification> =
        prefs.all.values
            .mapNotNull { (it as? String)?.toEntry() }
            .filter { it.dismissals >= DISMISSALS_BEFORE_SILENCED }
            .map { SilencedNotification(it.sourcePackage, it.title, it.dismissals) }
            .sortedWith(compareBy({ it.sourcePackage }, { it.title }))

    override suspend fun forget(sourcePackage: String, title: String) {
        keyOf(sourcePackage, title)?.let { prefs.edit().remove(it).apply() }
    }

    override suspend fun forgetEverything() {
        prefs.edit().clear().apply()
    }

    /**
     * Drops the least-dismissed entry when the store is full, so a long-lived
     * install cannot grow this without bound. The entries worth keeping are
     * the ones the user has thrown away most often.
     */
    private fun evictIfFull() {
        if (prefs.all.size < MAX_REMEMBERED) return
        val weakest = prefs.all.entries
            .mapNotNull { entry -> (entry.value as? String)?.toEntry()?.let { entry.key to it } }
            .minByOrNull { it.second.dismissals }
            ?: return
        prefs.edit().remove(weakest.first).apply()
    }

    private data class Entry(val dismissals: Int, val sourcePackage: String, val title: String) {
        fun serialise() = "$dismissals|$sourcePackage|$title"
    }

    private fun String.toEntry(): Entry? {
        val parts = split("|", limit = 3)
        if (parts.size < 3) return null
        val count = parts[0].toIntOrNull() ?: return null
        return Entry(dismissals = count, sourcePackage = parts[1], title = parts[2])
    }

    private companion object {
        const val MAX_REMEMBERED = 200
    }
}
