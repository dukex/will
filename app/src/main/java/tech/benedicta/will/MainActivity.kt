package tech.benedicta.will

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import tech.benedicta.will.infrastructure.SharedPrefsAppFilterRepository
import tech.benedicta.will.launcher.LauncherApp
import tech.benedicta.will.launcher.LauncherViewModel
import tech.benedicta.will.launcher.SystemAppRepository
import tech.benedicta.will.notification.AppFilterViewModel
import tech.benedicta.will.notification.NotificationActionStore
import tech.benedicta.will.notification.WillNotificationListenerService
import tech.benedicta.will.overlay.ActiveTaskOverlayService
import tech.benedicta.will.task.FocusDashboardViewModel
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskViewModel
import tech.benedicta.will.ui.AppGrid
import tech.benedicta.will.ui.FocusDashboard
import tech.benedicta.will.ui.HomeTopBar
import tech.benedicta.will.ui.IntentInputSheet
import tech.benedicta.will.ui.NotificationFilterSettings
import tech.benedicta.will.ui.NotificationPermissionPrompt
import tech.benedicta.will.ui.PrivacySettings
import tech.benedicta.will.ui.TaskActionSheet
import tech.benedicta.will.ui.TaskFeed
import tech.benedicta.will.ui.loadThemeMode
import tech.benedicta.will.ui.theme.WillTheme

private const val SWIPE_THRESHOLD = 100f

private enum class Screen {
    Home,
    NotificationFilter,
    FocusDashboard,
    PrivacySettings
}

class MainActivity : ComponentActivity() {

    private lateinit var taskVm: TaskViewModel

    private val taskActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val taskId = intent.getLongExtra(ActiveTaskOverlayService.EXTRA_TASK_ID, -1L)
            if (taskId == -1L || !::taskVm.isInitialized) return
            val task = taskVm.tasks.value.find { it.id == taskId } ?: return
            when (intent.action) {
                ActiveTaskOverlayService.ACTION_DONE -> taskVm.markDone(task)
                ActiveTaskOverlayService.ACTION_DELETE -> taskVm.deleteTask(task)
                ActiveTaskOverlayService.ACTION_LATER -> Unit
            }
            taskVm.clearActiveTask()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val appRepository = SystemAppRepository(packageManager)
        val taskRepository = (application as WillApplication).taskRepository

