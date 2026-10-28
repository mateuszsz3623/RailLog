package pl.raillog.app.presentation.trainstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import pl.raillog.app.core.designsystem.component.FormSectionHeader
import pl.raillog.app.core.designsystem.component.RailLogSecondaryButton
import pl.raillog.app.core.designsystem.component.TechnicalCard
import pl.raillog.app.core.network.LiveStop
import pl.raillog.app.core.network.PlannedStop
import pl.raillog.app.core.network.TrainMatch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainStatusScreen(
    viewModel: TrainStatusViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pociąg ${viewModel.numberLabel}",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (val s = state) {
                is TrainStatusUi.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is TrainStatusUi.Error -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = s.message, color = MaterialTheme.colorScheme.error)
                    RailLogSecondaryButton(
                        text = "SPRÓBUJ PONOWNIE",
                        onClick = viewModel::search,
                        contentColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
                is TrainStatusUi.Choose -> ChooseList(s.matches, viewModel)
                is TrainStatusUi.Result -> ResultContent(s, viewModel)
            }
        }
    }
}

@Composable
private fun ChooseList(matches: List<TrainMatch>, viewModel: TrainStatusViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { FormSectionHeader(title = "WYBIERZ POCIĄG") }
        items(matches) { match ->
            TechnicalCard(onClick = { viewModel.select(match) }, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${match.symbol} ${match.nationalNumber} · ${match.name}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${match.firstStation} → ${match.lastStation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultContent(result: TrainStatusUi.Result, viewModel: TrainStatusViewModel) {
    val live = result.live
    val match = result.match
    val delay = live?.currentDelayMinutes ?: 0
    val statusColor = delayColor(delay, live == null)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${match.symbol.ifBlank { "IC" }} ${viewModel.numberLabel} · ${match.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${match.firstStation} → ${match.lastStation}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = live?.statusText ?: "Brak danych na żywo",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                    live?.lastConfirmed?.let { stop ->
                        val time = (stop.actualDeparture ?: stop.actualArrival)?.let(::isoTime)
                        Text(
                            text = "Ostatnia stacja: ${stop.name}${time?.let { " ($it)" }.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    result.liveError?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        item {
            val updated = result.updatedAt.format(DateTimeFormatter.ofPattern("HH:mm"))
            val remaining = result.hourlyRemaining?.let { " · pozostało $it zapytań/h" }.orEmpty()
            Text(
                text = "Zaktualizowano $updated$remaining",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            RailLogSecondaryButton(
                text = if (result.refreshing) "ODŚWIEŻAM…" else "ODŚWIEŻ",
                onClick = viewModel::refresh,
                icon = Icons.Default.Refresh,
                contentColor = MaterialTheme.colorScheme.primary,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        item { FormSectionHeader(title = "TRASA") }
        if (live != null && live.stops.isNotEmpty()) {
            items(live.stops) { stop -> LiveStopRow(stop, match.stops.firstOrNull { it.stationId == stop.stationId }) }
        } else {
            items(match.stops) { stop -> PlannedStopRow(stop) }
        }
        item {
            Text(
                text = "Źródło danych: PKP Polskie Linie Kolejowe S.A. Dane orientacyjne.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun delayColor(delay: Int, unknown: Boolean): Color = when {
    unknown -> MaterialTheme.colorScheme.onSurfaceVariant
    delay <= 0 -> MaterialTheme.colorScheme.tertiary
    delay <= 5 -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.error
}

private fun isoTime(iso: String): String = if (iso.length >= 16) iso.substring(11, 16) else iso

@Composable
private fun LiveStopRow(stop: LiveStop, planned: PlannedStop?) {
    val delay = stop.delayMinutes
    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (stop.isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (delay != null && stop.isConfirmed) {
                    Text(
                        text = if (delay > 0) "+$delay min" else "0 min",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = delayColor(delay, false)
                    )
                }
            }
            val plan = listOfNotNull(
                stop.plannedArrival?.let { "przyj. ${isoTime(it)}" },
                stop.plannedDeparture?.let { "odj. ${isoTime(it)}" }
            ).joinToString(" · ")
            val actual = listOfNotNull(
                stop.actualArrival?.let { "przyj. ${isoTime(it)}" },
                stop.actualDeparture?.let { "odj. ${isoTime(it)}" }
            ).joinToString(" · ")
            if (plan.isNotEmpty()) {
                Text(text = "Plan: $plan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (actual.isNotEmpty()) {
                Text(text = "Rzeczywiste: $actual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
            platformText(planned)?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PlannedStopRow(stop: PlannedStop) {
    TechnicalCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stop.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            val plan = listOfNotNull(
                stop.arrival?.let { "przyj. $it" },
                stop.departure?.let { "odj. $it" }
            ).joinToString(" · ")
            if (plan.isNotEmpty()) {
                Text(text = "Plan: $plan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            platformText(stop)?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun platformText(stop: PlannedStop?): String? {
    if (stop == null) return null
    val parts = listOfNotNull(
        stop.platform?.takeIf { it.isNotBlank() }?.let { "peron $it" },
        stop.track?.takeIf { it.isNotBlank() }?.let { "tor $it" }
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
