package pl.raillog.app.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.repository.DutyRepository

class DutyDetailViewModel(
    private val repository: DutyRepository,
    private val dutyId: Long
) : ViewModel() {

    val duty: StateFlow<Duty?> = repository.getDutyById(dutyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteDuty(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteDuty(dutyId)
            onDeleted()
        }
    }

    class Factory(
        private val repository: DutyRepository,
        private val dutyId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DutyDetailViewModel(repository, dutyId) as T
        }
    }
}
