package tech.benedicta.will

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.benedicta.will.launcher.AppLaunchAction
import tech.benedicta.will.launcher.AppLaunchGate
import tech.benedicta.will.launcher.LauncherApp
import tech.benedicta.will.launcher.LauncherViewModel
import tech.benedicta.will.launcher.PinnedAppsViewModel
import tech.benedicta.will.launcher.SystemAppRepository
import tech.benedicta.will.notification.AppFilterViewModel
import tech.benedicta.will.onboarding.OnboardingViewModel
import tech.benedicta.will.overlay.ActiveTaskOverlayService
import tech.benedicta.will.task.FocusDashboardViewModel
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.task.TaskViewModel
import tech.benedicta.will.task.TaskStatus
import tech.benedicta.will.task.status
import tech.benedicta.will.ui.AppGrid
import tech.benedicta.will.ui.DoneTasksScreen
import tech.benedicta.will.ui.FocusDashboard
import tech.benedicta.will.ui.HomeTopBar
import tech.benedicta.will.ui.IntentForAppSheet
import tech.benedicta.will.ui.IntentInputSheet
import tech.benedicta.will.ui.OnboardingScreen
import tech.benedicta.will.ui.pinnedAppsItems
import tech.benedicta.will.ui.SettingsScreen
import tech.benedicta.will.ui.NotificationPermissionPrompt
import tech.benedicta.will.ui.PrivacySettings
import tech.benedicta.will.ui.TaskFeed
import tech.benedicta.will.ui.theme.WillTheme

private const val SWIPE_THRESHOLD = 100f

private enum class Screen {
    Home,
    DoneTasks,
    Settings,
    FocusDashboard,
    PrivacySettings
}

class MainActivity : ComponentActivity() {

    private lateinit var taskVm: TaskViewModel
    private var pendingActiveTask: Task? = null
    private var onboardingVm: OnboardingViewModel? = null
    private var launcherVm: LauncherViewModel? = null

