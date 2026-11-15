package tech.benedicta.will.launcher

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import tech.benedicta.will.MainActivity
import tech.benedicta.will.R
import tech.benedicta.will.task.Task

/**
 * Publishes the task in progress as a launcher shortcut, so what the user is
 * working on is reachable from outside Will.
 *
 * This is the Android half; [ActiveTaskShortcut] holds the rules and the tests.
 * Failures are swallowed: a shortcut is a convenience, and no launcher should
 * crash because a system surface refused one.
 */
class ActiveTaskShortcutPublisher(private val context: Context) {

    fun publish(task: Task?) {
        val spec = ActiveTaskShortcut.describe(task)
        if (spec == null) {
            runCatching {
                ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(ActiveTaskShortcut.ID))
            }
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra(EXTRA_TASK_ID, spec.taskId)
        }

        runCatching {
            ShortcutManagerCompat.pushDynamicShortcut(
                context,
                ShortcutInfoCompat.Builder(context, spec.id)
                    .setShortLabel(spec.shortLabel)
                    .setLongLabel(spec.longLabel)
                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(intent)
                    .build()
            )
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "tech.benedicta.will.ACTIVE_TASK_ID"
    }
}
