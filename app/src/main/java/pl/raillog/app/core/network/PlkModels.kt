package pl.raillog.app.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

private val plkJson = Json { ignoreUnknownKeys = true }

private fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
private fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull
private fun JsonObject.long(key: String): Long? = (this[key] as? JsonPrimitive)?.longOrNull
private fun JsonObject.bool(key: String): Boolean? = (this[key] as? JsonPrimitive)?.booleanOrNull
private fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject
private fun JsonObject.arr(key: String): JsonArray? = this[key] as? JsonArray

/** Przystanek z rozkładu jazdy. */
data class PlannedStop(
    val stationId: Int,
    val name: String,
    val order: Int,
    val arrival: String?,
    val departure: String?,
    val platform: String?,
    val track: String?
)

/** Pociąg znaleziony w rozkładzie. */
data class TrainMatch(
    val scheduleId: Int,
    val orderId: Long,
    val name: String,
    val carrierCode: String,
    val symbol: String,
    val nationalNumber: String,
    val operatingDate: LocalDate,
    val stops: List<PlannedStop>
) {
    val firstStation: String get() = stops.firstOrNull()?.name.orEmpty()
    val lastStation: String get() = stops.lastOrNull()?.name.orEmpty()
}

object TrainSearch {

    /** Para numerów: 5622 i 5623 to ten sam pociąg (zmiana parzystości przy zmianie kierunku). */
    fun numberPair(input: String): Set<String>? {
        val digits = input.filter { it.isDigit() }
        val n = digits.toLongOrNull() ?: return null
        val base = n - n % 2
        return setOf(base.toString(), (base + 1).toString())
    }

    fun find(scheduleJson: String, input: String, date: LocalDate): List<TrainMatch> {
        val pair = numberPair(input) ?: return emptyList()
        val root = try {
            plkJson.parseToJsonElement(scheduleJson) as? JsonObject
        } catch (e: Exception) {
            null
        } ?: return emptyList()

        val stationNames = stationNames(root)
        val dateText = date.toString()
        val result = mutableListOf<TrainMatch>()

        for (routeEl in root.arr("routes").orEmpty()) {
            val route = routeEl as? JsonObject ?: continue
            val dates = route.arr("operatingDates").orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            if (dateText !in dates) continue

            val stationEls = route.arr("stations").orEmpty().mapNotNull { it as? JsonObject }
            val national = route.str("nationalNumber").orEmpty()
            val numbers = buildSet {
                add(normalizeNumber(national))
                stationEls.forEach { st ->
                    st.str("arrivalTrainNumber")?.let { add(normalizeNumber(it)) }
                    st.str("departureTrainNumber")?.let { add(normalizeNumber(it)) }
                }
            }
            if (numbers.none { it in pair }) continue

            val stops = stationEls.map { st ->
                val id = st.int("stationId") ?: -1
                PlannedStop(
                    stationId = id,
                    name = stationNames[id] ?: "Stacja $id",
                    order = st.int("orderNumber") ?: 0,
                    arrival = st.str("arrivalTime")?.take(5),
                    departure = st.str("departureTime")?.take(5),
                    platform = st.str("departurePlatform") ?: st.str("arrivalPlatform"),
                    track = st.str("departureTrack") ?: st.str("arrivalTrack")
                )
            }.sortedBy { it.order }

            result.add(
                TrainMatch(
                    scheduleId = route.int("scheduleId") ?: continue,
                    orderId = route.long("orderId") ?: continue,
                    name = route.str("name").orEmpty(),
                    carrierCode = route.str("carrierCode").orEmpty(),
                    symbol = route.str("commercialCategorySymbol").orEmpty(),
                    nationalNumber = national,
                    operatingDate = date,
                    stops = stops
                )
            )
        }
        return result
    }

    private fun normalizeNumber(value: String): String =
        value.filter { it.isDigit() }.toLongOrNull()?.toString() ?: value.trim()

    private fun stationNames(root: JsonObject): Map<Int, String> {
        val stations = root.obj("dictionaries")?.obj("stations") ?: return emptyMap()
        val map = mutableMapOf<Int, String>()
        for ((key, value) in stations) {
            val name = (value as? JsonObject)?.str("name") ?: continue
            val id = key.toIntOrNull() ?: (value as? JsonObject)?.int("id") ?: continue
            map[id] = name
        }
        return map
    }
}

