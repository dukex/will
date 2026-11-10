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
 */
class TaskMatcher {

    fun filter(tasks: List<Task>, query: String): List<Task> {
        val needle = SearchText.fold(query)
        if (needle.isEmpty()) return emptyList()

        return tasks
            .asSequence()
            .filter { it.completedAt == null }
            .mapIndexedNotNull { position, task ->
                SearchText.score(task.title, needle)?.let { Triple(task, it, position) }
            }
            .sortedWith(compareBy({ it.second }, { it.third }))
            .map { it.first }
            .take(MAX_RESULTS)
            .toList()
    }

    companion object {
        /** Enough to find what you meant, few enough to leave the apps visible. */
        const val MAX_RESULTS = 5
    }
}
