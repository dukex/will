package tech.benedicta.will.infrastructure.memory

import tech.benedicta.will.notification.LearnedNoiseRepository
import tech.benedicta.will.notification.LearnedNoiseRepository.Companion.DISMISSALS_BEFORE_SILENCED
import tech.benedicta.will.notification.LearnedNoiseRepository.Companion.keyOf
import tech.benedicta.will.notification.SilencedNotification

class MemoryLearnedNoiseRepository : LearnedNoiseRepository {

    private data class Counted(val sourcePackage: String, val title: String, val dismissals: Int)

    private val counts = mutableMapOf<String, Counted>()

    override suspend fun recordDismissed(sourcePackage: String, title: String) {
        val key = keyOf(sourcePackage, title) ?: return
        val existing = counts[key]
        counts[key] = Counted(
            sourcePackage = sourcePackage,
            title = existing?.title ?: title.trim(),
            dismissals = (existing?.dismissals ?: 0) + 1
        )
    }

    override suspend fun recordKept(sourcePackage: String, title: String) {
        val key = keyOf(sourcePackage, title) ?: return
        counts.remove(key)
    }

    override suspend fun isLearnedNoise(sourcePackage: String, title: String): Boolean {
        val key = keyOf(sourcePackage, title) ?: return false
        return (counts[key]?.dismissals ?: 0) >= DISMISSALS_BEFORE_SILENCED
    }

    override suspend fun silenced(): List<SilencedNotification> =
        counts.values
            .filter { it.dismissals >= DISMISSALS_BEFORE_SILENCED }
            .map { SilencedNotification(it.sourcePackage, it.title, it.dismissals) }

    override suspend fun forget(sourcePackage: String, title: String) {
        keyOf(sourcePackage, title)?.let { counts.remove(it) }
    }

    override suspend fun forgetEverything() = counts.clear()
}
