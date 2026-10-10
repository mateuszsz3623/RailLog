package pl.raillog.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.raillog.app.domain.repository.DutyRepository

sealed class SettingsEvent {
    data class ShareContent(val content: String, val mimeType: String, val filename: String) : SettingsEvent()
    data class ShowMessage(val message: String) : SettingsEvent()
}

class SettingsViewModel(
    private val repository: DutyRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun exportCsv() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val csv = repository.exportCsv()
                _events.emit(
                    SettingsEvent.ShareContent(
                        content = csv,
                        mimeType = "text/csv",
                        filename = "RailLog_Sluzby.csv"
                    )
                )
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd eksportu CSV: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val json = repository.exportBackupJson()
                _events.emit(
                    SettingsEvent.ShareContent(
                        content = json,
                        mimeType = "application/json",
                        filename = "RailLog_Backup.json"
                    )
                )
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd eksportu JSON: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun restoreBackup(jsonContent: String, overwrite: Boolean) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = repository.restoreBackupJson(jsonContent, overwrite)
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    _events.emit(SettingsEvent.ShowMessage("Pomyślnie przywrócono $count służb ✓"))
                } else {
                    _events.emit(SettingsEvent.ShowMessage("Błąd przywracania: ${result.exceptionOrNull()?.localizedMessage}"))
                }
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd przywracania: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    class Factory(private val repository: DutyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
