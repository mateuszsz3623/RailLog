package pl.raillog.app.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.core.designsystem.GriffinRed
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.SignalGreen
import pl.raillog.app.core.designsystem.component.FormSectionHeader
import pl.raillog.app.core.designsystem.component.RailLogHeroButton
import pl.raillog.app.core.designsystem.component.StatusBadge
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyLeg
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyDetailScreen(
    viewModel: DutyDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val duty by viewModel.duty.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paszport Służby",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń służbę",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (duty != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Usuń", fontWeight = FontWeight.SemiBold)
                        }

                        RailLogHeroButton(
                            text = "EDYTUJ SŁUŻBĘ",
                            onClick = { onNavigateToEdit(duty!!.id) },
                            icon = Icons.Default.Edit,
                            modifier = Modifier.weight(2f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (duty == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Ładowanie danych służby...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val d = duty!!
            val polishDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl"))
            val formattedDate = d.date.format(polishDateFormatter).replaceFirstChar { it.uppercase() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Header Date & Overtime Badge
                    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DATA SŁUŻBY",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (d.overtimeMinutes > 0 || d.isWholeDutyOvertime) {
                                StatusBadge(
                                    text = d.displayOvertime,
                                    color = PantographAmber
                                )
                            }
                        }
                    }
                }

                // Shift Working Hours Block (if start/end time provided)
                if (!d.startTime.isNullOrBlank() || !d.endTime.isNullOrBlank() || d.displayDuration != null) {
                    item {
                        TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "CZAS PRACY",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = GriffinRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val timeRange = if (!d.startTime.isNullOrBlank() && !d.endTime.isNullOrBlank()) {
                                            "${d.startTime} — ${d.endTime}"
                                        } else {
                                            d.startTime ?: d.endTime ?: ""
                                        }
                                        Text(
                                            text = timeRange,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (d.displayDuration != null) {
                                        StatusBadge(
                                            text = d.displayDuration!!,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Ordered Route Legs with Visual Timeline
                item {
                    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ODCINKI TRASY (${d.legs.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = d.displayRoute,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            if (d.legs.isEmpty()) {
                                Text(
                                    text = "Brak podanej trasy",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    d.legs.forEachIndexed { index, leg ->
                                        LegTimelineItem(
                                            index = index,
                                            leg = leg,
                                            fallbackVehicle = d.displayVehicle,
                                            isLast = index == d.legs.size - 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Vehicles Summary Card (Equal weight, clean display)
                if (d.allVehicles.isNotEmpty()) {
                    item {
                        TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (d.allVehicles.size > 1) "PODSUMOWANIE POJAZDÓW" else "POJAZD TRAKCYJNY",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Train,
                                        contentDescription = null,
                                        tint = GriffinRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = d.displayAllVehicles,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes Block
                if (!d.notes.isNullOrBlank()) {
                    item {
                        TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "UWAGI ZE SZLAKU",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = d.notes!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Additional Private Notes Block
                if (!d.additionalNotes.isNullOrBlank()) {
                    item {
                        TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "DODATKOWE PRYWATNE NOTATKI",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = d.additionalNotes!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Confirmation Dialog before deletion
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text(text = "Usunąć tę służbę?", fontWeight = FontWeight.Bold) },
            text = { Text("Wpis zostanie trwale usunięty z Twojego dziennika. Operacji nie można cofnąć.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteDuty(onDeleted = onNavigateBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("USUŃ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("ANULUJ")
                }
            }
        )
    }
}

@Composable
private fun LegTimelineItem(
    index: Int,
    leg: DutyLeg,
    fallbackVehicle: String,
    isLast: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Leg Index and Train Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(text = "ODCINEK ${index + 1}", color = GriffinRed)

                if (leg.trainNumber.isNotBlank()) {
                    StatusBadge(
                        text = "Poc. ${leg.trainNumber}",
                        color = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Station connection
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = GriffinRed,
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = leg.routeText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Locomotive assigned to this specific leg
            val legVehicle = if (leg.displayVehicle != "—") leg.displayVehicle else fallbackVehicle
            if (legVehicle != "—") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRailway,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pojazd: $legVehicle",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
