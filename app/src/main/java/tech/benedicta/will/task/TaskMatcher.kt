package tech.benedicta.will.task

import tech.benedicta.will.launcher.SearchText

/**
 * Finds open tasks by what the user typed, with the same rules the app drawer
 * uses, so one search box behaves the same way over both.
 *
 * Finished tasks are left out: they have their own screen, and the search is
 * for finding work still in front of you.
 *
 * An empty query finds nothing rather than everything. The feed is already on
 * screen; repeating it under the search box would be noise.
 *
 * Three things are searched, in the order they are worth: the title, the title
 * the notification arrived with before US-19 rewrote it, and the message
 * itself. Without the last two a task that reads "responder Maria" could not
 * be found by the word Maria actually wrote, and a task whose title was
 * derived could not be found by what the user remembers seeing.
 */
class TaskMatcher {

    fun filter(tasks: List<Task>, query: String): List<Task> {
        val needle = SearchText.fold(query)
        if (needle.isEmpty()) return emptyList()

        return tasks
            .asSequence()
            .filter { it.completedAt == null }
            .mapIndexedNotNull { position, task ->
                score(task, needle)?.let { Triple(task, it, position) }
            }
            .sortedWith(compareBy({ it.second }, { it.third }))
            .map { it.first }
            .take(MAX_RESULTS)
            .toList()
    }

    /**
     * The best of what matched. A hit anywhere in the title outranks any hit
     * in the message, because the title is what the user is looking at.
     */
    private fun score(task: Task, needle: String): Int? {
        val inTitle = listOfNotNull(
            SearchText.score(task.title, needle),
            task.rawTitle?.let { SearchText.score(it, needle) }
        ).minOrNull()
        if (inTitle != null) return inTitle

        // Literal only: a message is long enough that a scattered subsequence
        // would match nearly any query.
        return SearchText.scoreLiteral(task.text, needle)?.plus(BEHIND_THE_TITLE)
    }

    companion object {
        /** Enough to find what you meant, few enough to leave the apps visible. */
        const val MAX_RESULTS = 5

        /** Past every title rank, so no message hit can outrank a title hit. */
        private const val BEHIND_THE_TITLE = 10
    }
}
