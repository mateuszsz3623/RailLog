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
