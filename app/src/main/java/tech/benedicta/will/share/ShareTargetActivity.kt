package tech.benedicta.will.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import tech.benedicta.will.R
import tech.benedicta.will.WillApplication
import tech.benedicta.will.task.TaskOrigin

/**
 * The way anything outside Will puts an intention on the home screen: the
 * Android share sheet, or a `will://task` link from a script, an automation
 * tool or an agent that can fire an intent.
 *
 * No UI of its own. It parses, hands the task to [WillApplication] and
 * finishes, so sharing to Will never pulls the user out of the app they were
 * in. The parsing lives in [SharedIntentParser], which is where the tests are.
 */
class ShareTargetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = when (intent?.action) {
            Intent.ACTION_SEND -> SharedIntentParser.fromSend(
                subject = intent.getStringExtra(Intent.EXTRA_SUBJECT),
                text = intent.getStringExtra(Intent.EXTRA_TEXT)
            )
            Intent.ACTION_VIEW -> SharedIntentParser.fromUri(intent.data?.toString())
            else -> null
        }

        if (shared == null) {
            toast(R.string.share_nothing_to_add)
        } else {
            // SHARED, not AGENT: an intent proves something outside Will sent
            // this in, not that an agent did. AGENT is for callers that
            // identify themselves, which is the AppFunctions surface.
            (application as WillApplication).acceptTaskFromOutside(
                shared = shared,
                origin = TaskOrigin.SHARED,
                sourcePackage = callerPackage()
            )
            toast(R.string.share_task_added)
        }

        finish()
    }

    /**
     * The app the share came from, so the feed can label and open it. Falls
     * back to the no-target sentinel when the caller is unknown or is Will
     * itself, which happens when a `will://` link is opened from inside.
     */
    private fun callerPackage(): String =
        referrer
            ?.takeIf { it.scheme == "android-app" }
            ?.host
            ?.takeIf { it != packageName }
            ?: TaskOrigin.MANUAL_SOURCE_PACKAGE

    private fun toast(messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()
    }
}
