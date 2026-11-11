package tech.benedicta.will.launcher

/**
 * Builds the question put to the on-device model, and reads its answer.
 *
 * Kept apart from the model so the two things that can actually go wrong,
 * asking badly and misreading the reply, have tests. The inference itself
 * needs a device.
 *
 * The apps are numbered and the answer comes back as numbers, so a reply can
 * only ever name an app that was offered. A model that invents a name, lists
 * everything or answers in prose yields nothing or a bounded subset, never a
 * launch target nobody has installed.
 */
object SemanticSearchPrompt {

    /**
     * Gemini Nano's context is under 4,000 tokens, so the list is cut rather
     * than the request failing. Sixty names is a few hundred tokens.
     */
    const val MAX_APPS = 60

    /** A search answers with a handful of candidates, not a new app drawer. */
    const val MAX_RESULTS = 5

    fun build(query: String, apps: List<LauncherApp>): String? {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || apps.isEmpty()) return null

        val offered = apps.take(MAX_APPS)
            .mapIndexed { index, app -> "${index + 1}. ${app.name}" }
            .joinToString("\n")

        return """
            Someone searched their phone for: "$trimmed"

            Which of these installed apps did they most likely mean?

            $offered

            Answer with the numbers only, best first, at most $MAX_RESULTS of them.
            If none of them fit, answer with nothing.
        """.trimIndent()
    }

    fun parse(answer: String, apps: List<LauncherApp>): List<LauncherApp> {
        val offered = apps.take(MAX_APPS)
        return NUMBER.findAll(answer)
            .mapNotNull { it.value.toIntOrNull() }
            .filter { it in 1..offered.size }
            .map { offered[it - 1] }
            .distinct()
            .take(MAX_RESULTS)
            .toList()
    }

    /**
     * A run of digits that is not part of a larger or negative number, so
     * "-3" is not read as a choice of 3.
     */
    private val NUMBER = Regex("""(?<![\d-])\d+""")
}
