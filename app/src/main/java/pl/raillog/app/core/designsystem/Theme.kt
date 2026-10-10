package pl.raillog.app.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GriffinRed,
    onPrimary = Color.White,
    primaryContainer = GriffinRedDark,
    onPrimaryContainer = Color.White,
    secondary = PantographAmber,
    onSecondary = Color.Black,
    secondaryContainer = CarbonDarkSurfaceElevated,
    onSecondaryContainer = PantographAmber,
    tertiary = SignalGreen,
    onTertiary = Color.White,
    background = CarbonDarkBackground,
    onBackground = CarbonDarkTextPrimary,
    surface = CarbonDarkSurface,
    onSurface = CarbonDarkTextPrimary,
    surfaceVariant = CarbonDarkSurfaceElevated,
    onSurfaceVariant = CarbonDarkTextSecondary,
    outline = CarbonDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GriffinRedDark,
    onPrimary = Color.White,
    primaryContainer = GriffinRedLight,
    onPrimaryContainer = Color.White,
    secondary = PantographAmber,
    onSecondary = Color.Black,
    secondaryContainer = TechnicalLightSurfaceElevated,
    onSecondaryContainer = TechnicalLightTextPrimary,
    tertiary = SignalGreen,
    onTertiary = Color.White,
    background = TechnicalLightBackground,
    onBackground = TechnicalLightTextPrimary,
    surface = TechnicalLightSurface,
    onSurface = TechnicalLightTextPrimary,
    surfaceVariant = TechnicalLightSurfaceElevated,
    onSurfaceVariant = TechnicalLightTextSecondary,
    outline = TechnicalLightBorder
)

@Composable
fun RailLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
