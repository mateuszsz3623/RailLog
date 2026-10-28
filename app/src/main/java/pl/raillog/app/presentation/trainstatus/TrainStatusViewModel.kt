package pl.raillog.app.presentation.trainstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.raillog.app.core.network.LiveTrain
import pl.raillog.app.core.network.PlkApiClient
import pl.raillog.app.core.network.PlkResult
import pl.raillog.app.core.network.TrainMatch
import pl.raillog.app.core.network.TrainSearch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed class TrainStatusUi {
    object Loading : TrainStatusUi()
    data class Error(val message: String) : TrainStatusUi()
    data class Choose(val matches: List<TrainMatch>) : TrainStatusUi()
    data class Result(
        val match: TrainMatch,
        val live: LiveTrain?,
        val liveError: String?,
        val updatedAt: LocalTime,
        val hourlyRemaining: Int?,
        val refreshing: Boolean = false
    ) : TrainStatusUi()
}

class TrainStatusViewModel(
    private val client: PlkApiClient,
    private val number: String,
    private val fixedDate: LocalDate?
) : ViewModel() {

    private val _state = MutableStateFlow<TrainStatusUi>(TrainStatusUi.Loading)
    val state: StateFlow<TrainStatusUi> = _state.asStateFlow()

    val numberLabel: String =
        TrainSearch.numberPair(number)?.sortedBy { it.toLong() }?.joinToString("/") ?: number

    init {
        search()
    }

    fun search() {
        _state.value = TrainStatusUi.Loading
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val dates = if (fixedDate != null) listOf(fixedDate) else listOf(today, today.minusDays(1))
                var matches: List<TrainMatch> = emptyList()
                for (date in dates) {
                    when (val schedule = client.scheduleJson(date)) {
                        is PlkResult.Failure -> {
                            _state.value = TrainStatusUi.Error(schedule.message)
                            return@launch
                        }
                        is PlkResult.Success -> {
                            matches = withContext(Dispatchers.Default) { TrainSearch.find(schedule.value, number, date) }
                        }
                    }
                    if (matches.isNotEmpty()) break
                }
                when {
                    matches.isEmpty() -> _state.value = TrainStatusUi.Error(
                        "Nie znaleziono pociągu $numberLabel w rozkładzie IC na " +
                            dates.first().format(DateTimeFormatter.ofPattern("dd.MM"))
                    )
                    matches.size == 1 -> select(matches.first())
                    else -> _state.value = TrainStatusUi.Choose(matches)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = TrainStatusUi.Error("Błąd: ${e.localizedMessage ?: e.javaClass.simpleName}")
            }
        }
    }

    fun select(match: TrainMatch) {
        _state.value = TrainStatusUi.Loading
        loadLive(match)
    }

    /** Ręczne odświeżenie (bez auto-odświeżania). */
    fun refresh() {
        val current = _state.value as? TrainStatusUi.Result ?: return
        _state.value = current.copy(refreshing = true)
        loadLive(current.match)
    }

    private fun loadLive(match: TrainMatch) {
        viewModelScope.launch {
            try {
                val names = match.stops.associate { it.stationId to it.name }
                when (val reply = client.liveTrainJson(match.scheduleId, match.orderId, match.operatingDate)) {
                    is PlkResult.Failure -> keepOrShow(match, null, reply.message, null)
                    is PlkResult.Success -> {
                        val live = withContext(Dispatchers.Default) { LiveTrain.parse(reply.value, names) }
                        keepOrShow(
                            match,
                            live,
                            if (live == null) "Nieczytelna odpowiedź PLK" else null,
                            reply.hourlyRemaining
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                keepOrShow(match, null, "Błąd: ${e.localizedMessage ?: e.javaClass.simpleName}", null)
            }
        }
    }

    private fun keepOrShow(match: TrainMatch, live: LiveTrain?, error: String?, remaining: Int?) {
        val previous = _state.value as? TrainStatusUi.Result
        _state.value = TrainStatusUi.Result(
            match = match,
            // Przy błędzie odświeżenia zostają poprzednie dane
            live = live ?: previous?.live,
            liveError = error,
            updatedAt = if (live != null) LocalTime.now() else previous?.updatedAt ?: LocalTime.now(),
            hourlyRemaining = remaining ?: previous?.hourlyRemaining
        )
    }

    class Factory(
        private val client: PlkApiClient,
        private val number: String,
        private val fixedDate: LocalDate?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrainStatusViewModel(client, number, fixedDate) as T
        }
    }
}
