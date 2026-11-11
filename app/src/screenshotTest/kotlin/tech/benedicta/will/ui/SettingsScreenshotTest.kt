package tech.benedicta.will.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.tools.screenshot.PreviewTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.notification.AppFilterRepository
import tech.benedicta.will.notification.AppFilterViewModel
import tech.benedicta.will.launcher.SemanticSearchStatus
import tech.benedicta.will.notification.AppNotificationFilter
import tech.benedicta.will.ui.theme.WillTheme

private fun filtersFor(vararg apps: Pair<String, String>) = object : AppFilterRepository {
    private val filters = MutableStateFlow(
        apps.map { AppNotificationFilter(it.first, it.second, true) }
    )

    override fun getAllFilters(): Flow<List<AppNotificationFilter>> = filters.asStateFlow()
    override suspend fun setEnabled(packageName: String, enabled: Boolean) = Unit
    override suspend fun addApp(packageName: String, appName: String) = Unit
    override fun isEnabled(packageName: String): Boolean = true
}

/** Search by meaning is off until asked for, and says what turning it on brings. */
@PreviewTest
@Preview(name = "settings with search by meaning off", widthDp = 412, heightDp = 720, showBackground = true)
@Composable
private fun SettingsSemanticSearchOffPreview() {
    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(
            filtersFor("com.whatsapp" to "WhatsApp", "com.slack" to "Slack")
        )
    )
    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            accessibilityServiceGranted = true,
            semanticSearchEnabled = false,
            semanticSearchStatus = SemanticSearchStatus.OFF,
            viewModel = viewModel
        )
    }
}

/** The same row once it is on. */
@PreviewTest
@Preview(name = "settings with search by meaning on", widthDp = 412, heightDp = 720, showBackground = true)
@Composable
private fun SettingsSemanticSearchOnPreview() {
    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(
            filtersFor("com.whatsapp" to "WhatsApp", "com.slack" to "Slack")
        )
    )
    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            accessibilityServiceGranted = true,
            semanticSearchEnabled = true,
            semanticSearchStatus = SemanticSearchStatus.ON,
            viewModel = viewModel
        )
    }
}

/** On, but the model still has to be fetched, and only the user starts that. */
@PreviewTest
@Preview(name = "settings with the model to download", widthDp = 412, heightDp = 720, showBackground = true)
@Composable
private fun SettingsSemanticSearchNeedsDownloadPreview() {
    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(
            filtersFor("com.whatsapp" to "WhatsApp", "com.slack" to "Slack")
        )
    )
    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            accessibilityServiceGranted = true,
            semanticSearchEnabled = true,
            semanticSearchStatus = SemanticSearchStatus.NEEDS_DOWNLOAD,
            viewModel = viewModel
        )
    }
}

/** A phone that cannot serve the model says so instead of pretending. */
@PreviewTest
@Preview(name = "settings where the model is unsupported", widthDp = 412, heightDp = 720, showBackground = true)
@Composable
private fun SettingsSemanticSearchUnsupportedPreview() {
    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(
            filtersFor("com.whatsapp" to "WhatsApp", "com.slack" to "Slack")
        )
    )
    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            accessibilityServiceGranted = true,
            semanticSearchEnabled = false,
            semanticSearchStatus = SemanticSearchStatus.UNSUPPORTED,
            viewModel = viewModel
        )
    }
}

/**
 * Both model switches on, side by side, because they are separate decisions
 * and the screen has to make that readable rather than imply one switch.
 */
@PreviewTest
@Preview(name = "settings reading notifications on", widthDp = 412, heightDp = 860, showBackground = true)
@Composable
private fun SettingsDerivedIntentionsOnPreview() {
    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(
            filtersFor("com.whatsapp" to "WhatsApp", "com.slack" to "Slack")
        )
    )
    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            accessibilityServiceGranted = true,
            semanticSearchEnabled = true,
            semanticSearchStatus = SemanticSearchStatus.ON,
            derivedIntentionsEnabled = true,
            modelOnThisPhone = true,
            viewModel = viewModel
        )
    }
}

/** The ordinary case: the model is here, and the user has not asked for it. */
@PreviewTest
@Preview(name = "settings reading notifications off", widthDp = 412, heightDp = 860, showBackground = true)
@Composable
private fun SettingsDerivedIntentionsOffPreview() {
    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(
            filtersFor("com.whatsapp" to "WhatsApp", "com.slack" to "Slack")
        )
    )
    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            accessibilityServiceGranted = true,
            semanticSearchEnabled = false,
            semanticSearchStatus = SemanticSearchStatus.OFF,
            derivedIntentionsEnabled = false,
            modelOnThisPhone = true,
            viewModel = viewModel
        )
    }
}

/** All three model switches, which are three different decisions. */
@PreviewTest
@Preview(name = "settings all three model switches", widthDp = 412, heightDp = 1000, showBackground = true)
@Composable
private fun SettingsModelTriageOnPreview() {
    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(
            filtersFor("com.whatsapp" to "WhatsApp", "com.slack" to "Slack")
        )
    )
    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            accessibilityServiceGranted = true,
            semanticSearchEnabled = true,
            semanticSearchStatus = SemanticSearchStatus.ON,
            derivedIntentionsEnabled = true,
            modelOnThisPhone = true,
            modelTriageEnabled = true,
            viewModel = viewModel
        )
    }
}
