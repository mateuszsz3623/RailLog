package pl.raillog.app.domain.model

import java.time.LocalDate

data class StationKnowledge(
    val stationName: String,
    val lastDate: LocalDate,
    val daysAgo: Long,
    val totalTrips: Int,
    val duties: List<Duty>
)

data class VehicleKnowledge(
    val vehicleType: String,
    val lastDate: LocalDate,
    val daysAgo: Long,
    val totalDuties: Int,
    val duties: List<Duty>
)
