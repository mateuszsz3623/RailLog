package pl.raillog.app.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.PantographAmberDark
import pl.raillog.app.core.designsystem.component.BentoCard
import pl.raillog.app.core.designsystem.component.RailLogHeroButton
import pl.raillog.app.core.designsystem.component.StatusBadge
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.domain.model.DutyLeg
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyDetailScreen(
    viewModel: DutyDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onOpenTrainStatus: ((String, java.time.LocalDate) -> Unit)?
) {
    val duty by viewModel.duty.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val amberAccent = if (isDark) PantographAmber else PantographAmberDark

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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
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
                    tonalElevation = 6.dp
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
            val isOvertime = d.overtimeMinutes > 0 || d.isWholeDutyOvertime
            val trainStatusAllowed = d.date == java.time.LocalDate.now() || d.date == java.time.LocalDate.now().minusDays(1)
            val timeRange = if (!d.startTime.isNullOrBlank() && !d.endTime.isNullOrBlank()) {
                "${d.startTime} — ${d.endTime}"
            } else {
                d.startTime ?: d.endTime
            }
            val (durationValue, durationUnit) =
                cockpitHoursMinutes(d.calculatedDurationMinutes ?: d.dutyDurationMinutes)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Kokpit czasu: data + zakres godzin + czas służby w jednym kaflu
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    BentoCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DATA SŁUŻBY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                d.displayDutyCode?.let { code ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = code,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (!timeRange.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = timeRange,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                if (isOvertime) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    StatusBadge(
                                        text = d.displayOvertime,
                                        color = PantographAmber
                                    )
                                }
                            }

                            // Prawa kolumna: czas służby, wyrównany do prawej i dołem
                            Column(horizontalAlignment = Alignment.End) {
                                CockpitNumberMedium(
                                    value = durationValue,
                                    unit = durationUnit,
                                    accentColor = if (isOvertime) {
                                        amberAccent
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = "CZAS SŁUŻBY",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Ordered Route Legs on Vertical Timeline
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
                                    style = MaterialTheme.typography.labelSmall,
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
                                Column {
                                    d.legs.forEachIndexed { index, leg ->
                                        LegTimelineItem(
                                            index = index,
                                            leg = leg,
                                            fallbackVehicle = d.displayVehicle,
                                            isLast = index == d.legs.size - 1,
                                            onTrainStatus = if (onOpenTrainStatus != null && trainStatusAllowed && leg.trainNumber.any { it.isDigit() }) {
                                                { onOpenTrainStatus(leg.trainNumber.filter { it.isDigit() }, d.date) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Vehicles Summary Card
                if (d.allVehicles.isNotEmpty()) {
                    item {
                        TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (d.allVehicles.size > 1) "PODSUMOWANIE POJAZDÓW" else "POJAZD TRAKCYJNY",
                                    style = MaterialTheme.typography.labelSmall,
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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = d.displayAllVehicles,
                                        style = MaterialTheme.typography.titleMedium,
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
                                    style = MaterialTheme.typography.labelSmall,
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
                                    style = MaterialTheme.typography.labelSmall,
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

/**
 * Pionowa oś czasu odcinka: po lewej kropka primary + linia do następnego odcinka,
 * po prawej treść (badges, trasa, pojazd). Bez tła — strukturę zapewnia oś.
 */
@Composable
private fun LegTimelineItem(
    index: Int,
    leg: DutyLeg,
    fallbackVehicle: String,
    isLast: Boolean,
    onTrainStatus: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Kolumna osi: kropka + linia łącząca z następnym odcinkiem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Treść odcinka
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leg.trainNumber.isNotBlank()) {
                    StatusBadge(
                        text = "Poc. ${leg.trainNumber}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusBadge(
                    text = "ODCINEK ${index + 1}",
                    color = MaterialTheme.colorScheme.primary
                )
                if (onTrainStatus != null) {
                    IconButton(onClick = onTrainStatus, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Podgląd pociągu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = leg.routeText,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Locomotive assigned to this specific leg
            val legVehicle = if (leg.displayVehicle != "—") leg.displayVehicle else fallbackVehicle
            if (legVehicle != "—") {
                Spacer(modifier = Modifier.height(6.dp))
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
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Mini-wariant [pl.raillog.app.core.designsystem.component.CockpitNumber] na displayMedium
 * (34 sp, mono ze skali typografii) — do ciaśniejszych kafli bento.
 */
@Composable
private fun CockpitNumberMedium(
    value: String,
    unit: String? = null,
    label: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = accentColor,
                modifier = Modifier.alignByBaseline()
            )
            if (unit != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Minuty → para (wartość, jednostka) dla liczb kokpitowych: „8h"+"30m", „8h", „45"+"min", „0"+"min". */
private fun cockpitHoursMinutes(minutes: Int?): Pair<String, String?> = when {
    minutes == null -> "—" to null
    minutes <= 0 -> "0" to "min"
    minutes / 60 > 0 && minutes % 60 > 0 -> "${minutes / 60}h" to "${minutes % 60}m"
    minutes / 60 > 0 -> "${minutes / 60}h" to null
    else -> "$minutes" to "min"
}
