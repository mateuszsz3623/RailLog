package pl.raillog.app.domain.model

import pl.raillog.app.core.database.entity.DutyEntity
import pl.raillog.app.core.util.DutyTimeCalculator
import pl.raillog.app.core.util.VehicleStringParser
import java.time.LocalDate

data class Duty(
    val id: Long = 0,
    val date: LocalDate,
    val vehicleSeries: String? = null,
    val vehicleNumber: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val dutyDurationMinutes: Int = 0,
    val legs: List<DutyLeg> = emptyList(),
    val overtimeMinutes: Int = 0,
    val isWholeDutyOvertime: Boolean = false,
    val notes: String? = null,
    val additionalNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dutyCode: String? = null
) {
    /** Etykieta „Służba 135” dla kodu zaczynającego się cyfrą, inaczej kod bez zmian; null gdy brak. */
    val displayDutyCode: String?
        get() {
            val code = dutyCode?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            return if (code.first().isDigit()) "Służba $code" else code
        }

    /**
     * Unique vehicle types for this duty, deduplicated and normalized (e.g. ["ED160", "ET42"]).
     * Always ignores vehicle instance numbers.
     */
    val vehicleTypes: List<String>
        get() {
            val list = mutableListOf<String>()
            val mainType = VehicleStringParser.extractVehicleType(vehicleSeries, vehicleNumber)
            if (mainType.isNotBlank() && mainType != "—") {
                list.add(mainType)
            }
            legs.forEach { leg ->
                val legType = leg.vehicleType
                if (legType.isNotBlank() && legType != "—" && !list.contains(legType)) {
                    list.add(legType)
                }
            }
            return list
        }

    /**
     * Compact summary of vehicle types for cards and lists (e.g. "ED160 • ET42").
     * Equal visual weight for all vehicles, without highlighting the first one.
     */
    val displayVehiclesSummary: String
        get() = VehicleStringParser.formatTypesSummary(vehicleTypes)

    /**
     * All unique vehicle plates with instance numbers (e.g. ["ED160-26", "ET42-045"]).
     * Equal weight, used in duty details view.
     */
    val allVehicles: List<String>
        get() {
            val list = mutableListOf<String>()
            val main = VehicleStringParser.format(vehicleSeries, vehicleNumber)
            if (main != "—" && main.isNotBlank()) {
                list.add(main)
            }
            legs.forEach { leg ->
                val v = leg.displayVehicle
                if (v != "—" && v.isNotBlank() && !list.contains(v)) {
                    list.add(v)
                }
            }
            return list
        }

    val displayAllVehicles: String
        get() = if (allVehicles.isEmpty()) "—" else allVehicles.joinToString(" • ")

    /**
     * Default summary vehicle string for backward compatibility: returns displayVehiclesSummary.
     */
    val displayVehicle: String
        get() = displayVehiclesSummary

    val displayRoute: String
        get() {
            if (legs.isEmpty()) return "—"
            val stations = mutableListOf<String>()
            legs.forEach { leg ->
                if (stations.isEmpty()) {
                    if (leg.startStation.isNotBlank()) stations.add(leg.startStation.trim())
                } else if (leg.startStation.isNotBlank() && leg.startStation.trim() != stations.last()) {
                    stations.add(leg.startStation.trim())
                }
                if (leg.endStation.isNotBlank()) {
                    stations.add(leg.endStation.trim())
                }
            }
            return if (stations.isEmpty()) "—" else stations.joinToString(" → ")
        }

    val displayTrains: String
        get() {
            val trains = legs.map { it.trainNumber.trim() }.filter { it.isNotEmpty() }.distinct()
            return if (trains.isEmpty()) "—" else trains.joinToString(", ")
        }

    val calculatedDurationMinutes: Int?
        get() = DutyTimeCalculator.calculateDurationMinutes(startTime, endTime)

    val displayDuration: String?
        get() {
            val mins = calculatedDurationMinutes ?: if (dutyDurationMinutes > 0) dutyDurationMinutes else null
            return mins?.let { DutyTimeCalculator.formatDuration(it) }
        }

    val displayOvertime: String
        get() {
            if (isWholeDutyOvertime) {
                val dur = displayDuration
                return if (dur != null) "Cała służba (+$dur)" else "Cała służba"
            }
            return VehicleStringParser.formatOvertime(overtimeMinutes, false)
        }
}

