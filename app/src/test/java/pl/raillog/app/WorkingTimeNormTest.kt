package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.raillog.app.core.util.WorkingTimeNorm
import java.time.LocalDate
import java.time.YearMonth

class WorkingTimeNormTest {

    @Test
    fun easterDates() {
        assertEquals(LocalDate.of(2026, 4, 5), WorkingTimeNorm.easterSunday(2026))
        assertEquals(LocalDate.of(2027, 3, 28), WorkingTimeNorm.easterSunday(2027))
    }

    @Test
    fun normHoursForKnownMonths() {
        assertEquals(160, WorkingTimeNorm.normHours(YearMonth.of(2026, 11)))
        assertEquals(160, WorkingTimeNorm.normHours(YearMonth.of(2026, 12)))
        assertEquals(168, WorkingTimeNorm.normHours(YearMonth.of(2026, 4)))
    }

    @Test
    fun christmasEveIsHolidayFrom2025Only() {
        assertTrue(LocalDate.of(2025, 12, 24) in WorkingTimeNorm.holidays(2025))
        assertTrue(LocalDate.of(2024, 12, 24) !in WorkingTimeNorm.holidays(2024))
    }

    @Test
    fun movableHolidaysIncluded() {
        val holidays = WorkingTimeNorm.holidays(2026)
        assertTrue(LocalDate.of(2026, 4, 6) in holidays)
        assertTrue(LocalDate.of(2026, 5, 24) in holidays)
        assertTrue(LocalDate.of(2026, 6, 4) in holidays)
    }
}
