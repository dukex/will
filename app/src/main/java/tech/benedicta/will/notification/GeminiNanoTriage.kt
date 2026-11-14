package tech.benedicta.will.notification

import androidx.annotation.RequiresApi
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Judges a notification as housekeeping with Gemini Nano, on the device.
 *
 * Same contract as the rest of the model surface: it never downloads the
 * model, and everything that can go wrong answers "not noise", so the
 * notification survives. [TriagePrompt] holds the question and the reading of
 * the answer, which is where the tests are. Inference itself needs a device.
 */
@RequiresApi(26)
class GeminiNanoTriage(
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) : SemanticNoiseReader {

    override suspend fun isNoise(title: String, text: String): Boolean {
        val prompt = TriagePrompt.build(title, text) ?: return false

        return withTimeoutOrNull(timeoutMillis) {
            runCatching {
                val model = Generation.getClient()
                if (model.checkStatus() != FeatureStatus.AVAILABLE) return@runCatching false
                val answer = model.generateContent(prompt)
                    .candidates
                    .firstOrNull()
                    ?.text
                    .orEmpty()
                TriagePrompt.parse(answer)
            }.getOrDefault(false)
        } ?: false
    }

    private companion object {
        /** A notification nobody is waiting on, but still bounded. */
        const val DEFAULT_TIMEOUT_MILLIS = 5_000L
    }
}
