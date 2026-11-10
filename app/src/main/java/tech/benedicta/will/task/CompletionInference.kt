package tech.benedicta.will.task

/** A task the log suggests is finished, waiting for the user to confirm. */
data class CompletionSuggestion(val taskId: Long, val suggestedAt: Long)

/**
 * Reads the task log and points out tasks that look finished.
 *
 * US-04 gave up on automatic completion because telling from inside another
 * app would have meant reading its window content, which costs more privacy
 * than the brand promise allows. The log reopens the question on the right
 * side of that line: this sees timestamps, event types and package names,
 * which is all the accessibility service ever exposes, and never a word of
 * anyone's content.
 *
 * It only ever suggests. Completing a task is still the user's tap, because
 * a wrong guess here would take work out of their sight without telling them.
 *
 * The rule, in order:
 * - the task is still open, so there is something to suggest;
 * - it was actually started, which is what separates "you dealt with this" from
 *   "you have not looked at it yet". A notification nobody opened is never
 *   finished, however long it has been sitting there;
 * - the user spent at least [minimumEngagement] on it, so a glance that bounced
 *   straight back out does not count;
 * - nothing has happened on it for [quietPeriod];
 * - and the last thing that happened is no older than [staleAfter], because
 *   asking about yesterday's task is noise rather than help.
 */
class CompletionInference(
    private val quietPeriod: Long = 20 * 60 * 1000L,
    private val minimumEngagement: Long = 30 * 1000L,
    private val staleAfter: Long = 24 * 60 * 60 * 1000L
) {

    fun suggestionFor(task: Task, events: List<TaskEvent>, now: Long): CompletionSuggestion? {
        if (task.completedAt != null) return null

        val own = events.filter { it.taskId == task.id }.sortedWith(compareBy({ it.at }, { it.id }))
        val last = own.lastOrNull() ?: return null

        val quietFor = now - last.at
        if (quietFor < quietPeriod) return null
        if (quietFor > staleAfter) return null

        val engagementStart = own.lastOrNull { it.type.startsWork() } ?: return null
        val engagementEnd = own.firstOrNull { it.at > engagementStart.at }?.at ?: now
        if (engagementEnd - engagementStart.at < minimumEngagement) return null

        return CompletionSuggestion(taskId = task.id, suggestedAt = now)
    }

    /** The ids of every task in [tasks] that looks finished. */
    fun suggestedTaskIds(tasks: List<Task>, events: List<TaskEvent>, now: Long): Set<Long> {
        val byTask = events.groupBy { it.taskId }
        return tasks
            .mapNotNull { task -> suggestionFor(task, byTask[task.id].orEmpty(), now) }
            .map { it.taskId }
            .toSet()
    }

    /** The window of log history the feed has to read for suggestions to work. */
    fun historyWindow(now: Long): Long = now - staleAfter

    private fun TaskEventType.startsWork(): Boolean =
        this == TaskEventType.STARTED || this == TaskEventType.RESUMED
}
