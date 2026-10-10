package pl.raillog.app.presentation.statistics

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.raillog.app.core.designsystem.GriffinRed
import pl.raillog.app.core.designsystem.PantographAmber
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
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

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
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Poprzedni miesiąc")
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "WYBRANY OKRES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = periodTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GriffinRed
                                )
                            }

                            IconButton(onClick = viewModel::selectNextMonth) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Następny miesiąc")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Służby w okresie",
                        value = "${stats.totalDuties}",
                        accentColor = GriffinRed,
                        icon = Icons.Default.DirectionsRailway,
                        modifier = Modifier.weight(1f)
                    )

                    val totalHours = stats.totalOvertimeMinutes / 60
                    val totalMins = stats.totalOvertimeMinutes % 60
                    val overtimeStr = if (totalHours > 0) "${totalHours}h ${totalMins}m" else "${totalMins}m"

                    StatMetricCard(
                        title = "Suma nadgodzin",
                        value = if (stats.totalOvertimeMinutes > 0) overtimeStr else "0 min",
                        accentColor = PantographAmber,
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section: Vehicle Series Distribution in period
            if (stats.seriesRanking.isNotEmpty()) {
                item {
                    Text(
                        text = "UDZIAŁ SERII POJAZDÓW W OKRESIE",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                    Text(
                        text = "NAJCZĘSTSZE TRASY W OKRESIE",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${route.count} razy",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GriffinRed
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
                    Text(
                        text = "HISTORIA ZESTAWIEŃ MIESIĘCZNYCH",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                                    shape = RoundedCornerShape(8.dp),
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
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${m.dutiesCount} służb" + if (m.overtimeMinutes > 0) " • +$ot" else "",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = GriffinRed
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

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    TechnicalCard(
        accentColor = accentColor,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            val percent = (item.percentage * 100).toInt()
            Text(
                text = "${item.count} służb ($percent%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { item.percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = GriffinRed,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun customChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = GriffinRed.copy(alpha = 0.2f),
    selectedLabelColor = GriffinRed
)
