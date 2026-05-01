package tech.benedicta.will.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.benedicta.will.WillApplication

/**
 * Debug-only entry point that lets a screenshot script populate Will with a
 * curated dataset. Trigger from a host machine with:
 *
 *   adb shell am broadcast -a tech.benedicta.will.SEED_DEMO -p tech.benedicta.will
 *
 * Lives under `src/debug/` so it is absent from release builds.
 */
class DemoSeedReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_SEED_DEMO) return

        val app = context.applicationContext as? WillApplication ?: run {
            Log.w(TAG, "applicationContext is not WillApplication; skipping seed")
            return
        }

        val pendingResult = goAsync()
        scope.launch {
            try {
                DemoSeeder(app.taskRepository, app.pinnedAppsRepository).seed()
                Log.i(TAG, "demo seed applied")
            } catch (t: Throwable) {
                Log.e(TAG, "demo seed failed", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SEED_DEMO = "tech.benedicta.will.SEED_DEMO"
        private const val TAG = "DemoSeedReceiver"
    }
}
