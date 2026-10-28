package pl.raillog.app.domain.model

import pl.raillog.app.core.util.VehicleStringParser

enum class DutyFilterType {
    ALL,
    OVERTIME_ONLY,
    THIS_MONTH
}

data class DutyFilter(
    val query: String = "",
    val filterType: DutyFilterType = DutyFilterType.ALL,
    val selectedSeries: String? = null
)

/** Wyszukiwanie po polach domenowych (stacje, pociągi, pojazdy, uwagi) – działa też dla polskich znaków. */
fun Duty.matchesQuery(query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    val fields = buildList {
        legs.forEach { leg ->
            add(leg.startStation)
            add(leg.endStation)
            add(leg.trainNumber)
            add(leg.displayVehicle)
        }
        add(VehicleStringParser.format(vehicleSeries, vehicleNumber))
        add(vehicleSeries.orEmpty())
        add(notes.orEmpty())
        add(additionalNotes.orEmpty())
    }
    return fields.any { it.contains(q, ignoreCase = true) }
}
