package tech.benedicta.will.lifecycle

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import tech.benedicta.will.WillApplication

/**
 * Reads only `event.packageName` from `TYPE_WINDOW_STATE_CHANGED` events
 * and feeds the foreground package into [TaskLifecycleApplier]. Never
 * inspects window content (`canRetrieveWindowContent=false` in the
 * service config).
 *
 * The user must enable this service in system settings; it is optional
 * — Will is a working launcher without it. When enabled, it powers
 * automatic startedAt timestamps and contextSwitchCount accumulation
 * for the active task.
 */
class WillAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString().orEmpty()
        if (packageName.isEmpty()) return

        val app = applicationContext as? WillApplication ?: run {
            Log.w(TAG, "applicationContext is not WillApplication; skipping event")
            return
        }
        scope.launch {
            runCatching { app.taskLifecycleApplier.onForegroundChange(packageName) }
                .onFailure { Log.e(TAG, "lifecycle applier failed", it) }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "WillAccessibilityService"
    }
}
