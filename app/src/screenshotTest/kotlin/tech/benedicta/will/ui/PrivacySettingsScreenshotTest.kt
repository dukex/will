package tech.benedicta.will.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import tech.benedicta.will.notification.ModelDrop
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

/**
 * The condition that makes model-backed triage acceptable at all: whatever it
 * hid is listed, with a way back that also stops it happening again.
 */
@PreviewTest
@Preview(name = "privacy model drops", widthDp = 412, heightDp = 1000, showBackground = true)
@Composable
private fun PrivacySettingsModelDropsPreview() {
    WillTheme {
        PrivacySettings(
            onBack = {},
            onDeleteAllTasks = {},
            modelDrops = listOf(
                ModelDrop("com.shop", "Experimente o premium por 7 dias", at = 3L),
                ModelDrop("com.cloud", "Backup concluído", at = 2L),
                ModelDrop("com.player", "Baixando episódio 4", at = 1L)
            )
        )
    }
}
