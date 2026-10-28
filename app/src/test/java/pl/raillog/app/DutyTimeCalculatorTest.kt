package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.raillog.app.core.util.DutyTimeCalculator

class DutyTimeCalculatorTest {

    @Test
    fun testNormalDayShiftDuration() {
        val duration = DutyTimeCalculator.calculateDurationMinutes("08:00", "16:30")
        assertEquals(510, duration)
        assertEquals("8 h 30 min", DutyTimeCalculator.formatDuration(duration ?: 0))
    }

    @Test
    fun testMidnightCrossingShiftDuration() {
        // Shift starts in the evening at 22:00 and ends next morning at 06:15
        val duration = DutyTimeCalculator.calculateDurationMinutes("22:00", "06:15")
        // 22:00 to midnight = 120m, midnight to 06:15 = 375m -> total 495m (8h 15m)
        assertEquals(495, duration)
        assertEquals("8 h 15 min", DutyTimeCalculator.formatDuration(duration ?: 0))
    }

    @Test
    fun testExactHoursDuration() {
        val duration = DutyTimeCalculator.calculateDurationMinutes("07:00", "15:00")
        assertEquals(480, duration)
        assertEquals("8 h", DutyTimeCalculator.formatDuration(duration ?: 0))
    }

    @Test
    fun testSameStartAndEndTime() {
        val duration = DutyTimeCalculator.calculateDurationMinutes("07:00", "07:00")
        assertEquals(0, duration)
        assertEquals("0 min", DutyTimeCalculator.formatDuration(duration ?: 0))
    }

    @Test
    fun testOnlyMinutes() {
        assertEquals("45 min", DutyTimeCalculator.formatDuration(45))
        assertEquals("0 min", DutyTimeCalculator.formatDuration(0))
    }

    @Test
    fun testNullOrBlankInputs() {
        assertNull(DutyTimeCalculator.calculateDurationMinutes(null, "16:00"))
        assertNull(DutyTimeCalculator.calculateDurationMinutes("08:00", null))
        assertNull(DutyTimeCalculator.calculateDurationMinutes("", "16:00"))
        assertNull(DutyTimeCalculator.calculateDurationMinutes("invalid", "16:00"))
    }

    @Test
    fun normalizeTimeInputAcceptsCommonFormats() {
        listOf("6:00", "06:00", "600", "0600", "6.00", "6,00", "6", " 06:00 ").forEach {
            assertEquals(it, "06:00", DutyTimeCalculator.normalizeTimeInput(it))
        }
        assertEquals("14:05", DutyTimeCalculator.normalizeTimeInput("1405"))
        assertEquals("23:59", DutyTimeCalculator.normalizeTimeInput("23:59"))
        assertEquals("00:00", DutyTimeCalculator.normalizeTimeInput("0"))
        assertEquals("22:00", DutyTimeCalculator.normalizeTimeInput("22"))
    }

    @Test
    fun normalizeTimeInputRejectsInvalid() {
        listOf("", "  ", "25:00", "0675", "24:00", "6:5", "abc", "12:345", "12345", "1:2:3", "-1:00").forEach {
            assertNull(it, DutyTimeCalculator.normalizeTimeInput(it))
        }
    }
}
