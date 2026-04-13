package tech.benedicta.will

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import tech.benedicta.will.ui.theme.WillTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WillTheme {
                Greeting("Welcome to Will - Your Task-Centric Launcher")
            }
        }
    }
}

@Composable
fun Greeting(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    )
y
}
