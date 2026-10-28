package pl.raillog.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyFilter
import pl.raillog.app.domain.model.DutyFilterType
import pl.raillog.app.domain.model.StationKnowledge
import pl.raillog.app.domain.model.VehicleKnowledge
import pl.raillog.app.domain.repository.DutyRepository
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HistoryTab {
    DUTIES,
    KNOWLEDGE
}

enum class KnowledgeType {
    STATIONS,
    VEHICLES
}

data class MonthGroup(
    val yearMonth: String,
    val displayMonth: String,
    val duties: List<Duty>,
    val totalOvertimeMinutes: Int
)

class HistoryViewModel(
    private val repository: DutyRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(HistoryTab.DUTIES)
    val currentTab: StateFlow<HistoryTab> = _currentTab.asStateFlow()

    private val _knowledgeType = MutableStateFlow(KnowledgeType.STATIONS)
    val knowledgeType: StateFlow<KnowledgeType> = _knowledgeType.asStateFlow()

    private val _knowledgeQuery = MutableStateFlow("")
    val knowledgeQuery: StateFlow<String> = _knowledgeQuery.asStateFlow()

    private val _filterState = MutableStateFlow(DutyFilter())
    val filterState: StateFlow<DutyFilter> = _filterState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val groupedDuties: StateFlow<List<MonthGroup>> = _filterState
        .flatMapLatest { filter ->
            repository.getFilteredDuties(filter)
        }
        .map { duties ->
            val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl"))
            duties.groupBy { "${it.date.year}-${it.date.monthValue.toString().padStart(2, '0')}" }
                .map { (ym, monthDuties) ->
                    val sample = monthDuties.first().date
                    MonthGroup(
                        yearMonth = ym,
                        displayMonth = sample.format(formatter).replaceFirstChar { it.uppercase() },
                        duties = monthDuties,
                        totalOvertimeMinutes = monthDuties.sumOf { it.overtimeMinutes }
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val stationKnowledgeList: StateFlow<List<StationKnowledge>> = _knowledgeQuery
        .flatMapLatest { query ->
            repository.getStationKnowledge(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val vehicleKnowledgeList: StateFlow<List<VehicleKnowledge>> = _knowledgeQuery
        .flatMapLatest { query ->
            repository.getVehicleKnowledge(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: HistoryTab) {
        _currentTab.value = tab
    }

    fun setKnowledgeType(type: KnowledgeType) {
        _knowledgeType.value = type
    }

    fun onKnowledgeQueryChanged(query: String) {
        _knowledgeQuery.value = query
    }

    fun onSearchQueryChanged(query: String) {
        _filterState.update { it.copy(query = query) }
    }

    fun onFilterTypeChanged(type: DutyFilterType) {
        _filterState.update { it.copy(filterType = type) }
    }

    class Factory(private val repository: DutyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
