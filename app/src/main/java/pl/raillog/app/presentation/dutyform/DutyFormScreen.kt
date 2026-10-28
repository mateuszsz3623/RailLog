package pl.raillog.app.presentation.dutyform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.component.FormSectionHeader
import pl.raillog.app.core.designsystem.component.QuickChip
import pl.raillog.app.core.designsystem.component.RailLogHeroButton
import pl.raillog.app.core.designsystem.component.RailLogSecondaryButton
import pl.raillog.app.core.designsystem.component.StatusBadge
import pl.raillog.app.core.designsystem.component.TechnicalCard
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyFormScreen(
    viewModel: DutyFormViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val frequentSeries by viewModel.frequentSeries.collectAsState()
    val recentVehicles by viewModel.recentVehicles.collectAsState()
    val recentRoutes by viewModel.recentRoutes.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var notesExpanded by remember { mutableStateOf(state.notes.isNotBlank() || state.additionalNotes.isNotBlank()) }

    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collect {
            onSaveSuccess()
        }
    }

    val polishDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl"))
    val formattedDate = state.date.format(polishDateFormatter).replaceFirstChar { it.uppercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.dutyId == 0L) "Nowa Służba" else "Edycja Służby",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.navigationBarsPadding().imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    RailLogHeroButton(
                        text = if (state.dutyId == 0L) "ZAPISZ SŁUŻBĘ" else "ZAPISZ ZMIANY",
                        onClick = viewModel::saveDuty,
                        enabled = !state.isSaving
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: DATE
            item {
                Spacer(modifier = Modifier.height(2.dp))
                FormSectionHeader(title = "DATA SŁUŻBY")
                TechnicalCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickChip(
                        text = "Dzisiaj",
                        isSelected = state.date == LocalDate.now(),
                        onClick = { viewModel.onDateChanged(LocalDate.now()) }
                    )
                    QuickChip(
                        text = "Wczoraj",
                        isSelected = state.date == LocalDate.now().minusDays(1),
                        onClick = { viewModel.onDateChanged(LocalDate.now().minusDays(1)) }
                    )
                }
            }

            // Section 2: MULTI-SEGMENT ROUTE WITH LEGS (ODCINKI TRASY & POJAZDY NA ODCINKACH)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormSectionHeader(title = "ODCINKI TRASY (${state.legs.size})")
                    if (recentRoutes.isNotEmpty() && state.legs.size == 1 && state.legs[0].startStation.isBlank()) {
                        Text(
                            text = "Szablon z historii",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Quick route chips for first leg if blank
                if (recentRoutes.isNotEmpty() && state.legs.size == 1 && state.legs[0].startStation.isBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentRoutes) { route ->
                            QuickChip(
                                text = route.joinToString(" → "),
                                onClick = { viewModel.applyRouteToFirstLeg(route) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.legs.forEachIndexed { index, leg ->
                        LegInputCard(
                            index = index,
                            leg = leg,
                            canDelete = state.legs.size > 1,
                            frequentSeries = frequentSeries,
                            recentVehicles = recentVehicles,
                            onUpdate = { updated -> viewModel.updateLeg(index, updated) },
                            onDelete = { viewModel.removeLeg(index) },
                            onSwap = { viewModel.swapLegStations(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                RailLogSecondaryButton(
                    text = "+ DODAJ KOLEJNY ODCINEK",
                    onClick = viewModel::addLeg,
                    icon = Icons.Default.Add,
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            // Section 3: SHIFT TIMES (GODZINY SŁUŻBY)
            item {
                FormSectionHeader(title = "GODZINY SŁUŻBY (OPCJONALNIE)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.startTime,
                        onValueChange = viewModel::onStartTimeChanged,
                        label = { Text("Rozpoczęcie (np. 22:00)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("06:00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        colors = customTextFieldColors()
                    )
                    OutlinedTextField(
                        value = state.endTime,
                        onValueChange = viewModel::onEndTimeChanged,
                        label = { Text("Zakończenie (np. 06:15)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("14:30") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        colors = customTextFieldColors()
                    )
                }

                if (state.displayDuration != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Długość służby: ${state.displayDuration} (uwzględnia północ)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Section 4: OVERTIME (NADGODZINY)
            item {
                FormSectionHeader(title = "NADGODZINY (DOKŁADNY CZAS)")
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cała służba w nadgodzinach",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.displayDuration != null) {
                                        "Zaliczy ${state.displayDuration} jako nadgodziny"
                                    } else {
                                        "Oznaczenie bez wymuszania wpisywania godzin"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.isWholeDutyOvertime,
                                onCheckedChange = viewModel::setWholeDutyOvertime,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        AnimatedVisibility(
                            visible = !state.isWholeDutyOvertime,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Wpisz dokładną liczbę godzin i minut:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Hours
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.adjustOvertime(-1, 0) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Mniej godzin")
                                        }
                                        OutlinedTextField(
                                            value = state.overtimeHours,
                                            onValueChange = viewModel::onOvertimeHoursChanged,
                                            label = { Text("Godziny") },
                                            placeholder = { Text("0") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = customTextFieldColors()
                                        )
                                        IconButton(
                                            onClick = { viewModel.adjustOvertime(1, 0) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Więcej godzin")
                                        }
                                    }

                                    // Minutes
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.adjustOvertime(0, -1) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Mniej minut")
                                        }
                                        OutlinedTextField(
                                            value = state.overtimeMinutes,
                                            onValueChange = viewModel::onOvertimeMinutesChanged,
                                            label = { Text("Minuty") },
                                            placeholder = { Text("0") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = customTextFieldColors()
                                        )
                                        IconButton(
                                            onClick = { viewModel.adjustOvertime(0, 1) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Więcej minut")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick helper chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item { QuickChip(text = "+15 min", onClick = { viewModel.adjustOvertime(0, 15) }) }
                                    item { QuickChip(text = "+30 min", onClick = { viewModel.adjustOvertime(0, 30) }) }
                                    item { QuickChip(text = "+1 h", onClick = { viewModel.adjustOvertime(1, 0) }) }
                                    item { QuickChip(text = "+2 h", onClick = { viewModel.adjustOvertime(2, 0) }) }
                                    if (state.totalOvertimeMinutes > 0) {
                                        item { QuickChip(text = "Wyzeruj", onClick = viewModel::resetOvertime) }
                                    }
                                }

                                if (state.totalOvertimeMinutes > 0) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val h = state.totalOvertimeMinutes / 60
                                    val m = state.totalOvertimeMinutes % 60
                                    Text(
                                        text = "Łącznie: +$h h ${m.toString().padStart(2, '0')} min",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PantographAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: NOTES & PRIVATE NOTES (COLLAPSIBLE / CLEAN)
            item {
                TechnicalCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { notesExpanded = !notesExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NOTATKI I UWAGI ZE SZLAKU",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.notes.isNotBlank() || state.additionalNotes.isNotBlank()) "Wprowadzono notatki" else "Opcjonalne uwagi i prywatne zapiski",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (notesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = notesExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = viewModel::onNotesChanged,
                            label = { Text("Uwagi ze szlaku") },
                            placeholder = { Text("np. Opóźnienie 8 min, spokojny przejazd, defekt...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp),
                            colors = customTextFieldColors()
                        )

                        OutlinedTextField(
                            value = state.additionalNotes,
                            onValueChange = viewModel::onAdditionalNotesChanged,
                            label = { Text("Dodatkowe prywatne notatki") },
                            placeholder = { Text("Prywatne zapiski, przypomnienia...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = customTextFieldColors()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onDateChanged(selected)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("WYBIERZ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("ANULUJ")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun LegInputCard(
    index: Int,
    leg: LegFormState,
    canDelete: Boolean,
    frequentSeries: List<String>,
    recentVehicles: List<String>,
    onUpdate: (LegFormState) -> Unit,
    onDelete: () -> Unit,
    onSwap: () -> Unit
) {
    TechnicalCard(
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Leg label and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(text = "ODCINEK ${index + 1}", color = MaterialTheme.colorScheme.primary)
                }
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń odcinek",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Start & End Stations
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = leg.startStation,
                        onValueChange = { onUpdate(leg.copy(startStation = it)) },
                        label = { Text("Stacja początkowa (Od)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        colors = customTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = leg.endStation,
                        onValueChange = { onUpdate(leg.copy(endStation = it)) },
                        label = { Text("Stacja końcowa (Do)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        colors = customTextFieldColors()
                    )
                }

                IconButton(
                    onClick = onSwap,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Odwróć stacje",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Train Number and Vehicle on this leg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = leg.trainNumber,
                    onValueChange = { onUpdate(leg.copy(trainNumber = it.uppercase())) },
                    label = { Text("Nr pociągu") },
                    placeholder = { Text("np. 3560") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = leg.vehicle,
                    onValueChange = { onUpdate(leg.copy(vehicle = it)) },
                    label = { Text("Pojazd odcinka") },
                    placeholder = { Text("np. EU160-025") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                    colors = customTextFieldColors()
                )
            }

            // Quick suggestion chips for vehicle on this leg
            if (frequentSeries.isNotEmpty() || recentVehicles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Show recent exact vehicles if available
                    items(recentVehicles.take(3)) { vehicle ->
                        QuickChip(
                            text = vehicle,
                            leadingIcon = Icons.Default.Train,
                            isSelected = leg.vehicle.equals(vehicle, ignoreCase = true),
                            onClick = { onUpdate(leg.copy(vehicle = vehicle)) }
                        )
                    }
                    // Show frequent series
                    items(frequentSeries.take(4)) { series ->
                        QuickChip(
                            text = series,
                            isSelected = leg.vehicle.startsWith(series, ignoreCase = true),
                            onClick = {
                                val currentNumber = leg.vehicle.substringAfter("-", "").ifBlank { null }
                                val newVeh = if (currentNumber != null) "$series-$currentNumber" else series
                                onUpdate(leg.copy(vehicle = newVeh))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
