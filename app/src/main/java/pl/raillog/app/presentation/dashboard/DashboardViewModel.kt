package pl.raillog.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.PlannedDuty
import pl.raillog.app.domain.model.StatisticsData
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.domain.repository.PlanRepository

class DashboardViewModel(
    private val repository: DutyRepository,
    planRepository: PlanRepository
) : ViewModel() {

    val latestDuty: StateFlow<Duty?> = repository.getLatestDuty()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val statistics: StateFlow<StatisticsData> = repository.getStatistics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

    val recentVehicles: StateFlow<List<String>> = repository.getRecentVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Najbliższa służba z planu, dla której nie ma jeszcze zapisanej służby z tą datą. */
    val nextPlanned: StateFlow<PlannedDuty?> = combine(
        planRepository.getPlannedDuties(),
        repository.getAllDuties()
    ) { planned, duties ->
        val now = System.currentTimeMillis()
        val savedDates = duties.map { it.date }.toSet()
        planned.firstOrNull { it.endAt > now && it.date !in savedDates }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    class Factory(
        private val repository: DutyRepository,
        private val planRepository: PlanRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository, planRepository) as T
        }
    }
}
