package pl.raillog.app.presentation.dutyform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.raillog.app.core.util.DutyTimeCalculator
import pl.raillog.app.core.util.VehicleStringParser
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyLeg
import pl.raillog.app.domain.repository.DutyRepository
import java.time.LocalDate

data class LegFormState(
    val startStation: String = "",
    val endStation: String = "",
    val trainNumber: String = "",
    val vehicle: String = ""
)

data class DutyFormState(
    val dutyId: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val vehicleSeries: String = "",
    val vehicleNumber: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val legs: List<LegFormState> = listOf(LegFormState()),
    val overtimeHours: String = "",
    val overtimeMinutes: String = "",
    val isWholeDutyOvertime: Boolean = false,
    val notes: String = "",
    val additionalNotes: String = "",
    val isSaving: Boolean = false
) {
    val calculatedDurationMinutes: Int?
        get() = DutyTimeCalculator.calculateDurationMinutes(startTime, endTime)

    val displayDuration: String?
        get() = calculatedDurationMinutes?.let { DutyTimeCalculator.formatDuration(it) }

    val totalOvertimeMinutes: Int
        get() {
            if (isWholeDutyOvertime) {
                return calculatedDurationMinutes ?: 0
            }
            val h = overtimeHours.toIntOrNull() ?: 0
            val m = overtimeMinutes.toIntOrNull() ?: 0
            return (h * 60 + m).coerceAtLeast(0)
        }
}

