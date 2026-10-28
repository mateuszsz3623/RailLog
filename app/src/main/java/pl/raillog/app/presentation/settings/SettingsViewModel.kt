package pl.raillog.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.raillog.app.core.util.IcsParser
import pl.raillog.app.domain.repository.AutoBackupInfo
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.domain.repository.PlanRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class SettingsEvent {
    data class ShareContent(val content: String, val mimeType: String, val filename: String) : SettingsEvent()
    data class SaveToFile(val content: String, val filename: String) : SettingsEvent()
    data class ShowMessage(val message: String) : SettingsEvent()
}

data class PlanInfo(val count: Int, val from: LocalDate, val to: LocalDate)

class SettingsViewModel(
    private val repository: DutyRepository,
    private val planRepository: PlanRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _autoBackupInfo = MutableStateFlow<AutoBackupInfo?>(null)
    val autoBackupInfo: StateFlow<AutoBackupInfo?> = _autoBackupInfo.asStateFlow()

    val planInfo: StateFlow<PlanInfo?> = planRepository.getPlannedDuties()
        .map { list ->
            if (list.isEmpty()) null else PlanInfo(list.size, list.minOf { it.date }, list.maxOf { it.date })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        refreshAutoBackup()
    }

    fun importPlan(icsText: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val events = withContext(Dispatchers.Default) { IcsParser.parse(icsText) }
                val summary = planRepository.importPlan(events)
                val message = if (summary == null) {
                    "W pliku nie znaleziono służb z godzinami"
                } else {
                    val dayMonth = DateTimeFormatter.ofPattern("dd.MM")
                    val full = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    "Wczytano ${summary.count} służb (${summary.from.format(dayMonth)}–${summary.to.format(full)})"
                }
                _events.emit(SettingsEvent.ShowMessage(message))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd wczytywania planu: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun clearPlan() {
        viewModelScope.launch {
            try {
                planRepository.clearPlan()
                _events.emit(SettingsEvent.ShowMessage("Plan służb wyczyszczony"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd czyszczenia planu: ${e.localizedMessage}"))
            }
        }
    }

    private fun refreshAutoBackup() {
        viewModelScope.launch {
            _autoBackupInfo.value = try {
                repository.latestAutoBackupInfo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
    }

    fun undoLastRestore() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = repository.restoreLatestAutoBackup()
                if (result.isSuccess) {
                    val count = result.getOrNull()?.count ?: 0
                    _events.emit(SettingsEvent.ShowMessage("Przywrócono dane sprzed ostatniej zmiany – $count służb ✓"))
                } else {
                    _events.emit(SettingsEvent.ShowMessage("Błąd przywracania: ${result.exceptionOrNull()?.localizedMessage}"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd przywracania: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
                refreshAutoBackup()
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val csv = repository.exportCsv()
                _events.emit(
                    SettingsEvent.ShareContent(
                        content = csv,
                        mimeType = "text/csv",
                        filename = "RailLog_Sluzby_${fileDate()}.csv"
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd eksportu CSV: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun saveJsonToFile() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val json = repository.exportBackupJson()
                _events.emit(SettingsEvent.SaveToFile(json, "RailLog_Backup_${fileDate()}.json"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd eksportu JSON: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun fileDate(): String =
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy_MM_dd"))

    fun exportJson() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val json = repository.exportBackupJson()
                _events.emit(
                    SettingsEvent.ShareContent(
                        content = json,
                        mimeType = "application/json",
                        filename = "RailLog_Backup_${fileDate()}.json"
                    )
                )
            } catch (e: CancellationException) {
                throw e
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
                    val outcome = result.getOrNull()
                    val count = outcome?.count ?: 0
                    val message = if (!overwrite) {
                        "Dodano $count, pominięto ${outcome?.skippedDuplicates ?: 0} duplikatów ✓"
                    } else if (outcome?.autoBackupCreated == true) {
                        "Przywrócono $count służb ✓. Poprzednie dane zapisano – możesz je przywrócić w Ustawieniach."
                    } else {
                        "Pomyślnie przywrócono $count służb ✓"
                    }
                    _events.emit(SettingsEvent.ShowMessage(message))
                } else {
                    _events.emit(SettingsEvent.ShowMessage("Błąd przywracania: ${result.exceptionOrNull()?.localizedMessage}"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Błąd przywracania: ${e.localizedMessage}"))
            } finally {
                _isProcessing.value = false
                refreshAutoBackup()
            }
        }
    }

    class Factory(
        private val repository: DutyRepository,
        private val planRepository: PlanRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, planRepository) as T
        }
    }
}
