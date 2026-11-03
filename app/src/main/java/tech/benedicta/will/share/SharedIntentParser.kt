package tech.benedicta.will.share

import java.net.URI
import java.net.URLDecoder

/** A task an outside caller asked Will to create. */
data class SharedTask(val title: String, val text: String)

/**
 * Pure parser for the two ways something outside Will puts an intention on the
 * home screen: the Android share sheet (`ACTION_SEND`, `text/plain`) and a
 * `will://task` link.
 *
 * Kept free of Android types so it is unit-testable, and kept dumb on purpose:
 * the text arrives exactly as the caller wrote it, with no interpretation. The
 * only transformations are trimming, collapsing runs of whitespace, and
 * splitting a long body into a title plus the rest.
 */
object SharedIntentParser {

    const val SCHEME = "will"
    const val TASK_HOST = "task"
    const val PARAM_TITLE = "title"
    const val PARAM_TEXT = "text"

    /**
     * A task card shows one line. Anything past this goes to the body so the
     * feed stays readable when someone shares a whole paragraph.
     */
    const val MAX_TITLE_LENGTH = 120

    private val WHITESPACE_RUN = Regex("\\s+")

    /**
     * Builds a task from share-sheet extras. [subject] wins as the title when
     * present, which is what mail and browser shares populate; otherwise the
     * first line of [text] becomes the title.
     *
     * Returns null when there is nothing to act on, so the caller creates no
     * empty task.
     */
    fun fromSend(subject: String?, text: String?): SharedTask? {
        val cleanSubject = subject.normalise()
        val cleanText = text?.trim().orEmpty()

        if (cleanSubject.isNotEmpty()) {
            return SharedTask(title = cleanSubject.take(MAX_TITLE_LENGTH), text = cleanText)
        }
        if (cleanText.isEmpty()) return null

        return splitBody(cleanText)
    }

    /**
     * Builds a task from a `will://task?title=…&text=…` link. Any other scheme
     * or host returns null: Will answers for its own links only.
     */
    fun fromUri(uri: String?): SharedTask? {
        val parsed = runCatching { URI(uri ?: return null) }.getOrNull() ?: return null
        if (!parsed.scheme.equals(SCHEME, ignoreCase = true)) return null
        if (!parsed.host.equals(TASK_HOST, ignoreCase = true)) return null

        val params = queryParameters(parsed.rawQuery)
        val title = params[PARAM_TITLE].normalise()
        val text = params[PARAM_TEXT]?.trim().orEmpty()

        if (title.isNotEmpty()) {
            return SharedTask(title = title.take(MAX_TITLE_LENGTH), text = text)
        }
        if (text.isEmpty()) return null

        return splitBody(text)
    }

    private fun splitBody(body: String): SharedTask {
        // The remainder is cut from the raw line, not the normalised title:
        // collapsing whitespace changes the text and would stop it matching.
        val rawFirstLine = body.lineSequence().first { it.isNotBlank() }
        val title = rawFirstLine.normalise()
        if (title.length <= MAX_TITLE_LENGTH) {
            val rest = body.substringAfter(rawFirstLine, missingDelimiterValue = "").trim()
            return SharedTask(title = title, text = rest)
        }
        return SharedTask(title = title.take(MAX_TITLE_LENGTH), text = body)
    }

    private fun queryParameters(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrEmpty()) return emptyMap()
        return rawQuery.split("&")
            .mapNotNull { pair ->
                val name = pair.substringBefore("=", missingDelimiterValue = "")
                if (name.isEmpty() || !pair.contains("=")) return@mapNotNull null
                val value = pair.substringAfter("=")
                val decoded = runCatching { URLDecoder.decode(value, "UTF-8") }.getOrNull()
                    ?: return@mapNotNull null
                decode(name) to decoded
            }
            .toMap()
    }

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)

    private fun String?.normalise(): String =
        this?.replace(WHITESPACE_RUN, " ")?.trim().orEmpty()
}