fun DutyEntity.toDomain(): Duty {
    val (normSeries, normNumber) = VehicleStringParser.normalizeSeriesAndNumber(vehicleSeries, vehicleNumber)
    val rawLegs = if (legs.isNotEmpty()) {
        legs
    } else if (routeSegments.size >= 2) {
        val mapped = mutableListOf<DutyLeg>()
        for (i in 0 until routeSegments.size - 1) {
            val train = trainNumbers.getOrNull(i) ?: trainNumbers.firstOrNull() ?: ""
            mapped.add(
                DutyLeg(
                    startStation = routeSegments[i],
                    endStation = routeSegments[i + 1],
                    trainNumber = train,
                    vehicleSeries = normSeries.ifEmpty { null },
                    vehicleNumber = normNumber.ifEmpty { null }
                )
            )
        }
        mapped
    } else {
        emptyList()
    }

    val domainLegs = rawLegs.map { leg ->
        val (legSeries, legNumber) = VehicleStringParser.normalizeSeriesAndNumber(leg.vehicleSeries, leg.vehicleNumber)
        leg.copy(
            vehicleSeries = legSeries.ifEmpty { null },
            vehicleNumber = legNumber.ifEmpty { null }
        )
    }

    return Duty(
        id = id,
        date = date,
        vehicleSeries = normSeries.ifEmpty { null },
        vehicleNumber = normNumber.ifEmpty { null },
        startTime = startTime,
        endTime = endTime,
        dutyDurationMinutes = dutyDurationMinutes,
        legs = domainLegs,
        overtimeMinutes = overtimeMinutes,
        isWholeDutyOvertime = isWholeDutyOvertime,
        notes = notes,
        additionalNotes = additionalNotes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dutyCode = dutyCode
    )
}

fun Duty.toEntity(): DutyEntity {
    val (normSeries, normNumber) = VehicleStringParser.normalizeSeriesAndNumber(vehicleSeries, vehicleNumber)
    val normalizedLegs = legs.map { leg ->
        val (legSeries, legNumber) = VehicleStringParser.normalizeSeriesAndNumber(leg.vehicleSeries, leg.vehicleNumber)
        leg.copy(
            vehicleSeries = legSeries.ifEmpty { null },
            vehicleNumber = legNumber.ifEmpty { null }
        )
    }

    val fallbackRoute = mutableListOf<String>()
    normalizedLegs.forEach { leg ->
        if (fallbackRoute.isEmpty()) {
            if (leg.startStation.isNotBlank()) fallbackRoute.add(leg.startStation.trim())
        } else if (leg.startStation.isNotBlank() && leg.startStation.trim() != fallbackRoute.last()) {
            fallbackRoute.add(leg.startStation.trim())
        }
        if (leg.endStation.isNotBlank()) {
            fallbackRoute.add(leg.endStation.trim())
        }
    }
    val fallbackTrains = normalizedLegs.map { it.trainNumber.trim() }.filter { it.isNotEmpty() }.distinct()

    val calculatedMins = DutyTimeCalculator.calculateDurationMinutes(startTime, endTime) ?: dutyDurationMinutes

    val finalSeries = normSeries.ifEmpty { normalizedLegs.firstOrNull { !it.vehicleSeries.isNullOrBlank() }?.vehicleSeries }
    val finalNumber = normNumber.ifEmpty { normalizedLegs.firstOrNull { !it.vehicleNumber.isNullOrBlank() }?.vehicleNumber }

    return DutyEntity(
        id = id,
        date = date,
        vehicleSeries = finalSeries,
        vehicleNumber = finalNumber,
        startTime = startTime,
        endTime = endTime,
        dutyDurationMinutes = calculatedMins,
        legs = normalizedLegs,
        routeSegments = fallbackRoute,
        trainNumbers = fallbackTrains,
        overtimeMinutes = overtimeMinutes,
        isWholeDutyOvertime = isWholeDutyOvertime,
        notes = notes,
        additionalNotes = additionalNotes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dutyCode = dutyCode?.trim()?.ifEmpty { null }
    )
}
