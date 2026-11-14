package tech.benedicta.will.launcher

import tech.benedicta.will.task.Task
import tech.benedicta.will.task.isActive

/** What the system should show as the shortcut for the task in progress. */
data class ShortcutSpec(
    val id: String,
    val taskId: Long,
    val shortLabel: String,
    val longLabel: String
)

/**
 * Describes the task in progress as a launcher shortcut, so what the user is
 * working on is reachable from outside Will: a long press on the icon, and the
 * system surfaces that read shortcuts.
 *
 * Pure, so the rules have tests. Publishing it is the caller's job.
 */
object ActiveTaskShortcut {

    const val ID = "will.active_task"

    /** The system only renders a few characters here. */
    const val SHORT_LABEL_LIMIT = 10

    const val LONG_LABEL_LIMIT = 25

    fun describe(task: Task?): ShortcutSpec? {
        if (task == null || !task.isActive) return null
        val title = task.title.trim()
        if (title.isEmpty()) return null

        return ShortcutSpec(
            id = ID,
            taskId = task.id,
            shortLabel = title.take(SHORT_LABEL_LIMIT),
            longLabel = title.take(LONG_LABEL_LIMIT)
        )
    }
}
