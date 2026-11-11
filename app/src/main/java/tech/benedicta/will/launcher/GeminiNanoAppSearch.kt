package tech.benedicta.will.launcher

import androidx.annotation.RequiresApi
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Semantic search answered by Gemini Nano, on the device, through AICore.
 *
 * Only ever consulted when the name match found nothing, so most searches
 * never reach it. Everything that can go wrong ends in an empty answer and the
 * search behaves as it did before: the model is off, the device cannot serve
 * it, the model is not downloaded, it takes too long, or it replies with
 * something unreadable.
 *
 * It never downloads the model on its own. A download is megabytes over
 * someone's connection, and a search box is not the place to decide that.
 * Until the model is on the device this returns nothing.
 *
 * The prompt and the reply are handled by [SemanticSearchPrompt], which is
 * where the tests are. Inference itself needs a device.
 */
@RequiresApi(26)
class GeminiNanoAppSearch(
    private val enabled: () -> Boolean,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) : SemanticAppSearch {

    override suspend fun rank(query: String, apps: List<LauncherApp>): List<LauncherApp> {
        if (!enabled()) return emptyList()
        val prompt = SemanticSearchPrompt.build(query, apps) ?: return emptyList()

        return withTimeoutOrNull(timeoutMillis) {
            runCatching {
                val model = Generation.getClient()
                if (model.checkStatus() != FeatureStatus.AVAILABLE) return@runCatching emptyList()
                val answer = model.generateContent(prompt)
                    .candidates
                    .firstOrNull()
                    ?.text
                    .orEmpty()
                SemanticSearchPrompt.parse(answer, apps)
            }.getOrDefault(emptyList())
        }.orEmpty()
    }

    private companion object {
        /** A search box that waits longer than this has already failed. */
        const val DEFAULT_TIMEOUT_MILLIS = 2_000L
    }
}
