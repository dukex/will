package tech.benedicta.will.task

/**
 * Builds the question put to the on-device model about the user's own tasks,
 * and reads its answer.
 *
 * The same shape as `SemanticSearchPrompt` for apps, for the same reason: the
 * tasks are numbered and the answer comes back as numbers, so a reply can
 * only ever name a task the user already has. A model that invents one, lists
 * everything or answers in prose yields nothing or a bounded subset, never a
 * row nobody wrote.
 *
 * The message is offered alongside the title, because a task that reads
 * "responder Maria" may be the one about a contract only because Maria said
 * so.
 */
object TaskSearchPrompt {

    /**
     * Gemini Nano's context is under 4,000 tokens, so the feed is cut rather
     * than the request failing. Tasks carry a message as well as a title, so
     * this is lower than the app list's sixty names.
     */
    const val MAX_TASKS = 30

    /** A search answers with a handful of candidates, not the whole feed. */
    const val MAX_RESULTS = 5

    fun build(query: String, tasks: List<Task>): String? {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || tasks.isEmpty()) return null

        val offered = tasks.take(MAX_TASKS)
            .mapIndexed { index, task ->
                val said = task.text.trim().take(MESSAGE_LIMIT)
                if (said.isEmpty()) "${index + 1}. ${task.title}" else "${index + 1}. ${task.title} — $said"
            }
            .joinToString("\n")

        return """
            Someone searched their own to-do list for: "$trimmed"

            Which of these open tasks did they most likely mean?

            $offered

            Answer with the numbers only, best first, at most $MAX_RESULTS of them.
            If none of them fit, answer with nothing.
        """.trimIndent()
    }

    fun parse(answer: String, tasks: List<Task>): List<Task> {
        val offered = tasks.take(MAX_TASKS)
        return NUMBER.findAll(answer)
            .mapNotNull { it.value.toIntOrNull() }
            .filter { it in 1..offered.size }
            .map { offered[it - 1] }
            .distinct()
            .take(MAX_RESULTS)
            .toList()
    }

    /** Enough of the message to recognise it, not enough to spend the context. */
    private const val MESSAGE_LIMIT = 80

    /**
     * A run of digits that is not part of a larger or negative number, so
     * "-3" is not read as a choice of 3.
     */
    private val NUMBER = Regex("""(?<![\d-])\d+""")
}