/** Stacja z danych na żywo. */
data class LiveStop(
    val stationId: Int,
    val name: String,
    val sequence: Int,
    val plannedArrival: String?,
    val plannedDeparture: String?,
    val actualArrival: String?,
    val actualDeparture: String?,
    val arrivalDelayMinutes: Int?,
    val departureDelayMinutes: Int?,
    val isConfirmed: Boolean,
    val isCancelled: Boolean
) {
    /** Opóźnienie na stacji (odjazd, a gdy go brak – przyjazd). */
    val delayMinutes: Int? get() = departureDelayMinutes ?: arrivalDelayMinutes
}

data class LiveTrain(
    val status: String,
    val stops: List<LiveStop>
) {
    /** Ostatnia potwierdzona stacja. */
    val lastConfirmed: LiveStop? get() = stops.filter { it.isConfirmed }.maxByOrNull { it.sequence }

    val currentDelayMinutes: Int get() = lastConfirmed?.delayMinutes ?: 0

    /** Duży status do pokazania w UI. */
    val statusText: String
        get() = when (status.uppercase()) {
            "X" -> "Odwołany"
            "Q" -> "Częściowo odwołany"
            "S", "N" -> "Nie wyruszył"
            "C", "F" -> "Zakończył kurs"
            else -> if (currentDelayMinutes >= 1) "Opóźniony +$currentDelayMinutes min" else "Planowo"
        }

    companion object {
        fun parse(json: String, extraStationNames: Map<Int, String> = emptyMap()): LiveTrain? {
            val root = try {
                plkJson.parseToJsonElement(json)
            } catch (e: Exception) {
                return null
            }
            val trainObj = findTrainObject(root) ?: return null
            val names = extraStationNames.toMutableMap()
            // Słownik nazw stacji może leżeć w obiekcie głównym lub w obiekcie pociągu
            listOfNotNull(root as? JsonObject, trainObj).forEach { o ->
                (o["stations"] as? JsonObject)?.forEach { (k, v) ->
                    val id = k.toIntOrNull()
                    val name = (v as? JsonPrimitive)?.contentOrNull
                    if (id != null && name != null) names[id] = name
                }
            }
            val stops = trainObj.arr("stations").orEmpty().mapNotNull { it as? JsonObject }.map { st ->
                val id = st.int("stationId") ?: -1
                val plannedArr = st.str("plannedArrival")
                val plannedDep = st.str("plannedDeparture")
                val actualArr = st.str("actualArrival")
                val actualDep = st.str("actualDeparture")
                LiveStop(
                    stationId = id,
                    name = names[id] ?: "Stacja $id",
                    sequence = st.int("actualSequenceNumber") ?: st.int("plannedSequenceNumber") ?: 0,
                    plannedArrival = plannedArr,
                    plannedDeparture = plannedDep,
                    actualArrival = actualArr,
                    actualDeparture = actualDep,
                    arrivalDelayMinutes = st.int("arrivalDelayMinutes") ?: delay(plannedArr, actualArr),
                    departureDelayMinutes = st.int("departureDelayMinutes") ?: delay(plannedDep, actualDep),
                    isConfirmed = st.bool("isConfirmed") ?: false,
                    isCancelled = st.bool("isCancelled") ?: false
                )
            }.sortedBy { it.sequence }
            return LiveTrain(status = trainObj.str("trainStatus").orEmpty(), stops = stops)
        }

        private fun findTrainObject(root: JsonElement): JsonObject? {
            val obj = root as? JsonObject ?: return null
            obj.arr("trains")?.let { return it.firstOrNull() as? JsonObject }
            obj.obj("train")?.let { return it }
            return obj
        }

        private fun delay(planned: String?, actual: String?): Int? {
            if (planned == null || actual == null) return null
            return try {
                Duration.between(LocalDateTime.parse(planned), LocalDateTime.parse(actual)).toMinutes().toInt()
            } catch (e: Exception) {
                null
            }
        }
    }
}
