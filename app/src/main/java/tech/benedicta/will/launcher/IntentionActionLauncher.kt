package tech.benedicta.will.launcher

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Turns a [ResolvedAction] into the Android intent that lands on the screen.
 *
 * The Android half of [IntentionActionResolver], kept apart so the rules stay
 * testable. Returns false when nothing on the phone can handle the action, so
 * the caller can fall back to opening the task's app as before.
 */
class IntentionActionLauncher(private val context: Context) {

    fun launch(resolved: ResolvedAction): Boolean {
        val intent = intentFor(resolved) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }

    private fun intentFor(resolved: ResolvedAction): Intent? {
        val argument = resolved.argument
        return when (resolved.action) {
            // ACTION_DIAL rather than ACTION_CALL: it opens the dialer with the
            // number ready and lets the user press call, and it needs no
            // permission. Will does not place calls on anyone's behalf.
            IntentionAction.DIAL -> Intent(Intent.ACTION_DIAL, "tel:${argument.asDialable()}".toUri())
            IntentionAction.EMAIL -> Intent(Intent.ACTION_SENDTO, "mailto:$argument".toUri())
            IntentionAction.MAP -> Intent(Intent.ACTION_VIEW, "geo:0,0?q=${argument.encoded()}".toUri())
            IntentionAction.BROWSE -> Intent(Intent.ACTION_VIEW, argument.toUri())
        }.takeIf { it.resolveActivity(context.packageManager) != null }
    }

    /**
     * The dialer only understands digits and the few symbols a number can
     * carry. A name is left for the user to search, which the dialer does.
     */
    private fun String.asDialable(): String =
        filter { it.isDigit() || it in "+*#-() " }.trim()

    private fun String.encoded(): String = android.net.Uri.encode(this)
}