        setContent {
            var themeMode by remember { mutableStateOf(loadThemeMode(this)) }

            WillTheme(themeMode = themeMode) {
                val launcherVm: LauncherViewModel = viewModel(
                    factory = LauncherViewModel.factory(appRepository)
                )
                taskVm = viewModel(factory = TaskViewModel.Factory(taskRepository))

                val context = androidx.compose.ui.platform.LocalContext.current
                val appFilterRepository = remember { SharedPrefsAppFilterRepository(context) }
                val appFilterVm: AppFilterViewModel = viewModel(
                    factory = AppFilterViewModel.Factory(appFilterRepository)
                )

                val filteredApps by launcherVm.filteredApps.collectAsState()
                val appSearchQuery by launcherVm.query.collectAsState()
                val tasks by taskVm.tasks.collectAsState()
                val notificationPermissionGranted = isNotificationListenerEnabled()

                WillScreen(
                    apps = filteredApps,
                    appSearchQuery = appSearchQuery,
                    onAppSearchQueryChange = launcherVm::setQuery,
                    tasks = tasks,
                    taskVm = taskVm,
                    appFilterVm = appFilterVm,
                    notificationPermissionGranted = notificationPermissionGranted,
                    onGrantPermission = { openNotificationListenerSettings() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val task = if (::taskVm.isInitialized) taskVm.activeTask.value else null
        if (task != null) {
            if (Settings.canDrawOverlays(this)) {
                ActiveTaskOverlayService.show(this, task.id, task.title, task.text)
            } else {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }
        val filter = IntentFilter().apply {
            addAction(ActiveTaskOverlayService.ACTION_DONE)
            addAction(ActiveTaskOverlayService.ACTION_LATER)
            addAction(ActiveTaskOverlayService.ACTION_DELETE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(taskActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(taskActionReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        ActiveTaskOverlayService.hide(this)
        runCatching { unregisterReceiver(taskActionReceiver) }
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
private fun AppDrawerOverlay(
    visible: Boolean,
    apps: List<LauncherApp>,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var overscrollAccumulator by remember { mutableFloatStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0f) {
                    overscrollAccumulator += available.y
                    if (overscrollAccumulator > SWIPE_THRESHOLD) {
                        onDismiss()
                        overscrollAccumulator = 0f
                    }
                } else {
                    overscrollAccumulator = 0f
                }
                return Offset.Zero
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier.fillMaxSize()
    ) {
        AppGrid(
            apps = apps,
            query = query,
            onQueryChange = onQueryChange,
            onDismiss = onDismiss,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(nestedScrollConnection)
        )
    }
}

@Composable
private fun WillScreen(
    apps: List<LauncherApp>,
    appSearchQuery: String,
    onAppSearchQueryChange: (String) -> Unit,
    tasks: List<Task>,
    taskVm: TaskViewModel,
    appFilterVm: AppFilterViewModel,
    notificationPermissionGranted: Boolean,
    onGrantPermission: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var appDrawerOpen by remember { mutableStateOf(false) }
    var overscrollAccumulator by remember { mutableFloatStateOf(0f) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showActionSheet by remember { mutableStateOf(false) }
    var showIntentInputSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Task?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
        when {
            currentScreen == Screen.PrivacySettings -> currentScreen = Screen.NotificationFilter
            currentScreen != Screen.Home -> currentScreen = Screen.Home
            appDrawerOpen -> appDrawerOpen = false
        }
    }

    when (currentScreen) {
        Screen.NotificationFilter -> {
            NotificationFilterSettings(
                onBack = { currentScreen = Screen.Home },
                onNavigateToPrivacy = { currentScreen = Screen.PrivacySettings },
                viewModel = appFilterVm
            )
            return
        }
        Screen.FocusDashboard -> {
            val allTasks by taskVm.tasks.collectAsState()
            FocusDashboard(
                viewModel = remember(allTasks) { FocusDashboardViewModel(allTasks) },
                onBack = { currentScreen = Screen.Home }
            )
            return
        }
        Screen.PrivacySettings -> {
            PrivacySettings(
                onBack = { currentScreen = Screen.NotificationFilter },
                onDeleteAllTasks = { taskVm.deleteAllTasks() }
            )
            return
        }
        Screen.Home -> Unit
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeTopBar(
                    onAddTask = { showIntentInputSheet = true },
                    onOpenFilter = { currentScreen = Screen.NotificationFilter },
                    onOpenDashboard = { currentScreen = Screen.FocusDashboard }
                )
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
                    TaskFeed(
                        tasks = tasks,
                        onTap = { task ->
                            taskVm.setActiveTask(task)
                            val storedIntent = NotificationActionStore.get(task.id)
                            val activeIntent = WillNotificationListenerService.instance?.getPendingIntentForTask(task.id)
                            val pendingIntent = storedIntent ?: activeIntent
                            if (pendingIntent != null) {
                                runCatching { pendingIntent.send(context, 0, null) }
                                    .onFailure { android.util.Log.e("WillTap", "send failed", it) }
                            } else {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(task.sourcePackage)
                                if (launchIntent != null) {
                                    launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(launchIntent)
                                }
                            }
                        },
                        onLongPress = { task ->
                            selectedTask = task
                            showActionSheet = true
                        },
                        onSwipeDone = { task ->
                            taskVm.markDone(task)
                        },
                        onSwipeDelete = { task ->
                            pendingDelete = task
                            taskVm.deleteTask(task)
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "task deleted",
                                    actionLabel = "undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    pendingDelete?.let { taskVm.addTask(it) }
                                }
                                pendingDelete = null
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (!notificationPermissionGranted) {
                        NotificationPermissionPrompt(
                            onGrantPermission = onGrantPermission,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    AppDrawerOverlay(
                        visible = appDrawerOpen,
                        apps = apps,
                        query = appSearchQuery,
                        onQueryChange = onAppSearchQueryChange,
                        onDismiss = { appDrawerOpen = false }
                    )
                }
            }
        }

        if (showActionSheet && selectedTask != null) {
            TaskActionSheet(
                task = selectedTask!!,
                onDismiss = { showActionSheet = false; selectedTask = null },
                onMarkDone = { taskVm.markDone(selectedTask!!); showActionSheet = false; selectedTask = null },
                onStart = { taskVm.startTask(selectedTask!!); showActionSheet = false; selectedTask = null },
                onPause = { taskVm.pauseTask(selectedTask!!); showActionSheet = false; selectedTask = null },
                onReopen = { taskVm.reopenTask(selectedTask!!); showActionSheet = false; selectedTask = null },
                onDelete = { taskVm.deleteTask(selectedTask!!); showActionSheet = false; selectedTask = null }
            )
        }

        if (showIntentInputSheet) {
            IntentInputSheet(
                onDismiss = { showIntentInputSheet = false },
                onAddTask = { title ->
                    taskVm.addTask(
                        Task(
                            title = title,
                            text = "",
                            sourcePackage = "manual",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    showIntentInputSheet = false
                }
            )
        }
    }
}
