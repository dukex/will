package tech.benedicta.will.notification

import androidx.annotation.RequiresApi
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads a notification's words with Gemini Nano, on the device, through
 * AICore.
 *
 * The same contract as `GeminiNanoAppSearch`, for the same reasons.
 * Everything that can go wrong ends in null and the notification keeps the
 * title it arrived with: the feature is off, the device cannot serve the
 * model, the model is not downloaded, it takes too long, or it answers with
 * something [IntentionPrompt] refuses to read.
 *
 * It never downloads the model on its own, and it is only ever reached for a
 * notification whose category told Will nothing.
 *
 * The prompt and the parsing are in [IntentionPrompt], which is where the
 * tests are. Inference itself needs a device.
 */
@RequiresApi(26)
class GeminiNanoIntentions(
    private val enabled: () -> Boolean,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) : SemanticIntentionReader {

    override suspend fun read(title: String, text: String): ReadIntention? {
        if (!enabled()) return null
        val prompt = IntentionPrompt.build(title, text) ?: return null

        return withTimeoutOrNull(timeoutMillis) {
            runCatching {
                val model = Generation.getClient()
                if (model.checkStatus() != FeatureStatus.AVAILABLE) return@runCatching null
                val answer = model.generateContent(prompt)
                    .candidates
                    .firstOrNull()
                    ?.text
                    .orEmpty()
                IntentionPrompt.parse(answer, title, text)
            }.getOrNull()
        }
    }

    private companion object {
        /**
         * Longer than the search's two seconds, because nobody is watching a
         * cursor blink: a notification arrives in the background and a task
         * that appears a moment later costs nothing. Still bounded, so a
         * stuck model cannot hold the listener's coroutine open.
         */
        const val DEFAULT_TIMEOUT_MILLIS = 5_000L
    }
}
