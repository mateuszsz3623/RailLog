package pl.raillog.app.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.R
import pl.raillog.app.core.designsystem.GriffinRed
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.component.QuickChip
import pl.raillog.app.core.designsystem.component.RailLogHeroButton
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.domain.model.Duty
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToNewDuty: (String?) -> Unit,
    onNavigateToDutyDetail: (Long) -> Unit
) {
    val latestDuty by viewModel.latestDuty.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val recentVehicles by viewModel.recentVehicles.collectAsState()

    val today = LocalDate.now()
    val polishDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl"))
    val formattedToday = today.format(polishDateFormatter).replaceFirstChar { it.uppercase() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            TechnicalDashboardHeader(formattedToday = formattedToday)
        }

        // Monthly KPI Summary Widgets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiWidget(
                    title = "Służby w tym m-cu",
                    value = "${statistics.currentMonthDuties}",
                    icon = Icons.Default.DirectionsRailway,
                    accentColor = GriffinRed,
                    modifier = Modifier.weight(1f)
                )
                val hours = statistics.currentMonthOvertimeMinutes / 60
                val mins = statistics.currentMonthOvertimeMinutes % 60
                val overtimeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                KpiWidget(
                    title = "Nadgodziny",
                    value = if (statistics.currentMonthOvertimeMinutes > 0) overtimeStr else "0 min",
                    icon = Icons.Default.Schedule,
                    accentColor = PantographAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Primary Hero CTA Button
        item {
            RailLogHeroButton(
                text = "+ NOWA SŁUŻBA",
                onClick = { onNavigateToNewDuty(null) },
                icon = Icons.Default.Add
            )
        }

        // Quick Vehicle Shortcuts
        if (recentVehicles.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "OSTATNIO PROWADZONE POJAZDY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentVehicles) { vehicle ->
                            QuickChip(
                                text = vehicle,
                                leadingIcon = Icons.Default.Train,
                                onClick = { onNavigateToNewDuty(vehicle) }
                            )
                        }
                    }
                }
            }
        }

        // Latest Duty Card or Empty State
        item {
            Text(
                text = "OSTATNIA ZAREJESTROWANA SŁUŻBA",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (latestDuty != null) {
                LastDutyCard(
                    duty = latestDuty!!,
                    onClick = { onNavigateToDutyDetail(latestDuty!!.id) }
                )
            } else {
                EmptyDutyCard(onStartClick = { onNavigateToNewDuty(null) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TechnicalDashboardHeader(formattedToday: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GriffinRed,
                        modifier = Modifier.size(width = 4.dp, height = 20.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RAILLOG",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedToday,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Small friendly diesel locomotive illustration: subtle, decorative, premium
            Surface(
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(width = 62.dp, height = 54.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_diesel_header),
                    contentDescription = "Lokomotywa RailLog",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun KpiWidget(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LastDutyCard(
    duty: Duty,
    onClick: () -> Unit
) {
    val polishDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
    val formattedDate = duty.date.format(polishDateFormatter)

    TechnicalCard(
        accentColor = if (duty.overtimeMinutes > 0 || duty.isWholeDutyOvertime) GriffinRed else null,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Date and Overtime badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (duty.overtimeMinutes > 0 || duty.isWholeDutyOvertime) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GriffinRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GriffinRed.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = duty.displayOvertime,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GriffinRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Route
            Text(
                text = duty.displayRoute,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Vehicles row: all vehicles equal weight
            if (duty.displayVehiclesSummary != "—") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (duty.vehicleTypes.size > 1) "Pojazdy: " else "Pojazd: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = duty.displayVehiclesSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Train & Duration
            val info = mutableListOf<String>()
            if (duty.displayTrains != "—") {
                info.add("Pociąg: ${duty.displayTrains}")
            }
            if (duty.displayDuration != null) {
                val timeSpan = if (!duty.startTime.isNullOrBlank() && !duty.endTime.isNullOrBlank()) " (${duty.startTime} - ${duty.endTime})" else ""
                info.add("Czas: ${duty.displayDuration}$timeSpan")
            }
            if (info.isNotEmpty()) {
                Text(
                    text = info.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!duty.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${duty.notes}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zobacz szczegóły",
                    style = MaterialTheme.typography.labelMedium,
                    color = GriffinRed
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = GriffinRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyDutyCard(onStartClick: () -> Unit) {
    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_locomotive),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(width = 120.dp, height = 50.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Brak zapisanych służb",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Twój dziennik jest gotowy do drogi. Zapisz swoją pierwszą służbę.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            RailLogHeroButton(
                text = "ROZPOCZNIJ DZIENNIK",
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
