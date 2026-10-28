package pl.raillog.app.core.util

object DutyTimeCalculator {

    /**
     * Calculates duration in minutes between startTime and endTime in format "HH:mm".
     * Handles crossing midnight (e.g., 22:00 to 06:00 = 480 min).
     * Returns null if either time string is blank or invalid.
     */
    fun calculateDurationMinutes(startTime: String?, endTime: String?): Int? {
        if (startTime.isNullOrBlank() || endTime.isNullOrBlank()) return null
        val startMin = parseTimeToMinutes(startTime) ?: return null
        val endMin = parseTimeToMinutes(endTime) ?: return null

        return if (endMin >= startMin) {
            endMin - startMin
        } else {
            // Crossed midnight (e.g., 22:00 -> 06:00 => (1440 - 1320) + 360 = 480 min)
            (24 * 60 - startMin) + endMin
        }
    }

    fun parseTimeToMinutes(timeStr: String): Int? {
        val trimmed = timeStr.trim()
        val parts = trimmed.split(":")
        if (parts.size != 2) return null
        val h = parts[0].trim().toIntOrNull() ?: return null
        val m = parts[1].trim().toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    /**
     * Normalizuje wpis godziny do "HH:mm": "6:00", "600", "0600", "6.00", "6,00", "6" -> "06:00".
     * Zwraca null dla pustego lub błędnego wpisu (np. "25:00", "0675").
     */
    fun normalizeTimeInput(raw: String): String? {
        val text = raw.trim().replace('.', ':').replace(',', ':')
        if (text.isEmpty()) return null
        val hours: String
        val minutes: String
        if (text.contains(':')) {
            val parts = text.split(":")
            if (parts.size != 2) return null
            hours = parts[0]
            minutes = parts[1]
            if (hours.length !in 1..2 || minutes.length != 2) return null
        } else {
            when (text.length) {
                1, 2 -> { hours = text; minutes = "00" }
                3 -> { hours = text.substring(0, 1); minutes = text.substring(1) }
                4 -> { hours = text.substring(0, 2); minutes = text.substring(2) }
                else -> return null
            }
        }
        if (!hours.all { it in '0'..'9' } || !minutes.all { it in '0'..'9' }) return null
        val h = hours.toInt()
        val m = minutes.toInt()
        if (h !in 0..23 || m !in 0..59) return null
        return formatTime(h, m)
    }

    fun formatDuration(minutes: Int): String {
        if (minutes <= 0) return "0 min"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "$h h ${m.toString().padStart(2, '0')} min"
            h > 0 -> "$h h"
            else -> "$m min"
        }
    }

    fun formatTime(hours: Int, minutes: Int): String {
        val h = hours.coerceIn(0, 23).toString().padStart(2, '0')
        val m = minutes.coerceIn(0, 59).toString().padStart(2, '0')
        return "$h:$m"
    }
}
