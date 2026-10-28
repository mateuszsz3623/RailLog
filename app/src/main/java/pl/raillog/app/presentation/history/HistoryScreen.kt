package pl.raillog.app.presentation.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.PantographAmberDark
import pl.raillog.app.core.designsystem.SignalGreen
import pl.raillog.app.core.designsystem.component.StatusBadge
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyFilterType
import pl.raillog.app.domain.model.StationKnowledge
import pl.raillog.app.domain.model.VehicleKnowledge
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToDutyDetail: (Long) -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val filter by viewModel.filterState.collectAsState()
    val monthGroups by viewModel.groupedDuties.collectAsState()
    val knowledgeType by viewModel.knowledgeType.collectAsState()
    val knowledgeQuery by viewModel.knowledgeQuery.collectAsState()
    val stationKnowledge by viewModel.stationKnowledgeList.collectAsState()
    val vehicleKnowledge by viewModel.vehicleKnowledgeList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dziennik & Karta Szlaku",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Widok Kalendarza",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            // Main Top Tabs: [Dziennik Służb] | [Karta Szlaku i Taboru]
            TabRow(
                selectedTabIndex = if (currentTab == HistoryTab.DUTIES) 0 else 1,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (currentTab == HistoryTab.DUTIES) 0 else 1]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = currentTab == HistoryTab.DUTIES,
                    onClick = { viewModel.setTab(HistoryTab.DUTIES) },
                    text = { Text("Dziennik Służb", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = currentTab == HistoryTab.KNOWLEDGE,
                    onClick = { viewModel.setTab(HistoryTab.KNOWLEDGE) },
                    text = { Text("Karta Szlaku i Taboru", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentTab == HistoryTab.DUTIES) {
                // --- TAB 1: DZIENNIK SŁUŻB ---
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Szukaj pojazdu, trasy, pociągu...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (filter.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Wyczyść")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = customSearchFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = filter.filterType == DutyFilterType.ALL,
                            onClick = { viewModel.onFilterTypeChanged(DutyFilterType.ALL) },
                            label = { Text("Wszystkie") },
                            colors = customChipColors()
                        )
                    }
                    item {
                        FilterChip(
                            selected = filter.filterType == DutyFilterType.OVERTIME_ONLY,
                            onClick = { viewModel.onFilterTypeChanged(DutyFilterType.OVERTIME_ONLY) },
                            label = { Text("Z nadgodzinami") },
                            colors = customChipColors()
                        )
                    }
                    item {
                        FilterChip(
                            selected = filter.filterType == DutyFilterType.THIS_MONTH,
                            onClick = { viewModel.onFilterTypeChanged(DutyFilterType.THIS_MONTH) },
                            label = { Text("Ten miesiąc") },
                            colors = customChipColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (monthGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filter.query.isNotEmpty()) "Brak wyników wyszukiwania" else "Brak zarejestrowanych służb",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        monthGroups.forEach { group ->
                            item {
                                MonthHeader(group = group)
                            }

                            items(group.duties, key = { it.id }) { duty ->
                                HistoryDutyItem(
                                    duty = duty,
                                    onClick = { onNavigateToDutyDetail(duty.id) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            } else {
                // --- TAB 2: KARTA ZNAJOMOŚCI SZLAKU I TABORU ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = knowledgeType == KnowledgeType.STATIONS,
                        onClick = { viewModel.setKnowledgeType(KnowledgeType.STATIONS) },
                        label = { Text("Znajomość Stacji / Szlaku") },
                        colors = customChipColors(),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = knowledgeType == KnowledgeType.VEHICLES,
                        onClick = { viewModel.setKnowledgeType(KnowledgeType.VEHICLES) },
                        label = { Text("Historia Taboru") },
                        colors = customChipColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = knowledgeQuery,
                    onValueChange = viewModel::onKnowledgeQueryChanged,
                    placeholder = {
                        Text(if (knowledgeType == KnowledgeType.STATIONS) "Szukaj stacji (np. Gdynia, Piła)..." else "Szukaj pojazdu (np. EU160, EP09)...")
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (knowledgeQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onKnowledgeQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Wyczyść")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = customSearchFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (knowledgeType == KnowledgeType.STATIONS) {
                    if (stationKnowledge.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (knowledgeQuery.isNotEmpty()) "Brak stacji o nazwie \"$knowledgeQuery\"" else "Brak zarejestrowanych stacji",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(stationKnowledge, key = { it.stationName }) { item ->
                                StationKnowledgeCard(
                                    item = item,
                                    onDutyClick = onNavigateToDutyDetail
                                )
                            }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                } else {
                    if (vehicleKnowledge.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (knowledgeQuery.isNotEmpty()) "Brak pojazdu \"$knowledgeQuery\"" else "Brak zarejestrowanych pojazdów",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(vehicleKnowledge, key = { it.vehiclePlate }) { item ->
                                VehicleKnowledgeCard(
                                    item = item,
                                    onDutyClick = onNavigateToDutyDetail
                                )
                            }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationKnowledgeCard(
    item: StationKnowledge,
    onDutyClick: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val polishDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
    val formattedDate = item.lastDate.format(polishDateFormatter)

    val daysText = when (item.daysAgo) {
        0L -> "Dzisiaj"
        1L -> "Wczoraj"
        else -> "${item.daysAgo} dni temu"
    }

    val badgeColor = when {
        item.daysAgo <= 90L -> SignalGreen
        item.daysAgo <= 180L -> MaterialTheme.colorScheme.primary
        else -> PantographAmber
    }

    TechnicalCard(
        accentColor = badgeColor,
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRailway,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.stationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusBadge(text = daysText, color = badgeColor)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ostatni przejazd: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Łącznie: ${item.totalTrips} służb",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ostatnie służby z tą stacją:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    item.duties.take(5).forEach { duty ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDutyClick(duty.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${duty.date} • ${duty.displayRoute}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (duty.displayTrains != "—" || duty.displayVehicle != "—") {
                                        Text(
                                            text = "Pociąg: ${duty.displayTrains} • Pojazd: ${duty.displayVehicle}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "Szczegóły ›",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleKnowledgeCard(
    item: VehicleKnowledge,
    onDutyClick: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val polishDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
    val formattedDate = item.lastDate.format(polishDateFormatter)

    val daysText = when (item.daysAgo) {
        0L -> "Dzisiaj"
        1L -> "Wczoraj"
        else -> "${item.daysAgo} dni temu"
    }

    TechnicalCard(
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Train,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.vehicleType,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusBadge(text = daysText, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ostatnio prowadziłeś ${item.vehicleType}: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Łącznie: ${item.totalDuties} służb",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ostatnie służby z tym pojazdem:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    item.duties.take(5).forEach { duty ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDutyClick(duty.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${duty.date} • ${duty.displayRoute}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (duty.displayTrains != "—") {
                                        Text(
                                            text = "Pociągi: ${duty.displayTrains}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "Szczegóły ›",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(group: MonthGroup) {
    val hours = group.totalOvertimeMinutes / 60
    val mins = group.totalOvertimeMinutes % 60
    val overtimeText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val amberAccent = if (isDark) PantographAmber else PantographAmberDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.displayMonth.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${group.duties.size} służb" + if (group.totalOvertimeMinutes > 0) " • +$overtimeText" else "",
            style = MaterialTheme.typography.labelMedium,
            color = if (group.totalOvertimeMinutes > 0) amberAccent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryDutyItem(
    duty: Duty,
    onClick: () -> Unit
) {
    val dayFormatter = DateTimeFormatter.ofPattern("dd", Locale("pl"))
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale("pl"))

    TechnicalCard(
        accentColor = if (duty.overtimeMinutes > 0 || duty.isWholeDutyOvertime) PantographAmber else null,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(44.dp)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = duty.date.format(dayFormatter),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = duty.date.format(dayOfWeekFormatter).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier
                    .width(1.dp)
                    .height(46.dp)
            ) {}

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Route as main duty identifier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = duty.displayRoute,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (duty.overtimeMinutes > 0 || duty.isWholeDutyOvertime) {
                        StatusBadge(
                            text = duty.displayOvertime,
                            color = PantographAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Compact and equal-weight row for all vehicles normalized to types
                if (duty.displayVehiclesSummary != "—") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (duty.vehicleTypes.size > 1) "Pojazdy: " else "Pojazd: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = duty.displayVehiclesSummary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Train numbers & duration details
                val metaItems = mutableListOf<String>()
                if (duty.displayTrains != "—") {
                    metaItems.add("Poc. ${duty.displayTrains}")
                }
                if (duty.displayDuration != null) {
                    metaItems.add("Czas: ${duty.displayDuration}")
                }
                if (metaItems.isNotEmpty()) {
                    Text(
                        text = metaItems.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun customSearchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)

@Composable
private fun customChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    selectedLabelColor = MaterialTheme.colorScheme.primary,
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
