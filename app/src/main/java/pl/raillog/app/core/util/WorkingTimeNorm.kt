package pl.raillog.app.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Wymiar czasu pracy wg Kodeksu pracy: 8 h × dni pon–pt, minus 8 h za każde święto wypadające poza niedzielą. */
object WorkingTimeNorm {

    fun easterSunday(year: Int): LocalDate {
        // Algorytm Meeusa/Jonesa/Butchera
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }

    fun holidays(year: Int): Set<LocalDate> {
        val easter = easterSunday(year)
        val days = mutableSetOf(
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 1, 6),
            easter,
            easter.plusDays(1),
            LocalDate.of(year, 5, 1),
            LocalDate.of(year, 5, 3),
            easter.plusDays(49),
            easter.plusDays(60),
            LocalDate.of(year, 8, 15),
            LocalDate.of(year, 11, 1),
            LocalDate.of(year, 11, 11),
            LocalDate.of(year, 12, 25),
            LocalDate.of(year, 12, 26)
        )
        if (year >= 2025) days.add(LocalDate.of(year, 12, 24))
        return days
    }

    fun normHours(month: YearMonth): Int {
        val holidays = holidays(month.year)
        var hours = 0
        for (day in 1..month.lengthOfMonth()) {
            val date = month.atDay(day)
            val weekday = date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
            if (weekday) hours += 8
        }
        val reductions = holidays.count {
            YearMonth.from(it) == month && it.dayOfWeek != DayOfWeek.SUNDAY
        }
        return hours - 8 * reductions
    }
}
