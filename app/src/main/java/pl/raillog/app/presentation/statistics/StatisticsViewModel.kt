package pl.raillog.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import pl.raillog.app.core.util.WorkingTimeNorm
import pl.raillog.app.domain.model.StatisticsData
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.domain.repository.PlanRepository
import java.time.YearMonth

data class PlanStats(
    val plannedMinutes: Int,
    val normHours: Int,
    val savedMinutes: Int
)

sealed class SelectedPeriod {
    data class Month(val yearMonth: YearMonth) : SelectedPeriod()
    object AllTime : SelectedPeriod()
}

class StatisticsViewModel(
    private val repository: DutyRepository,
    planRepository: PlanRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow<SelectedPeriod>(SelectedPeriod.Month(YearMonth.now()))
    val selectedPeriod: StateFlow<SelectedPeriod> = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val statistics: StateFlow<StatisticsData> = _selectedPeriod
        .flatMapLatest { period ->
            when (period) {
                is SelectedPeriod.Month -> repository.getStatistics(period.yearMonth)
                is SelectedPeriod.AllTime -> repository.getStatistics(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

    /** Podsumowanie planu dla wybranego miesiąca; null poza widokiem miesiąca lub gdy brak planu na ten miesiąc. */
    val planStats: StateFlow<PlanStats?> = combine(
        _selectedPeriod,
        planRepository.getPlannedDuties(),
        repository.getAllDuties()
    ) { period, planned, duties ->
        val month = (period as? SelectedPeriod.Month)?.yearMonth ?: return@combine null
        val monthPlanned = planned.filter { YearMonth.from(it.date) == month }
        if (monthPlanned.isEmpty()) return@combine null
        PlanStats(
            plannedMinutes = monthPlanned.sumOf { it.durationMinutes },
            normHours = WorkingTimeNorm.normHours(month),
            savedMinutes = duties
                .filter { YearMonth.from(it.date) == month }
                .sumOf { it.calculatedDurationMinutes ?: it.dutyDurationMinutes }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectCurrentMonth() {
        _selectedPeriod.value = SelectedPeriod.Month(YearMonth.now())
    }

    fun selectPreviousMonth() {
        val currentYm = when (val p = _selectedPeriod.value) {
            is SelectedPeriod.Month -> p.yearMonth
            is SelectedPeriod.AllTime -> YearMonth.now()
        }
        _selectedPeriod.value = SelectedPeriod.Month(currentYm.minusMonths(1))
    }

    fun selectNextMonth() {
        val currentYm = when (val p = _selectedPeriod.value) {
            is SelectedPeriod.Month -> p.yearMonth
            is SelectedPeriod.AllTime -> YearMonth.now()
        }
        _selectedPeriod.value = SelectedPeriod.Month(currentYm.plusMonths(1))
    }

    fun selectMonth(ym: YearMonth) {
        _selectedPeriod.value = SelectedPeriod.Month(ym)
    }

    fun selectAllTime() {
        _selectedPeriod.value = SelectedPeriod.AllTime
    }

    class Factory(
        private val repository: DutyRepository,
        private val planRepository: PlanRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatisticsViewModel(repository, planRepository) as T
        }
    }
}
