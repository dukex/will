package tech.benedicta.will.infrastructure.memory

import tech.benedicta.will.notification.LearnedNoiseRepository
import tech.benedicta.will.notification.ModelDrop
import tech.benedicta.will.notification.ModelTriageLog

/** In-memory [ModelTriageLog], used in tests. */
class MemoryModelTriageLog : ModelTriageLog {

    private val drops = linkedMapOf<String, ModelDrop>()
    private val protectedKeys = mutableSetOf<String>()

    override suspend fun recordDropped(sourcePackage: String, title: String, at: Long) {
        val key = LearnedNoiseRepository.keyOf(sourcePackage, title) ?: return
        if (key in protectedKeys) return
        drops.remove(key)
        drops[key] = ModelDrop(sourcePackage, title.trim(), at)
        while (drops.size > ModelTriageLog.MAX_REMEMBERED) {
            drops.remove(drops.keys.first())
        }
    }

    override suspend fun dropped(): List<ModelDrop> =
        drops.values.sortedByDescending { it.at }

    override suspend fun showAgain(sourcePackage: String, title: String) {
        val key = LearnedNoiseRepository.keyOf(sourcePackage, title) ?: return
        drops.remove(key)
        protectedKeys += key
    }

    override suspend fun isProtected(sourcePackage: String, title: String): Boolean =
        LearnedNoiseRepository.keyOf(sourcePackage, title)?.let { it in protectedKeys } == true

    override suspend fun forgetEverything() {
        drops.clear()
        protectedKeys.clear()
    }
}