    // Settings intents do not return useful data, but we still need a launcher so
    // we get the activity-result callback to refresh onboarding state when the
    // user returns from the system page.
    private val systemSettingsLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onboardingVm?.refresh()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            (application as WillApplication).overlayEvents.collect { event ->
                if (!::taskVm.isInitialized) return@collect
                taskVm.handleOverlayAction(event.action, event.taskId)
            }
        }
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val appRepository = SystemAppRepository(packageManager)
        val taskRepository = (application as WillApplication).taskRepository

        setContent {
            WillTheme {
                val app = application as WillApplication
                val onboardingViewModel: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModel.Factory(
                        app.onboardingRepository,
                        app.permissionInspector
                    )
                )
                onboardingVm = onboardingViewModel
                val onboardingState by onboardingViewModel.state.collectAsState()

                // Gate the launcher on the actual required grants, not just the
                // persisted "user has seen onboarding once" flag. If Will is no
                // longer the default launcher or the overlay permission has been
                // revoked, redirect the user back through onboarding for the
                // missing step. The notification listener is optional and only
                // gated by the first-walkthrough flag — we don't nag for it on
                // every cold start once the user has either granted or skipped it.
                val needsRequiredGrants =
                    !onboardingState.isDefaultLauncher ||
                    !onboardingState.hasOverlayPermission
                val firstWalkthroughActive = !onboardingState.isComplete

                if (needsRequiredGrants || firstWalkthroughActive) {
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onSetDefaultLauncher = { openDefaultLauncherSettings() },
                        onGrantOverlay = { openOverlayPermissionSettings() },
                        onGrantNotificationListener = { openNotificationListenerSettings() }
                    )
                    return@WillTheme
                }

                val launcherVm: LauncherViewModel = viewModel(
                    factory = LauncherViewModel.factory(appRepository)
                )
                this@MainActivity.launcherVm = launcherVm
                taskVm = viewModel(factory = TaskViewModel.Factory(taskRepository))

                val appFilterVm: AppFilterViewModel = viewModel(
                    factory = AppFilterViewModel.Factory(app.appFilterRepository)
                )

                val pinnedAppsVm: PinnedAppsViewModel = viewModel(
                    factory = PinnedAppsViewModel.factory(
                        app.pinnedAppsRepository,
                        launcherVm.apps
                    )
                )

                val filteredApps by launcherVm.filteredApps.collectAsState()
                val appSearchQuery by launcherVm.query.collectAsState()
                val tasks by taskVm.tasks.collectAsState()
                val pinnedApps by pinnedAppsVm.pinnedApps.collectAsState()
                val notificationPermissionGranted = isNotificationListenerEnabled()

                WillScreen(
                    apps = filteredApps,
                    appSearchQuery = appSearchQuery,
                    onAppSearchQueryChange = launcherVm::setQuery,
                    tasks = tasks,
                    taskVm = taskVm,
                    appFilterVm = appFilterVm,
                    pinnedApps = pinnedApps,
                    pinnedAppsVm = pinnedAppsVm,
                    notificationPermissionGranted = notificationPermissionGranted,
                    accessibilityServiceGranted = app.permissionInspector.hasAccessibilityService(),
                    onGrantPermission = { openNotificationListenerSettings() },
                    onSetPendingActiveTask = { task -> pendingActiveTask = task },
                    onRerunOnboarding = { onboardingViewModel.rerun() },
                    onOpenAccessibilitySettings = { openAccessibilitySettings() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val task = pendingActiveTask
            ?: if (::taskVm.isInitialized) taskVm.activeTask.value else null
        if (task != null && Settings.canDrawOverlays(this)) {
            ActiveTaskOverlayService.show(this, task.id, task.title, task.text)
        }
    }

    override fun onResume() {
        super.onResume()
        pendingActiveTask = null
        ActiveTaskOverlayService.hide(this)
        onboardingVm?.refresh()
        launcherVm?.refresh()
    }

    private fun isNotificationListenerEnabled(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

    private fun openNotificationListenerSettings() {
        systemSettingsLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun openAccessibilitySettings() {
        runCatching {
            systemSettingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun openOverlayPermissionSettings() {
        val withPackage = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:$packageName".toUri()
        )
        runCatching {
            systemSettingsLauncher.launch(withPackage)
        }.onFailure {
            systemSettingsLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }

    private fun openDefaultLauncherSettings() {
        runCatching {
            systemSettingsLauncher.launch(Intent(Settings.ACTION_HOME_SETTINGS))
        }
    }
}

private fun launchTask(context: android.content.Context, task: Task) {
    if (task.shortcutId != null && launchShortcut(context, task.sourcePackage, task.shortcutId)) return
    launchAppByPackage(context, task.sourcePackage)
}

private fun launchShortcut(
    context: android.content.Context,
    packageName: String,
    shortcutId: String
): Boolean {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) return false
    val launcherApps = context.getSystemService(android.content.Context.LAUNCHER_APPS_SERVICE)
        as android.content.pm.LauncherApps
    return runCatching {
        launcherApps.startShortcut(packageName, shortcutId, null, null, android.os.Process.myUserHandle())
    }.isSuccess
}

private fun launchAppByPackage(context: android.content.Context, packageName: String) {
    context.packageManager.getLaunchIntentForPackage(packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ?.let { context.startActivity(it) }
}

@Composable
private fun AppDrawerOverlay(
    visible: Boolean,
    apps: List<LauncherApp>,
    query: String,
    onQueryChange: (String) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onDismiss: () -> Unit,
    onLongPressApp: (LauncherApp) -> Unit = {}
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
            onLaunchApp = onLaunchApp,
            onDismiss = onDismiss,
            onLongPressApp = onLongPressApp,
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
    pinnedApps: List<LauncherApp>,
    pinnedAppsVm: PinnedAppsViewModel,
    notificationPermissionGranted: Boolean,
    accessibilityServiceGranted: Boolean = false,
    onGrantPermission: () -> Unit,
    onSetPendingActiveTask: (Task) -> Unit = {},
    onRerunOnboarding: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var appDrawerOpen by remember { mutableStateOf(false) }
    var showIntentInputSheet by remember { mutableStateOf(false) }

    val onLongPressApp: (LauncherApp) -> Unit = { app ->
        scope.launch {
            currentScreen = Screen.Home
            appDrawerOpen = false
            onAppSearchQueryChange("")
            if (pinnedAppsVm.isPinned(app.packageName)) {
                pinnedAppsVm.unpin(app.packageName)
                snackbarHostState.showSnackbar("Unpinned ${app.name}")
            } else {
                val ok = pinnedAppsVm.pin(app.packageName)
                if (!ok) snackbarHostState.showSnackbar("Pin limit reached (5)")
            }
        }
    }

    var pendingAppLaunch by remember { mutableStateOf<LauncherApp?>(null) }
    val activeTask by taskVm.activeTask.collectAsState()

    val onLaunchApp: (LauncherApp) -> Unit = { app ->
        when (val action = AppLaunchGate.decide(activeTask, app.packageName, app.name)) {
            is AppLaunchAction.LaunchDirectly -> {
                launchAppByPackage(context, action.packageName)
            }
            is AppLaunchAction.PromptForIntent -> {
                pendingAppLaunch = app
            }
        }
    }

    val doneTasks = tasks.filter { it.status == TaskStatus.DONE }

    BackHandler(enabled = true) {
        when {
            currentScreen == Screen.PrivacySettings -> currentScreen = Screen.Settings
            currentScreen != Screen.Home -> currentScreen = Screen.Home
            appDrawerOpen -> appDrawerOpen = false
        }
    }

    when (currentScreen) {
        Screen.Settings -> {
            SettingsScreen(
                onBack = { currentScreen = Screen.Home },
                onNavigateToPrivacy = { currentScreen = Screen.PrivacySettings },
                onRerunOnboarding = onRerunOnboarding,
                accessibilityServiceGranted = accessibilityServiceGranted,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
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
                onBack = { currentScreen = Screen.Settings },
                onDeleteAllTasks = { taskVm.deleteAllTasks() }
            )
            return
        }
        Screen.DoneTasks -> {
            DoneTasksScreen(
                doneTasks = doneTasks,
                onBack = { currentScreen = Screen.Home },
                onDelete = { task -> taskVm.deleteTask(task) },
                onReopen = { task -> taskVm.reopenTask(task) }
            )
            return
        }
        Screen.Home -> Unit
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar(
                onAddTask = { showIntentInputSheet = true },
                onOpenSettings = { currentScreen = Screen.Settings },
                onOpenDashboard = { currentScreen = Screen.FocusDashboard },
                onOpenDoneTasks = { currentScreen = Screen.DoneTasks },
                onOpenApps = { appDrawerOpen = true },
                doneCount = doneTasks.size
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                val pinnedHeader: (androidx.compose.foundation.lazy.LazyListScope.() -> Unit)? =
                    if (pinnedApps.isNotEmpty()) {
                        {
                            pinnedAppsItems(
                                pinnedApps = pinnedApps,
                                onLaunch = onLaunchApp,
                                onUnpin = { app -> pinnedAppsVm.unpin(app.packageName) }
                            )
                        }
                    } else null

                TaskFeed(
                    tasks = tasks,
                    onTap = { task ->
                        onSetPendingActiveTask(task)
                        taskVm.setActiveTask(task)
                        launchTask(context, task)
                    },
                    onMarkDone = { task ->
                        taskVm.markDone(task)
                    },
                    header = pinnedHeader,
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
                    onLaunchApp = onLaunchApp,
                    onDismiss = { appDrawerOpen = false },
                    onLongPressApp = onLongPressApp
                )

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    pendingAppLaunch?.let { app ->
        IntentForAppSheet(
            appName = app.name,
            onDismiss = { pendingAppLaunch = null },
            onStart = { intent ->
                taskVm.addTaskAndStart(
                    Task(
                        title = intent,
                        text = "",
                        sourcePackage = app.packageName,
                        createdAt = System.currentTimeMillis(),
                        origin = TaskOrigin.MANUAL
                    )
                )
                launchAppByPackage(context, app.packageName)
                pendingAppLaunch = null
            },
            onOpenAnyway = {
                launchAppByPackage(context, app.packageName)
                pendingAppLaunch = null
            }
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
                        sourcePackage = TaskOrigin.MANUAL_SOURCE_PACKAGE,
                        createdAt = System.currentTimeMillis(),
                        origin = TaskOrigin.MANUAL
                    )
                )
                showIntentInputSheet = false
            }
        )
    }
}
