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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.core.designsystem.GriffinRed
import pl.raillog.app.core.designsystem.PantographAmber

@Composable
fun RailLogHeroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = GriffinRed,
    contentColor: Color = Color.White
) {
    val view = LocalView.current
    Button(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun QuickChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val view = LocalView.current
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) GriffinRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GriffinRed else MaterialTheme.colorScheme.outline,
        label = "chipBorder"
    )
    val textColor = if (isSelected) GriffinRed else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(38.dp)
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
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
    val cardShape = RoundedCornerShape(12.dp)
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    val cardBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

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
fun OvertimeStepper(
    minutes: Int,
    onMinutesChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = minutes / 60
    val remMinutes = minutes % 60
    val view = LocalView.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours adjuster
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (hours > 0) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onMinutesChanged((hours - 1) * 60 + remMinutes)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Mniej godzin")
                }
                Text(
                    text = "${hours} h",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onMinutesChanged((hours + 1) * 60 + remMinutes)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Więcej godzin")
                }
            }

            // Minutes adjuster
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        val newTotal = (minutes - 15).coerceAtLeast(0)
                        onMinutesChanged(newTotal)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Mniej minut")
                }
                Text(
                    text = "${remMinutes} min",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onMinutesChanged(minutes + 15)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Więcej minut")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick add chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickChip(text = "+15m", onClick = { onMinutesChanged(minutes + 15) })
            QuickChip(text = "+30m", onClick = { onMinutesChanged(minutes + 30) })
            QuickChip(text = "+1h", onClick = { onMinutesChanged(minutes + 60) })
            QuickChip(text = "+2h", onClick = { onMinutesChanged(minutes + 120) })
            if (minutes > 0) {
                QuickChip(
                    text = "Wyzeruj",
                    onClick = { onMinutesChanged(0) }
                )
            }
        }
    }
}
