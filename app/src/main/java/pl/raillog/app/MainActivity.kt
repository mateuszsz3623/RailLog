package pl.raillog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pl.raillog.app.core.designsystem.RailLogTheme
import pl.raillog.app.presentation.navigation.RailLogNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as RailLogApplication).dutyRepository

        setContent {
            // Default to Griffin Dark mode (premium dark railway look)
            var isDarkTheme by remember { mutableStateOf(true) }

            RailLogTheme(darkTheme = isDarkTheme) {
                RailLogNavHost(
                    repository = repository,
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { isDarkTheme = it }
                )
            }
        }
    }
}
