package tech.benedicta.will

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.benedicta.will.launcher.LauncherViewModel
import tech.benedicta.will.launcher.SystemAppRepository
import tech.benedicta.will.notification.WillNotificationListenerService
import tech.benedicta.will.task.RoomTaskRepository
import tech.benedicta.will.task.TaskViewModel
import tech.benedicta.will.task.WillDatabase
import tech.benedicta.will.ui.AppGrid
import tech.benedicta.will.ui.NotificationPermissionPrompt
import tech.benedicta.will.ui.TaskFeed
import tech.benedicta.will.ui.theme.WillTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
private fun WillScreen(
    apps: List<tech.benedicta.will.launcher.LauncherApp>,
    tasks: List<tech.benedicta.will.task.Task>,
    notificationPermissionGranted: Boolean,
    onGrantPermission: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Tasks")
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Apps")
            }
        }
        when (selectedTab) {
            0 -> {
                if (!notificationPermissionGranted) {
                    NotificationPermissionPrompt(onGrantPermission = onGrantPermission)
                }
                TaskFeed(tasks = tasks, modifier = Modifier.weight(1f))
            }
            1 -> AppGrid(apps = apps, modifier = Modifier.weight(1f))
        }
    }
}
