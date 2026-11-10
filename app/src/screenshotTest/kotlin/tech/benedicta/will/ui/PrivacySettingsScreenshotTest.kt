package tech.benedicta.will.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import tech.benedicta.will.notification.SilencedNotification
import tech.benedicta.will.ui.theme.WillTheme

/** What Will stopped showing, and the way back. */
@PreviewTest
@Preview(name = "privacy with silenced notifications", widthDp = 412, heightDp = 900, showBackground = true)
@Composable
private fun PrivacySettingsSilencedPreview() {
    WillTheme {
        PrivacySettings(
            onBack = {},
            onDeleteAllTasks = {},
            silencedNotifications = listOf(
                SilencedNotification("com.shop", "Veja as novidades da semana", 4),
                SilencedNotification("com.streaming", "Experimente o premium", 3)
            )
        )
    }
}
