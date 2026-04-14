package tech.benedicta.will

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.benedicta.will.launcher.LauncherApp
import tech.benedicta.will.launcher.LauncherViewModel
import tech.benedicta.will.launcher.SystemAppRepository
import tech.benedicta.will.notification.WillNotificationListenerService
import tech.benedicta.will.task.RoomTaskRepository
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskViewModel
import tech.benedicta.will.task.WillDatabase
import tech.benedicta.will.ui.AppGrid
import tech.benedicta.will.ui.HomeTopBar
import tech.benedicta.will.ui.NotificationPermissionPrompt
import tech.benedicta.will.ui.TaskFeed
import tech.benedicta.will.ui.theme.WillTheme

private const val SWIPE_THRESHOLD = 100f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val appRepository = SystemAppRepository(packageManager)
        val taskRepository = RoomTaskRepository(WillDatabase.getInstance(this).taskDao())

        setContent {
            WillTheme {
                val launcherVm: LauncherViewModel = viewModel(
                    factory = LauncherViewModel.factory(appRepository)
                )
                val taskVm: TaskViewModel = viewModel(
                    factory = TaskViewModel.Factory(taskRepository)
                )
                val apps by launcherVm.apps.collectAsState()
                val tasks by taskVm.tasks.collectAsState()
                val notificationPermissionGranted = isNotificationListenerEnabled()

                WillScreen(
                    apps = apps,
                    tasks = tasks,
                    notificationPermissionGranted = notificationPermissionGranted,
                    onGrantPermission = { openNotificationListenerSettings() }
                )
            }
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val componentName = ComponentName(this, WillNotificationListenerService::class.java)
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

@Composable
private fun AppDrawerOverlay(visible: Boolean, apps: List<LauncherApp>) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier.fillMaxSize()
    ) {
        AppGrid(
            apps = apps,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
private fun WillScreen(
    apps: List<LauncherApp>,
    tasks: List<Task>,
    notificationPermissionGranted: Boolean,
    onGrantPermission: () -> Unit
) {
    var appDrawerOpen by remember { mutableStateOf(false) }
    var overscrollAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!appDrawerOpen && available.y < 0f) {
                    overscrollAccumulator += available.y
                    if (overscrollAccumulator < -SWIPE_THRESHOLD) {
                        appDrawerOpen = true
                        overscrollAccumulator = 0f
                    }
                } else {
                    overscrollAccumulator = 0f
                }
                return Offset.Zero
            }
        }
    }

    BackHandler(enabled = true) {
        if (appDrawerOpen) appDrawerOpen = false
        // else: home screen — back does nothing, launcher never exits
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .nestedScroll(nestedScrollConnection)
                    .pointerInput(appDrawerOpen) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (!appDrawerOpen && dragAmount < -SWIPE_THRESHOLD) appDrawerOpen = true
                        }
                    }
            ) {
                TaskFeed(tasks = tasks, modifier = Modifier.fillMaxSize())

                if (!notificationPermissionGranted) {
                    NotificationPermissionPrompt(
                        onGrantPermission = onGrantPermission,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                AppDrawerOverlay(visible = appDrawerOpen, apps = apps)
            }
        }
    }
}
