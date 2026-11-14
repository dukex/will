package tech.benedicta.will.task

import androidx.annotation.RequiresApi
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Finds the user's own tasks by meaning, with Gemini Nano, on the device.
 *
 * The same contract as `GeminiNanoAppSearch`, including its two-second
 * budget: this runs while someone is looking at a search box, and a box that
 * waits longer than that has already failed. Everything that can go wrong
 * ends in an empty answer and the search behaves as it did before.
 *
 * It runs in the app drawer, which is the launcher's own foreground, so ML
 * Kit's foreground-only rule is satisfied without the deferral the
 * notification path needed.
 *
 * The prompt and the reply are handled by [TaskSearchPrompt], which is where
 * the tests are. Inference itself needs a device.
 */
@RequiresApi(26)
class GeminiNanoTaskSearch(
    private val enabled: () -> Boolean,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) : SemanticTaskSearch {

    override suspend fun rank(query: String, tasks: List<Task>): List<Task> {
        if (!enabled()) return emptyList()
        val prompt = TaskSearchPrompt.build(query, tasks) ?: return emptyList()

        return withTimeoutOrNull(timeoutMillis) {
            runCatching {
                val model = Generation.getClient()
                if (model.checkStatus() != FeatureStatus.AVAILABLE) return@runCatching emptyList()
                val answer = model.generateContent(prompt)
                    .candidates
                    .firstOrNull()
                    ?.text
                    .orEmpty()
                TaskSearchPrompt.parse(answer, tasks)
            }.getOrDefault(emptyList())
        }.orEmpty()
    }

    private companion object {
        /** A search box that waits longer than this has already failed. */
        const val DEFAULT_TIMEOUT_MILLIS = 2_000L
    }
}
