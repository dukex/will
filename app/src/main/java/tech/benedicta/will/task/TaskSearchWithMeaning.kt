package tech.benedicta.will.task

/**
 * The drawer's task search: exact first, meaning second.
 *
 * The same contract `SearchWithMeaning` gives the app half, so one box
 * behaves one way. Until now it did not: apps were found by meaning and tasks
 * only by their letters, so "banco" found the bank app and never the task
 * about paying a boleto.
 *
 * The model is never the only thing between the user and their own work. It
 * cannot remove a result the matcher found, it cannot name a task that is not
 * in the feed (it answers with numbers from a list Will gave it), it never
 * sees a finished task, and when it is off, unavailable, slow or broken the
 * search behaves exactly as it did before.
 *
 * It runs in the app drawer, which is the launcher's own foreground. That
 * matters: ML Kit refuses to infer anywhere else, which is what made the
 * notification path need `ModelReviewPass`.
 */
class TaskSearchWithMeaning(
    private val matcher: TaskMatcher,
    private val semantic: SemanticTaskSearch = SemanticTaskSearch.Unavailable
) {

    suspend fun filter(tasks: List<Task>, query: String): List<Task> {
        if (query.isBlank()) return emptyList()

        val byWords = matcher.filter(tasks, query)
        if (byWords.isNotEmpty()) return byWords

        val open = tasks.filter { it.completedAt == null }
        val byId = open.associateBy { it.id }
        return runCatching { semantic.rank(query, open) }
            .getOrDefault(emptyList())
            .mapNotNull { byId[it.id] }
            .take(TaskMatcher.MAX_RESULTS)
    }
}
