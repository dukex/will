package tech.benedicta.will

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.benedicta.will.launcher.LauncherViewModel
import tech.benedicta.will.launcher.SystemAppRepository
import tech.benedicta.will.ui.AppGrid
import tech.benedicta.will.ui.theme.WillTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SystemAppRepository(packageManager)
        setContent {
            WillTheme {
                val vm: LauncherViewModel = viewModel(
                    factory = LauncherViewModel.factory(repository)
                )
                val apps by vm.apps.collectAsState()
                AppGrid(apps = apps)
            }
        }
    }
}
