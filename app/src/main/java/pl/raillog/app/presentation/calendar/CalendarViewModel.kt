package pl.raillog.app.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.PlannedDuty
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.domain.repository.PlanRepository
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    repository: DutyRepository,
    planRepository: PlanRepository
) : ViewModel() {

    val duties: StateFlow<List<Duty>> = repository.getAllDuties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val planned: StateFlow<List<PlannedDuty>> = planRepository.getPlannedDuties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _yearMonth = MutableStateFlow(YearMonth.now())
    val yearMonth: StateFlow<YearMonth> = _yearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun previousMonth() {
        _yearMonth.value = _yearMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _yearMonth.value = _yearMonth.value.plusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    class Factory(
        private val repository: DutyRepository,
        private val planRepository: PlanRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository, planRepository) as T
        }
    }
}
