@file:OptIn(ExperimentalTextApi::class)

package pl.raillog.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pl.raillog.app.R

/** Inter — font tekstowy (UI, opisy, przyciski). */
val InterFamily = FontFamily(
    Font(
        R.font.inter_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

/** JetBrains Mono — liczby i etykiety techniczne (styl „wyświetlacza maszyny"). */
val MonoFamily = FontFamily(
    Font(
        R.font.jetbrains_mono_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.jetbrains_mono_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.jetbrains_mono_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.jetbrains_mono_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

/** Skala typografii „KABINA": Inter dla tekstu, JetBrains Mono dla liczb. */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
