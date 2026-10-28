package pl.raillog.app.presentation.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.component.FormSectionHeader
import pl.raillog.app.core.designsystem.component.StatusBadge
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.domain.model.Duty
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDutyDetail: (Long) -> Unit
) {
    val allDuties by viewModel.duties.collectAsState()
    val currentYearMonth by viewModel.yearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl"))
    val displayMonth = currentYearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() }

    val dutiesOnSelectedDate = allDuties.filter { it.date == selectedDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kalendarz Służb",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Month navigation header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Poprzedni miesiąc")
                }
                Text(
                    text = displayMonth,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Następny miesiąc")
                }
            }

            // Days of week header
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month Grid
            MonthGrid(
                yearMonth = currentYearMonth,
                selectedDate = selectedDate,
                duties = allDuties,
                onDateSelected = viewModel::selectDate
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Duties for selected date
            val selectedDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl"))
            val formattedSelected = selectedDate.format(selectedDateFormatter).replaceFirstChar { it.uppercase() }

            FormSectionHeader(title = formattedSelected.uppercase())

            if (dutiesOnSelectedDate.isEmpty()) {
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Brak zarejestrowanej służby w tym dniu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dutiesOnSelectedDate, key = { it.id }) { duty ->
                        TechnicalCard(
                            accentColor = if (duty.overtimeMinutes > 0 || duty.isWholeDutyOvertime) PantographAmber else null,
                            onClick = { onNavigateToDutyDetail(duty.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = duty.displayRoute,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (duty.overtimeMinutes > 0 || duty.isWholeDutyOvertime) {
                                        StatusBadge(text = duty.displayOvertime, color = PantographAmber)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (duty.displayVehicle != "—") {
                                        Text(
                                            text = "Pojazd: ${duty.displayVehicle}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (duty.displayTrains != "—") {
                                        Text(
                                            text = " • Poc. ${duty.displayTrains}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    duties: List<Duty>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1..7 (Pn..Nd)

    val totalCells = 42
    val dates = (1..totalCells).map { cellIndex ->
        val dayNumber = cellIndex - (startDayOfWeek - 1)
        if (dayNumber in 1..daysInMonth) yearMonth.atDay(dayNumber) else null
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        dates.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val isSelected = date == selectedDate
                            val dayDuties = duties.filter { it.date == date }
                            val hasDuty = dayDuties.isNotEmpty()
                            val hasOvertime = dayDuties.any { it.overtimeMinutes > 0 || it.isWholeDutyOvertime }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { onDateSelected(date) }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${date.dayOfMonth}",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected || hasDuty) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasDuty) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (hasOvertime) PantographAmber else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(5.dp)
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
