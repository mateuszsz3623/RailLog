package pl.raillog.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.StatisticsData
import pl.raillog.app.domain.repository.DutyRepository

class DashboardViewModel(
    private val repository: DutyRepository
) : ViewModel() {

    val latestDuty: StateFlow<Duty?> = repository.getLatestDuty()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val statistics: StateFlow<StatisticsData> = repository.getStatistics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

    val recentVehicles: StateFlow<List<String>> = repository.getRecentVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(private val repository: DutyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
