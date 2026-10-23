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
    primaryContainer = Color(0xFF2C1518),
    onPrimaryContainer = Color(0xFFFFB4BA),
    secondary = PantographAmber,
    onSecondary = Color(0xFF1E1400),
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
    outline = CarbonDarkBorder,
    outlineVariant = CarbonDarkBorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = GriffinRedDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBED),
    onPrimaryContainer = Color(0xFF900018),
    secondary = PantographAmberDark,
    onSecondary = Color.White,
    secondaryContainer = TechnicalLightSurfaceElevated,
    onSecondaryContainer = TechnicalLightTextPrimary,
    tertiary = SignalGreenDark,
    onTertiary = Color.White,
    background = TechnicalLightBackground,
    onBackground = TechnicalLightTextPrimary,
    surface = TechnicalLightSurface,
    onSurface = TechnicalLightTextPrimary,
    surfaceVariant = TechnicalLightSurfaceElevated,
    onSurfaceVariant = TechnicalLightTextSecondary,
    outline = TechnicalLightBorder,
    outlineVariant = TechnicalLightBorderSubtle
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
            window.navigationBarColor = colorScheme.surface.toArgb()
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

@Composable
fun RailLogTheme(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    RailLogTheme(darkTheme = darkTheme, content = content)
}
