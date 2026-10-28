package pl.raillog.app.core.util

data class ParsedVehicle(
    val series: String,
    val number: String
)

object VehicleStringParser {

    private val SPACED_SERIES_REGEX = Regex("""^([A-Za-z]{2,4})\s+(\d+)""")
    private val SERIES_NUMBER_REGEX = Regex("""^([A-Za-z0-9]+)[\s\-_\./]+([A-Za-z0-9]+)$""")


    /**
     * Normalizes series and number. If series contains both type and instance number
     * (e.g. "ED160-26" or "ET42-045"), it separates them cleanly into type and number.
     * Also normalizes series formatting (e.g. "ED 160" -> "ED160").
     */
    fun normalizeSeriesAndNumber(series: String?, number: String?): Pair<String, String> {
        val s = series?.trim() ?: ""
        val n = number?.trim() ?: ""
        if (s.isEmpty()) return Pair("", n)

        val parsed = parse(s)
        val finalType = if (parsed.series.isNotEmpty()) parsed.series else s.uppercase()
        val finalNumber = when {
            n.isNotEmpty() -> n
            parsed.number.isNotEmpty() -> parsed.number
            else -> ""
        }
        return Pair(finalType, finalNumber)
    }

    /**
     * Extracts pure vehicle type/series (e.g. "ED160" from "ED160-26", "ED160 01", "ED160").
     * Always ignores vehicle instance number.
     */
    fun extractVehicleType(series: String?, number: String? = null): String {
        val s = series?.trim() ?: ""
        if (s.isEmpty()) return ""
        val parsed = parse(s)
        return if (parsed.series.isNotEmpty()) parsed.series else s.uppercase()
    }

    /**
     * Parses a vehicle string like "ED160-26", "ET42-045", "EU200-005", "EU 160 014", "EP09/046", "ED250"
     * into series (type) and number parts.
     */
    fun parse(input: String): ParsedVehicle {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ParsedVehicle(series = "", number = "")
        }

        // Normalize internal spacing in Polish series like "EU 160" or "ED 160-26" -> "EU160" / "ED160-26"
        val normalized = trimmed.replace(SPACED_SERIES_REGEX, "$1$2")

        // Match patterns like "ED160-26", "ET42-045", "EU200 005", "EU160/012", "EP09.046"
        val match = SERIES_NUMBER_REGEX.find(normalized)

        return if (match != null) {
            val series = match.groupValues[1].uppercase().trim()
            val number = match.groupValues[2].trim()
            ParsedVehicle(series = series, number = number)
        } else {
            ParsedVehicle(series = normalized.uppercase(), number = "")
        }
    }

    /**
     * Formats series and number into a standard rail format, e.g. "ED160-26"
     */
    fun format(series: String?, number: String?): String {
        val (normSeries, normNumber) = normalizeSeriesAndNumber(series, number)
        return when {
            normSeries.isNotEmpty() && normNumber.isNotEmpty() -> "$normSeries-$normNumber"
            normSeries.isNotEmpty() -> normSeries
            normNumber.isNotEmpty() -> normNumber
            else -> "—"
        }
    }

    /**
     * Formats a list of vehicle types or full vehicles into a clean, deduplicated summary string of TYPES:
     * e.g. ["ED160-26", "ED160-01", "ET42-045"] -> "ED160 • ET42"
     */
    fun formatTypesSummary(vehiclesOrTypes: List<String>): String {
        val distinct = vehiclesOrTypes
            .map { extractVehicleType(it) }
            .filter { it.isNotBlank() && it != "—" }
            .distinct()
        return if (distinct.isEmpty()) "—" else distinct.joinToString(" • ")
    }

    /**
     * Formats overtime minutes into human readable Polish format: e.g. "+1 h 20 min" or "+45 min"
     */
    fun formatOvertime(minutes: Int, isWholeDuty: Boolean = false): String {
        if (isWholeDuty) {
            return "Cała służba"
        }
        if (minutes <= 0) {
            return "Brak"
        }
        val hours = minutes / 60
        val rem = minutes % 60
        return when {
            hours > 0 && rem > 0 -> "+$hours h $rem min"
            hours > 0 -> "+$hours h"
            else -> "+$rem min"
        }
    }
}
