package tech.benedicta.will.task

/**
 * Finds the user's own tasks by what they meant rather than by the words they
 * happened to use.
 *
 * `TaskMatcher` answers "is this word in the task"; this answers "which of
 * these is about money". The two are complementary, which is why this only
 * runs when the matcher found nothing.
 *
 * [Unavailable] is what every device gets when the model is off, absent or
 * not yet downloaded, and it is the default, so forgetting to wire a model
 * leaves the old behaviour rather than a crash.
 */
fun interface SemanticTaskSearch {
    suspend fun rank(query: String, tasks: List<Task>): List<Task>

    companion object {
        val Unavailable = SemanticTaskSearch { _, _ -> emptyList() }
    }
}
