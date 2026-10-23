package pl.raillog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pl.raillog.app.core.designsystem.RailLogTheme
import pl.raillog.app.core.designsystem.ThemePreferences
import pl.raillog.app.presentation.navigation.RailLogNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as RailLogApplication).dutyRepository
        val themePreferences = ThemePreferences(this)

        setContent {
            val themeMode by themePreferences.themeMode.collectAsState()

            RailLogTheme(themeMode = themeMode) {
                RailLogNavHost(
                    repository = repository,
                    themeMode = themeMode,
                    onThemeModeChanged = { newMode ->
                        themePreferences.setThemeMode(newMode)
                    }
                )
            }
        }
    }
}
