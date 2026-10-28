package pl.raillog.app.core.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class IcsEvent(
    val uid: String,
    val summary: String,
    val description: String,
    val start: Instant,
    val end: Instant
)

private typealias IcsProperty = Pair<Map<String, String>, String>

/** Prosty parser kalendarza iCalendar (.ics): tylko VEVENT z godzinami, bez zdarzeń całodniowych. */
object IcsParser {

    private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")

    fun parse(text: String, deviceZone: ZoneId = ZoneId.systemDefault()): List<IcsEvent> {
        val events = mutableListOf<IcsEvent>()
        var current: MutableMap<String, IcsProperty>? = null

        for (line in unfold(text)) {
            when {
                line.equals("BEGIN:VEVENT", ignoreCase = true) -> current = mutableMapOf()
                line.equals("END:VEVENT", ignoreCase = true) -> {
                    current?.let { toEvent(it, deviceZone) }?.let { events.add(it) }
                    current = null
                }
                current != null -> {
                    val prop = parseProperty(line) ?: continue
                    // Pierwsze wystąpienie właściwości wygrywa
                    current.putIfAbsent(prop.first, prop.second to prop.third)
                }
            }
        }
        return events
    }

    /** Łączy linie zawinięte (kontynuacja zaczyna się spacją lub tabem). */
    internal fun unfold(text: String): List<String> {
        val result = mutableListOf<String>()
        for (raw in text.split("\r\n", "\n", "\r")) {
            if ((raw.startsWith(" ") || raw.startsWith("\t")) && result.isNotEmpty()) {
                result[result.lastIndex] = result.last() + raw.substring(1)
            } else {
                result.add(raw)
            }
        }
        return result
    }

    /** Zwraca (NAZWA, parametry, wartość) dla linii typu `DTSTART;TZID=Europe/Berlin:20261101T153200`. */
    private fun parseProperty(line: String): Triple<String, Map<String, String>, String>? {
        var inQuotes = false
        var colon = -1
        for (i in line.indices) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == ':' && !inQuotes) {
                colon = i
                break
            }
        }
        if (colon <= 0) return null
        val head = splitOutsideQuotes(line.substring(0, colon), ';')
        val name = head.first().trim().uppercase()
        val params = head.drop(1).mapNotNull { part ->
            val eq = part.indexOf('=')
            if (eq <= 0) null else part.substring(0, eq).trim().uppercase() to part.substring(eq + 1).trim().trim('"')
        }.toMap()
        return Triple(name, params, line.substring(colon + 1))
    }

    private fun splitOutsideQuotes(text: String, separator: Char): List<String> {
        val parts = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in text) {
            if (c == '"') inQuotes = !inQuotes
            if (c == separator && !inQuotes) {
                parts.add(sb.toString())
                sb.clear()
            } else {
                sb.append(c)
            }
        }
        parts.add(sb.toString())
        return parts
    }

    internal fun unescape(value: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length) {
                val n = value[i + 1]
                if (n == 'n' || n == 'N') sb.append('\n') else sb.append(n)
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun toEvent(props: Map<String, IcsProperty>, zone: ZoneId): IcsEvent? {
        val startProp = props["DTSTART"] ?: return null
        val start = parseDateTime(startProp.first, startProp.second, zone) ?: return null
        val end = props["DTEND"]?.let { parseDateTime(it.first, it.second, zone) } ?: start
        val summary = unescape(props["SUMMARY"]?.second.orEmpty()).trim()
        val description = unescape(props["DESCRIPTION"]?.second.orEmpty()).trim()
        val uid = props["UID"]?.second?.trim()?.takeIf { it.isNotEmpty() }
            ?: "$summary@${start.toEpochMilli()}"
        return IcsEvent(uid = uid, summary = summary, description = description, start = start, end = end)
    }

    /** Null także dla zdarzeń całodniowych (VALUE=DATE) – te pomijamy. */
    private fun parseDateTime(params: Map<String, String>, value: String, deviceZone: ZoneId): Instant? {
        val v = value.trim()
        if (params["VALUE"].equals("DATE", ignoreCase = true) || !v.contains('T')) return null
        return try {
            if (v.endsWith("Z", ignoreCase = true)) {
                LocalDateTime.parse(v.dropLast(1), dateTimeFormat).toInstant(ZoneOffset.UTC)
            } else {
                val zone = params["TZID"]?.let {
                    try {
                        ZoneId.of(it)
                    } catch (e: Exception) {
                        deviceZone
                    }
                } ?: deviceZone
                LocalDateTime.parse(v, dateTimeFormat).atZone(zone).toInstant()
            }
        } catch (e: Exception) {
            null
        }
    }
}
