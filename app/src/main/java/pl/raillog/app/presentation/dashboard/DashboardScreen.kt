package pl.raillog.app.presentation.dashboard

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.raillog.app.R
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.PantographAmberDark
import pl.raillog.app.core.designsystem.SignalGreen
import pl.raillog.app.core.designsystem.component.BentoCard
import pl.raillog.app.core.designsystem.component.CockpitNumber
import pl.raillog.app.core.designsystem.component.FormSectionHeader
import pl.raillog.app.core.designsystem.component.QuickChip
import pl.raillog.app.core.designsystem.component.RailLogHeroButton
import pl.raillog.app.core.designsystem.component.StatusBadge
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.PlannedDuty
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToNewDuty: (String?) -> Unit,
    onNavigateToDutyDetail: (Long) -> Unit,
    onSaveFromPlan: (Long) -> Unit,
    onCheckTrain: ((String) -> Unit)?
) {
    val nextPlanned by viewModel.nextPlanned.collectAsState()
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
        // Sprawdź pociąg (tylko gdy w buildzie jest klucz API PLK)
        if (onCheckTrain != null) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                CheckTrainRow(onCheckTrain = onCheckTrain)
            }
        }

        // Kafel „DZIŚ" — identyfikacja + status dnia
        item {
            Spacer(modifier = Modifier.height(4.dp))
            TodayBentoCard(
                formattedToday = formattedToday,
                hasDutyToday = latestDuty?.date == today
            )
        }

        // Najbliższa służba z planu (tylko gdy zaimportowano plan)
        nextPlanned?.let { planned ->
            item {
                PlannedDutyCard(planned = planned, onSaveFromPlan = { onSaveFromPlan(planned.id) })
            }
        }

        // Bento KPI — asymetryczny wiersz liczb kokpitowych
        item {
            DashboardKpiRow(
                currentMonthDuties = statistics.currentMonthDuties,
                totalDuties = statistics.totalDuties,
                currentMonthOvertimeMinutes = statistics.currentMonthOvertimeMinutes,
                monthFraction = today.dayOfMonth.toFloat() / today.lengthOfMonth()
            )
        }

        // Primary Hero CTA
        item {
            RailLogHeroButton(
                text = "NOWA SŁUŻBA",
                onClick = { onNavigateToNewDuty(null) },
                icon = Icons.Default.Add
            )
        }

        // Quick Vehicle Shortcuts
        if (recentVehicles.isNotEmpty()) {
            item {
                Column {
                    FormSectionHeader(title = "OSTATNIO PROWADZONE POJAZDY")
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
            FormSectionHeader(title = "OSTATNIA ZAREJESTROWANA SŁUŻBA")
            if (latestDuty != null) {
                LatestDutyCard(
                    duty = latestDuty!!,
                    onClick = { onNavigateToDutyDetail(latestDuty!!.id) }
                )
            } else {
                DashboardEmptyState(onStartClick = { onNavigateToNewDuty(null) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TodayBentoCard(
    formattedToday: String,
    hasDutyToday: Boolean
) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_taktrail_logo),
                contentDescription = "Logo RailLog",
                modifier = Modifier.size(48.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RAILLOG",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "DZIENNIK SŁUŻB MASZYNISTY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedToday,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (hasDutyToday) {
                    StatusBadge(text = "SŁUŻBA DZISIAJ", color = SignalGreen)
                } else {
                    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
                    StatusBadge(
                        text = "DZIEŃ BEZ SŁUŻBY",
                        color = neutral,
                        backgroundColor = neutral.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardKpiRow(
    currentMonthDuties: Int,
    totalDuties: Int,
    currentMonthOvertimeMinutes: Int,
    monthFraction: Float
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val amberAccent = if (isDark) PantographAmber else PantographAmberDark
    val (overtimeValue, overtimeUnit) = cockpitHoursMinutes(currentMonthOvertimeMinutes)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Kafel A — służby w miesiącu + pasek tempa miesiąca
        BentoCard(modifier = Modifier.weight(1.3f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CockpitNumber(
                    value = "$currentMonthDuties",
                    unit = if (currentMonthDuties == 1) "służba" else "służb",
                    label = "W TYM MIESIĄCU • ogółem $totalDuties",
                    accentColor = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(14.dp))
                MonthPaceBar(monthFraction = monthFraction)
            }
        }

        // Kafel B — nadgodziny miesiąca
        BentoCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                CockpitNumberMedium(
                    value = overtimeValue,
                    unit = overtimeUnit,
                    label = "NADGODZINY M-CA",
                    accentColor = if (currentMonthOvertimeMinutes > 0) {
                        amberAccent
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/** Cienki pasek „tempa miesiąca": wypełnienie = dzień miesiąca / liczba dni miesiąca. */
@Composable
private fun MonthPaceBar(monthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(monthFraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

/**
 * Mini-wariant [CockpitNumber] na displayMedium (34 sp, mono ze skali typografii) —
 * do ciaśniejszych kafli bento (nadgodziny, czas ostatniej służby).
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

@Composable
private fun LatestDutyCard(
    duty: Duty,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val amberAccent = if (isDark) PantographAmber else PantographAmberDark
    val polishDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
    val formattedDate = duty.date.format(polishDateFormatter)

    val hasOvertime = duty.overtimeMinutes > 0 || duty.isWholeDutyOvertime
    val durationMinutes = duty.calculatedDurationMinutes
        ?: duty.dutyDurationMinutes.takeIf { it > 0 }
    val (durationValue, durationUnit) = cockpitHoursMinutes(durationMinutes)

    TechnicalCard(
        accentColor = if (hasOvertime) PantographAmber else null,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lewa kolumna — treść służby
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listOfNotNull(formattedDate, duty.displayDutyCode).joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = duty.displayRoute,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (duty.displayVehiclesSummary != "—") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Train,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (duty.vehicleTypes.size > 1) "Pojazdy" else "Pojazd"}: ${duty.displayVehiclesSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (duty.displayTrains != "—") {
                        Text(
                            text = "Pociąg: ${duty.displayTrains}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (!duty.notes.isNullOrBlank()) {
                        Text(
                            text = "\"${duty.notes}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                // Prawa kolumna — wyeksponowany czas trwania
                Column(horizontalAlignment = Alignment.End) {
                    CockpitNumberMedium(
                        value = durationValue,
                        unit = durationUnit,
                        accentColor = if (hasOvertime) amberAccent else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "CZAS SŁUŻBY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!duty.startTime.isNullOrBlank() && !duty.endTime.isNullOrBlank()) {
                        Text(
                            text = "${duty.startTime} - ${duty.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paszport służby",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardEmptyState(onStartClick: () -> Unit) {
    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_taktrail_logo),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Brak zarejestrowanych służb",
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
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }
    }
}

@Composable
private fun PlannedDutyCard(
    planned: PlannedDuty,
    onSaveFromPlan: () -> Unit
) {
    val today = LocalDate.now()
    val dayLabel = when (planned.date) {
        today -> "Dzisiaj"
        today.plusDays(1) -> "Jutro"
        else -> planned.date.format(DateTimeFormatter.ofPattern("EEEE dd.MM", Locale("pl")))
            .replaceFirstChar { it.uppercase() }
    }
    Column {
        FormSectionHeader(title = "NAJBLIŻSZA SŁUŻBA Z PLANU")
        TechnicalCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "$dayLabel · ${planned.displayName} · ${planned.timeRange()} (${planned.displayDuration})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                RailLogHeroButton(
                    text = "ZAPISZ Z PLANU",
                    onClick = onSaveFromPlan
                )
            }
        }
    }
}

@Composable
private fun CheckTrainRow(onCheckTrain: (String) -> Unit) {
    var number by rememberSaveable { mutableStateOf("") }
    val digits = number.filter { it.isDigit() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = number,
            onValueChange = { number = it.filter { c -> c.isDigit() }.take(6) },
            label = { Text("Sprawdź pociąg (nr)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { if (digits.isNotEmpty()) onCheckTrain(digits) })
        )
        IconButton(onClick = { onCheckTrain(digits) }, enabled = digits.isNotEmpty()) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Sprawdź pociąg")
        }
    }
}
