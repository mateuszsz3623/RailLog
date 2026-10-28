package pl.raillog.app.domain.model

import pl.raillog.app.core.database.entity.PlannedDutyEntity
import pl.raillog.app.core.util.DutyTimeCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class PlannedKind(val dbValue: String) {
    DUTY("SLUZBA"),
    RESERVE("REZERWA");

    companion object {
        fun fromDb(value: String): PlannedKind = entries.firstOrNull { it.dbValue == value } ?: DUTY
    }
}

data class PlannedDuty(
    val id: Long,
    val uid: String,
    val code: String,
    val kind: PlannedKind,
    val date: LocalDate,
    val startAt: Long,
    val endAt: Long
) {
    private val strippedCode: String
        get() = code.trim().replace(CODE_PREFIX_REGEX, "")

    val durationMinutes: Int
        get() = ((endAt - startAt) / 60_000L).toInt().coerceAtLeast(0)

    val displayDuration: String
        get() = DutyTimeCalculator.formatDuration(durationMinutes)

    /** "Służba 135" albo "Rezerwa · Jazda EU-200". */
    val displayName: String
        get() = when (kind) {
            PlannedKind.DUTY -> strippedCode.let { if (it.firstOrNull()?.isDigit() == true) "Służba $it" else it }
            PlannedKind.RESERVE -> "Rezerwa · ${code.trim()}"
        }

    /** Wartość pola „Numer służby" w formularzu po prefillu z planu. */
    val formDutyCode: String
        get() = when (kind) {
            PlannedKind.DUTY -> strippedCode
            PlannedKind.RESERVE -> "Rezerwa: ${code.trim()}"
        }

    fun startTime(zone: ZoneId = ZoneId.systemDefault()): String = formatTime(startAt, zone)

    fun endTime(zone: ZoneId = ZoneId.systemDefault()): String = formatTime(endAt, zone)

    fun timeRange(zone: ZoneId = ZoneId.systemDefault()): String = "${startTime(zone)}–${endTime(zone)}"

    private fun formatTime(millis: Long, zone: ZoneId): String =
        Instant.ofEpochMilli(millis).atZone(zone).format(TIME_FORMAT)

    private companion object {
        val CODE_PREFIX_REGEX = Regex("^\\p{L}+(?=\\d)")
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

fun PlannedDutyEntity.toDomain() = PlannedDuty(
    id = id,
    uid = uid,
    code = code,
    kind = PlannedKind.fromDb(kind),
    date = date,
    startAt = startAt,
    endAt = endAt
)
