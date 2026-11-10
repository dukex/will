package tech.benedicta.will.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import tech.benedicta.will.launcher.AppCategory
import tech.benedicta.will.launcher.LauncherApp
import tech.benedicta.will.task.Task
import tech.benedicta.will.ui.theme.WillTheme

/** One search box over the tasks you still have and the apps you can open. */
@PreviewTest
@Preview(name = "drawer search over tasks and apps", widthDp = 412, heightDp = 560, showBackground = true)
@Composable
private fun AppGridWithTaskResultsPreview() {
    WillTheme {
        AppGrid(
            apps = listOf(
                LauncherApp("com.whatsapp", "WhatsApp", AppCategory.SOCIAL),
                LauncherApp("com.itau", "Itaú")
            ),
            query = "contrato",
            onQueryChange = {},
            matchingTasks = listOf(
                Task(
                    id = 1,
                    title = "revisar o contrato de aluguel",
                    text = "",
                    sourcePackage = "com.google.android.apps.docs",
                    createdAt = 0L
                ),
                Task(
                    id = 2,
                    title = "mandar o contrato para Ana",
                    text = "",
                    sourcePackage = "com.whatsapp",
                    createdAt = 0L
                )
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    }
}
