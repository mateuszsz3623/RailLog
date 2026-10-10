package pl.raillog.app.presentation.dutyform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.core.designsystem.GriffinRed
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.SignalGreen
import pl.raillog.app.core.designsystem.component.QuickChip
import pl.raillog.app.core.designsystem.component.RailLogHeroButton
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    RailLogHeroButton(
                        text = if (state.dutyId == 0L) "ZAPISZ SŁUŻBĘ" else "ZAPISZ ZMIANY",
                        onClick = viewModel::saveDuty
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
                Spacer(modifier = Modifier.height(4.dp))
                FormSectionTitle(title = "DATA SŁUŻBY")
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
                                tint = GriffinRed,
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

            // Section 2: SHIFT TIMES (CZAS ROZPOCZĘCIA I ZAKOŃCZENIA)
            item {
                FormSectionTitle(title = "GODZINY SŁUŻBY (OPCJONALNIE)")
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
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = GriffinRed,
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

            // Section 3: PRIMARY VEHICLE
            item {
                FormSectionTitle(title = "GŁÓWNY POJAZD TRAKCYJNY")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.vehicleSeries,
                        onValueChange = viewModel::onVehicleSeriesChanged,
                        label = { Text("Seria (np. EU200)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        colors = customTextFieldColors()
                    )
                    OutlinedTextField(
                        value = state.vehicleNumber,
                        onValueChange = viewModel::onVehicleNumberChanged,
                        label = { Text("Numer (np. 005)") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        colors = customTextFieldColors()
                    )
                }

                // Frequent Series Chips
                if (frequentSeries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Najczęściej używane serie:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(frequentSeries) { series ->
                            QuickChip(
                                text = series,
                                isSelected = state.vehicleSeries.equals(series, ignoreCase = true),
                                onClick = { viewModel.onVehicleSeriesChanged(series) }
                            )
                        }
                    }
                }

                // Recent Vehicles Chips
                if (recentVehicles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ostatnio używane pojazdy:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentVehicles) { vehicle ->
                            QuickChip(
                                text = vehicle,
                                leadingIcon = Icons.Default.Train,
                                onClick = { viewModel.applyVehicle(vehicle) }
                            )
                        }
                    }
                }
            }

            // Section 4: MULTI-SEGMENT ROUTE (ODCINKI SŁUŻBY)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormSectionTitle(title = "ODCINKI SŁUŻBY (${state.legs.size})")
                    if (recentRoutes.isNotEmpty() && state.legs.size == 1 && state.legs[0].startStation.isBlank()) {
                        Text(
                            text = "Szablon z historii",
                            style = MaterialTheme.typography.labelSmall,
                            color = GriffinRed
                        )
                    }
                }

                // Quick route chips for first leg
                if (recentRoutes.isNotEmpty() && state.legs.size == 1 && state.legs[0].startStation.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentRoutes) { route ->
                            QuickChip(
                                text = route.joinToString(" → "),
                                onClick = { viewModel.applyRouteToFirstLeg(route) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.legs.forEachIndexed { index, leg ->
                        LegInputCard(
                            index = index,
                            leg = leg,
                            canDelete = state.legs.size > 1,
                            onUpdate = { updated -> viewModel.updateLeg(index, updated) },
                            onDelete = { viewModel.removeLeg(index) },
                            onSwap = { viewModel.swapLegStations(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = viewModel::addLeg,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GriffinRed),
                    border = BorderStroke(1.dp, GriffinRed)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ DODAJ KOLEJNY ODCINEK", fontWeight = FontWeight.Bold)
                }
            }

            // Section 5: OVERTIME (NADGODZINY)
            item {
                FormSectionTitle(title = "NADGODZINY (DOKŁADNY CZAS)")
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Switch for "Cała służba w nadgodzinach"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cała służba w nadgodzinach",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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
                                    checkedThumbColor = GriffinRed,
                                    checkedTrackColor = GriffinRed.copy(alpha = 0.3f)
                                )
                            )
                        }

                        AnimatedVisibility(visible = !state.isWholeDutyOvertime) {
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
                                    // Hours input
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

                                    // Minutes input
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
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = GriffinRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 6: NOTES
            item {
                FormSectionTitle(title = "UWAGI ZE SZLAKU")
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChanged,
                    placeholder = { Text("np. Opóźnienie 8 min, spokojny przejazd, defekt...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = customTextFieldColors()
                )
            }

            // Section 7: ADDITIONAL PRIVATE NOTES
            item {
                FormSectionTitle(title = "DODATKOWE PRYWATNE NOTATKI")
                OutlinedTextField(
                    value = state.additionalNotes,
                    onValueChange = viewModel::onAdditionalNotesChanged,
                    placeholder = { Text("Prywatne zapiski, przypomnienia...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = customTextFieldColors()
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
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
                    Text("WYBIERZ", color = GriffinRed)
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
    onUpdate: (LegFormState) -> Unit,
    onDelete: () -> Unit,
    onSwap: () -> Unit
) {
    TechnicalCard(
        accentColor = GriffinRed,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ODCINEK ${index + 1}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GriffinRed
                )
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

            Spacer(modifier = Modifier.height(8.dp))

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
                        tint = GriffinRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Train Number & Optional Leg Vehicle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = leg.trainNumber,
                    onValueChange = { onUpdate(leg.copy(trainNumber = it.uppercase())) },
                    label = { Text("Nr pociągu (np. 3560)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = leg.vehicle,
                    onValueChange = { onUpdate(leg.copy(vehicle = it)) },
                    label = { Text("Pojazd (opcjonalny)") },
                    placeholder = { Text("np. EU160-26") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                    colors = customTextFieldColors()
                )
            }
        }
    }
}

@Composable
private fun FormSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GriffinRed,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)
