package pl.raillog.app.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.core.designsystem.PantographAmber
import pl.raillog.app.core.designsystem.PantographAmberDark
import pl.raillog.app.core.designsystem.component.BentoCard
import pl.raillog.app.core.designsystem.component.CockpitNumber
import pl.raillog.app.core.designsystem.component.FormSectionHeader
import pl.raillog.app.core.util.DutyTimeCalculator
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.domain.model.SeriesStatItem
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
) {
    val stats by viewModel.statistics.collectAsState()
    val planStats by viewModel.planStats.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val amberAccent = if (isDark) PantographAmber else PantographAmberDark

    val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl"))
    val currentYm = YearMonth.now()
    val prevYm = currentYm.minusMonths(1)

    val periodTitle = when (val p = selectedPeriod) {
        is SelectedPeriod.Month -> p.yearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() }
        is SelectedPeriod.AllTime -> "Cała historia (Wszystko)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statystyki Pracy",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = viewModel::selectPreviousMonth) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Poprzedni miesiąc",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "WYBRANY OKRES",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = periodTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = viewModel::selectNextMonth) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Następny miesiąc",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick period chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                val isCur = selectedPeriod is SelectedPeriod.Month && (selectedPeriod as SelectedPeriod.Month).yearMonth == currentYm
                                FilterChip(
                                    selected = isCur,
                                    onClick = viewModel::selectCurrentMonth,
                                    label = { Text("Bieżący miesiąc") },
                                    colors = customChipColors()
                                )
                            }
                            item {
                                val isPrev = selectedPeriod is SelectedPeriod.Month && (selectedPeriod as SelectedPeriod.Month).yearMonth == prevYm
                                FilterChip(
                                    selected = isPrev,
                                    onClick = viewModel::selectPreviousMonth,
                                    label = { Text("Poprzedni miesiąc") },
                                    colors = customChipColors()
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedPeriod is SelectedPeriod.AllTime,
                                    onClick = viewModel::selectAllTime,
                                    label = { Text("Wszystko") },
                                    colors = customChipColors()
                                )
                            }
                        }
                    }
                }
            }

            // Section: KPI Summary for selected period
            item {
                FormSectionHeader(title = "ZESTAWIENIE OKRESU")
                val (overtimeValue, overtimeUnit) = cockpitHoursMinutes(stats.totalOvertimeMinutes)

                BentoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CockpitNumber(
                            value = "${stats.totalDuties}",
                            unit = if (stats.totalDuties == 1) "służba" else "służb",
                            label = "SŁUŻBY W OKRESIE",
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        CockpitNumberMedium(
                            value = overtimeValue,
                            unit = overtimeUnit,
                            label = "NADGODZINY",
                            accentColor = if (stats.totalOvertimeMinutes > 0) {
                                amberAccent
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section: plan służb (tylko gdy zaimportowano plan na ten miesiąc)
            planStats?.let { plan ->
                item {
                    FormSectionHeader(title = "PLAN SŁUŻB")
                    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Plan: ${DutyTimeCalculator.formatDuration(plan.plannedMinutes)}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Wymiar (Kodeks pracy): ${plan.normHours} h",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Zapisane: ${DutyTimeCalculator.formatDuration(plan.savedMinutes)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section: Vehicle Series Distribution in period
            if (stats.seriesRanking.isNotEmpty()) {
                item {
                    FormSectionHeader(title = "UDZIAŁ SERII POJAZDÓW W OKRESIE")
                    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            stats.seriesRanking.forEach { item ->
                                SeriesProgressRow(item = item)
                            }
                        }
                    }
                }
            }

            // Section: Top Routes in period
            if (stats.topRoutes.isNotEmpty()) {
                item {
                    FormSectionHeader(title = "NAJCZĘSTSZE TRASY W OKRESIE")
                    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            stats.topRoutes.forEachIndexed { index, route ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. ${route.route}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${route.count} razy",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: Monthly Breakdown History
            if (stats.monthlyHistory.isNotEmpty()) {
                item {
                    FormSectionHeader(title = "HISTORIA ZESTAWIEŃ MIESIĘCZNYCH")
                    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            stats.monthlyHistory.forEach { m ->
                                val h = m.overtimeMinutes / 60
                                val min = m.overtimeMinutes % 60
                                val ot = if (h > 0) "${h}h ${min}m" else "${min}m"

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val parts = m.yearMonth.split("-")
                                            if (parts.size == 2) {
                                                val y = parts[0].toIntOrNull() ?: 2026
                                                val month = parts[1].toIntOrNull() ?: 10
                                                viewModel.selectMonth(YearMonth.of(y, month))
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = m.displayMonth,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${m.dutiesCount} służb" + if (m.overtimeMinutes > 0) " • +$ot" else "",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (m.overtimeMinutes > 0) amberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Mini-wariant [CockpitNumber] na displayMedium (34 sp, mono ze skali typografii) —
 * do ciaśniejszych kafli bento (nadgodziny).
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
private fun SeriesProgressRow(item: SeriesStatItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.series,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            val percent = (item.percentage * 100).toInt()
            Text(
                text = "${item.count} służb ($percent%)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Cienki pasek udziału — wzór MonthPaceBar z Dashboardu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.percentage.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun customChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    selectedLabelColor = MaterialTheme.colorScheme.primary,
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