class DutyFormViewModel(
    private val repository: DutyRepository,
    private val initialDutyId: Long = 0L,
    private val prefilledVehicle: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DutyFormState(dutyId = initialDutyId))
    val uiState: StateFlow<DutyFormState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    val frequentSeries: StateFlow<List<String>> = repository.getFrequentVehicleSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("EU200", "EU160", "EP09", "ED250"))

    val recentVehicles: StateFlow<List<String>> = repository.getRecentVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentRoutes: StateFlow<List<List<String>>> = repository.getRecentRoutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (initialDutyId > 0L) {
            loadExistingDuty(initialDutyId)
        } else if (!prefilledVehicle.isNullOrBlank()) {
            applyVehicle(prefilledVehicle)
        }
    }

    private fun loadExistingDuty(id: Long) {
        viewModelScope.launch {
            val duty = repository.getDutyByIdDirect(id) ?: return@launch
            val formLegs = if (duty.legs.isNotEmpty()) {
                duty.legs.map { leg ->
                    LegFormState(
                        startStation = leg.startStation,
                        endStation = leg.endStation,
                        trainNumber = leg.trainNumber,
                        vehicle = if (!leg.vehicleSeries.isNullOrBlank() || !leg.vehicleNumber.isNullOrBlank()) {
                            leg.displayVehicle
                        } else ""
                    )
                }
            } else {
                listOf(LegFormState())
            }

            val otTotal = duty.overtimeMinutes
            val otH = if (otTotal > 0) (otTotal / 60).toString() else ""
            val otM = if (otTotal > 0) (otTotal % 60).toString() else ""

            _uiState.update {
                it.copy(
                    dutyId = duty.id,
                    date = duty.date,
                    vehicleSeries = duty.vehicleSeries ?: "",
                    vehicleNumber = duty.vehicleNumber ?: "",
                    startTime = duty.startTime ?: "",
                    endTime = duty.endTime ?: "",
                    legs = formLegs,
                    overtimeHours = otH,
                    overtimeMinutes = otM,
                    isWholeDutyOvertime = duty.isWholeDutyOvertime,
                    notes = duty.notes ?: "",
                    additionalNotes = duty.additionalNotes ?: ""
                )
            }
        }
    }

    fun onDateChanged(newDate: LocalDate) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun onVehicleSeriesChanged(input: String) {
        val (series, number) = VehicleStringParser.normalizeSeriesAndNumber(input, null)
        if (number.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    vehicleSeries = series,
                    vehicleNumber = number
                )
            }
        } else {
            _uiState.update { it.copy(vehicleSeries = series) }
        }
    }

    fun onVehicleNumberChanged(number: String) {
        _uiState.update { it.copy(vehicleNumber = number.trim()) }
    }

    fun applyVehicle(formattedVehicle: String) {
        val (series, number) = VehicleStringParser.normalizeSeriesAndNumber(formattedVehicle, null)
        _uiState.update {
            it.copy(
                vehicleSeries = series,
                vehicleNumber = number
            )
        }
    }

    fun onStartTimeChanged(time: String) {
        _uiState.update { it.copy(startTime = time.trim()) }
    }

    fun onEndTimeChanged(time: String) {
        _uiState.update { it.copy(endTime = time.trim()) }
    }

    // --- LEGS MANAGEMENT ---

    fun addLeg() {
        _uiState.update { current ->
            val prevEnd = current.legs.lastOrNull()?.endStation?.trim() ?: ""
            val newLeg = LegFormState(startStation = prevEnd)
            current.copy(legs = current.legs + newLeg)
        }
    }

    fun updateLeg(index: Int, leg: LegFormState) {
        _uiState.update { current ->
            val updated = current.legs.toMutableList()
            if (index in updated.indices) {
                updated[index] = leg
            }
            current.copy(legs = updated)
        }
    }

    fun removeLeg(index: Int) {
        _uiState.update { current ->
            if (current.legs.size <= 1) return@update current
            val updated = current.legs.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            current.copy(legs = updated)
        }
    }

    fun swapLegStations(index: Int) {
        _uiState.update { current ->
            val updated = current.legs.toMutableList()
            if (index in updated.indices) {
                val leg = updated[index]
                updated[index] = leg.copy(startStation = leg.endStation, endStation = leg.startStation)
            }
            current.copy(legs = updated)
        }
    }

    fun applyRouteToFirstLeg(route: List<String>) {
        if (route.size >= 2) {
            _uiState.update { current ->
                val updated = current.legs.toMutableList()
                if (updated.isNotEmpty()) {
                    updated[0] = updated[0].copy(startStation = route[0], endStation = route[1])
                }
                current.copy(legs = updated)
            }
        }
    }

    // --- OVERTIME MANAGEMENT ---

    fun onOvertimeHoursChanged(hours: String) {
        val filtered = hours.filter { it.isDigit() }
        _uiState.update { it.copy(overtimeHours = filtered, isWholeDutyOvertime = false) }
    }

    fun onOvertimeMinutesChanged(minutes: String) {
        val filtered = minutes.filter { it.isDigit() }
        _uiState.update { it.copy(overtimeMinutes = filtered, isWholeDutyOvertime = false) }
    }

    fun setWholeDutyOvertime(enabled: Boolean) {
        _uiState.update { current ->
            if (enabled) {
                val dur = current.calculatedDurationMinutes
                if (dur != null && dur > 0) {
                    val h = dur / 60
                    val m = dur % 60
                    current.copy(isWholeDutyOvertime = true, overtimeHours = h.toString(), overtimeMinutes = m.toString())
                } else {
                    current.copy(isWholeDutyOvertime = true)
                }
            } else {
                current.copy(isWholeDutyOvertime = false)
            }
        }
    }

    fun adjustOvertime(hoursDelta: Int, minutesDelta: Int) {
        _uiState.update { current ->
            val curH = current.overtimeHours.toIntOrNull() ?: 0
            val curM = current.overtimeMinutes.toIntOrNull() ?: 0
            val totalM = (curH * 60 + curM + hoursDelta * 60 + minutesDelta).coerceAtLeast(0)
            val newH = totalM / 60
            val newM = totalM % 60
            current.copy(
                overtimeHours = if (newH > 0 || totalM > 0) newH.toString() else "",
                overtimeMinutes = if (newM > 0 || totalM > 0) newM.toString() else "",
                isWholeDutyOvertime = false
            )
        }
    }

    fun resetOvertime() {
        _uiState.update { it.copy(overtimeHours = "", overtimeMinutes = "", isWholeDutyOvertime = false) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onAdditionalNotesChanged(notes: String) {
        _uiState.update { it.copy(additionalNotes = notes) }
    }

    fun saveDuty() {
        val current = _uiState.value
        if (current.isSaving) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val domainLegs = current.legs
                .filter { it.startStation.isNotBlank() || it.endStation.isNotBlank() || it.trainNumber.isNotBlank() }
                .map { leg ->
                    val (vSeries, vNumber) = if (leg.vehicle.isNotBlank()) {
                        VehicleStringParser.normalizeSeriesAndNumber(leg.vehicle, null)
                    } else {
                        VehicleStringParser.normalizeSeriesAndNumber(current.vehicleSeries, current.vehicleNumber)
                    }
                    DutyLeg(
                        startStation = leg.startStation.trim(),
                        endStation = leg.endStation.trim(),
                        trainNumber = leg.trainNumber.trim().uppercase(),
                        vehicleSeries = vSeries.ifBlank { null },
                        vehicleNumber = vNumber.ifBlank { null }
                    )
                }

            val (primarySeries, primaryNumber) = VehicleStringParser.normalizeSeriesAndNumber(current.vehicleSeries, current.vehicleNumber)
            val finalSeries = primarySeries.ifBlank {
                domainLegs.firstOrNull { !it.vehicleSeries.isNullOrBlank() }?.vehicleSeries
            }
            val finalNumber = primaryNumber.ifBlank {
                domainLegs.firstOrNull { !it.vehicleNumber.isNullOrBlank() }?.vehicleNumber
            }

            val duty = Duty(
                id = current.dutyId,
                date = current.date,
                vehicleSeries = primarySeries,
                vehicleNumber = primaryNumber,
                startTime = current.startTime.ifBlank { null },
                endTime = current.endTime.ifBlank { null },
                dutyDurationMinutes = current.calculatedDurationMinutes ?: 0,
                legs = domainLegs,
                overtimeMinutes = current.totalOvertimeMinutes,
                isWholeDutyOvertime = current.isWholeDutyOvertime,
                notes = current.notes.ifBlank { null },
                additionalNotes = current.additionalNotes.ifBlank { null }
            )

            repository.saveDuty(duty)
            _uiState.update { it.copy(isSaving = false) }
            _saveSuccessEvent.emit(Unit)
        }
    }

    class Factory(
        private val repository: DutyRepository,
        private val dutyId: Long = 0L,
        private val prefilledVehicle: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DutyFormViewModel(repository, dutyId, prefilledVehicle) as T
        }
    }
}
