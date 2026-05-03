package tech.benedicta.will.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.benedicta.will.notification.AppFilterRepository
import tech.benedicta.will.notification.AppFilterViewModel
import tech.benedicta.will.notification.AppNotificationFilter
import tech.benedicta.will.ui.theme.WillTheme

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SettingsScreenPreview() {
    val mockRepository = object : AppFilterRepository {
        private val _filters = MutableStateFlow(
            listOf(
                AppNotificationFilter("com.whatsapp", "WhatsApp", true),
                AppNotificationFilter("com.instagram.android", "Instagram", false),
                AppNotificationFilter("com.android.vending", "Play Store", true),
                AppNotificationFilter("com.spotify.music", "Spotify", true),
                AppNotificationFilter("com.google.android.gm", "Gmail", false)
            ).sortedBy { it.appName }
        )

        override fun getAllFilters(): Flow<List<AppNotificationFilter>> = _filters.asStateFlow()
        override suspend fun setEnabled(packageName: String, enabled: Boolean) = Unit
        override suspend fun addApp(packageName: String, appName: String) = Unit
        override fun isEnabled(packageName: String): Boolean = true
    }

    val viewModel: AppFilterViewModel = viewModel(
        factory = AppFilterViewModel.Factory(mockRepository)
    )

    WillTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToPrivacy = {},
            onRerunOnboarding = {},
            viewModel = viewModel
        )
    }
}
