package pl.raillog.app.domain.model

import kotlinx.serialization.Serializable
import pl.raillog.app.core.util.VehicleStringParser

@Serializable
data class DutyLeg(
    val startStation: String = "",
    val endStation: String = "",
    val trainNumber: String = "",
    val vehicleSeries: String? = null,
    val vehicleNumber: String? = null
) {
    val vehicleType: String
        get() = VehicleStringParser.extractVehicleType(vehicleSeries, vehicleNumber)

    val displayVehicle: String
        get() = VehicleStringParser.format(vehicleSeries, vehicleNumber)

    val routeText: String
        get() = when {
            startStation.isNotBlank() && endStation.isNotBlank() -> "$startStation → $endStation"
            startStation.isNotBlank() -> startStation
            else -> endStation
        }
}
