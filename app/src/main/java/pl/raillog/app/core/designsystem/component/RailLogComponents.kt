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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.core.designsystem.GriffinRed

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
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
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
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        label = "chipBorder"
    )
    val textColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
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
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
    val cardShape = RoundedCornerShape(14.dp)
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    val cardBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

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
            modifier = modifier
        ) {
            CardContentWithAccent(accentColor = accentColor, content = content)
        }
    } else {
        Card(
            shape = cardShape,
            colors = cardColors,
            border = cardBorder,
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
                            size = Size(3.dp.toPx(), size.height)
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
    backgroundColor: Color = color.copy(alpha = 0.12f),
    borderColor: Color = color.copy(alpha = 0.4f)
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun FormSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 6.dp)
    )
}
