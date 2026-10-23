package pl.raillog.app.core.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.core.designsystem.GriffinRed
import pl.raillog.app.core.designsystem.LightGriffinRedBg
import pl.raillog.app.core.designsystem.LightGriffinRedBorder
import pl.raillog.app.core.designsystem.LightGriffinRedText
import pl.raillog.app.core.designsystem.LightPantographAmberBg
import pl.raillog.app.core.designsystem.LightPantographAmberBorder
import pl.raillog.app.core.designsystem.LightPantographAmberText
import pl.raillog.app.core.designsystem.LightSignalGreenBg
import pl.raillog.app.core.designsystem.LightSignalGreenBorder
import pl.raillog.app.core.designsystem.LightSignalGreenText
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.SignalGreen

@Composable
fun RailLogHeroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = GriffinRed,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) {
    val view = LocalView.current
    // Strip leading '+' if icon is already provided, avoiding double plus '+  + NOWA SŁUŻBA'
    val cleanedText = if (icon != null && text.startsWith("+")) {
        text.removePrefix("+").trim()
    } else {
        text
    }

    Button(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = cleanedText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun RailLogSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline
) {
    val view = LocalView.current
    OutlinedButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColor)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun QuickChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    leadingIcon: ImageVector? = null,
    accentColor: Color = GriffinRed
) {
    val view = LocalView.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val backgroundColor = if (isSelected) {
        if (isDark) accentColor.copy(alpha = 0.15f) else LightGriffinRedBg
    } else {
        if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    }

    val borderColor = if (isSelected) {
        if (isDark) accentColor else LightGriffinRedBorder
    } else {
        if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFCBD5E1)
    }

    val textColor = if (isSelected) {
        if (isDark) accentColor else LightGriffinRedText
    } else {
        if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF1E293B)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDark || isSelected) 0.dp else 1.dp,
        modifier = modifier
            .height(36.dp)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                color = textColor
            )
        }
    }
}

@Composable
fun TechnicalCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardShape = RoundedCornerShape(14.dp)
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    val cardBorder = BorderStroke(
        width = 1.dp,
        color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.55f) else Color(0xFFCBD5E1)
    )
    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = if (isDark) 0.dp else 1.5.dp
    )

    if (onClick != null) {
        val view = LocalView.current
        Card(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
            shape = cardShape,
            colors = cardColors,
            border = cardBorder,
            elevation = cardElevation,
            modifier = modifier
        ) {
            CardContentWithAccent(accentColor = accentColor, content = content)
        }
    } else {
        Card(
            shape = cardShape,
            colors = cardColors,
            border = cardBorder,
            elevation = cardElevation,
            modifier = modifier
        ) {
            CardContentWithAccent(accentColor = accentColor, content = content)
        }
    }
}

@Composable
private fun CardContentWithAccent(
    accentColor: Color?,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (accentColor != null) {
                    Modifier.drawBehind {
                        drawRect(
                            color = accentColor,
                            topLeft = Offset.Zero,
                            size = Size(3.5.dp.toPx(), size.height)
                        )
                    }
                } else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = GriffinRed,
    backgroundColor: Color? = null,
    borderColor: Color? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Intelligent accessibility contrast resolution for Light vs Dark theme
    val (resolvedText, resolvedBg, resolvedBorder) = if (isDark) {
        Triple(
            color,
            backgroundColor ?: color.copy(alpha = 0.15f),
            borderColor ?: color.copy(alpha = 0.45f)
        )
    } else {
        when {
            // Amber / Warning badges
            color == PantographAmber || color.red > 0.8f && color.green > 0.5f && color.blue < 0.2f -> Triple(
                LightPantographAmberText,
                backgroundColor ?: LightPantographAmberBg,
                borderColor ?: LightPantographAmberBorder
            )
            // Red / Danger badges
            color == GriffinRed || color.red > 0.7f && color.green < 0.4f -> Triple(
                LightGriffinRedText,
                backgroundColor ?: LightGriffinRedBg,
                borderColor ?: LightGriffinRedBorder
            )
            // Green / Success badges
            color == SignalGreen || color.green > 0.6f && color.red < 0.2f -> Triple(
                LightSignalGreenText,
                backgroundColor ?: LightSignalGreenBg,
                borderColor ?: LightSignalGreenBorder
            )
            else -> Triple(
                color,
                backgroundColor ?: color.copy(alpha = 0.12f),
                borderColor ?: color.copy(alpha = 0.5f)
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = resolvedBg,
        border = BorderStroke(1.dp, resolvedBorder),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = resolvedText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun FormSectionHeader(title: String, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        ),
        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155),
        modifier = modifier.padding(vertical = 4.dp)
    )
}
