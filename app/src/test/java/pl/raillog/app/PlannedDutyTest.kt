package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.PlannedDuty
import pl.raillog.app.domain.model.PlannedKind
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class PlannedDutyTest {

    private val warsaw = ZoneId.of("Europe/Warsaw")

    private fun planned(code: String, kind: PlannedKind): PlannedDuty {
        val start = ZonedDateTime.of(2026, 11, 1, 15, 32, 0, 0, warsaw)
        val end = start.plusHours(11).plusMinutes(24)
        return PlannedDuty(
            id = 1, uid = "u", code = code, kind = kind,
            date = LocalDate.of(2026, 11, 1),
            startAt = start.toInstant().toEpochMilli(),
            endAt = end.toInstant().toEpochMilli()
        )
    }

    @Test
    fun dutyCodeLosesLetterPrefix() {
        assertEquals("Służba 135", planned("MBY135", PlannedKind.DUTY).displayName)
        assertEquals("135", planned("MBY135", PlannedKind.DUTY).formDutyCode)
        assertEquals("Służba 109A", planned("MBY109A", PlannedKind.DUTY).displayName)
        assertEquals("109A", planned("MBY109A", PlannedKind.DUTY).formDutyCode)
    }

    @Test
    fun reserveKeepsSummaryAsName() {
        val reserve = planned("Jazda EU-200", PlannedKind.RESERVE)

        assertEquals("Rezerwa · Jazda EU-200", reserve.displayName)
        assertEquals("Rezerwa: Jazda EU-200", reserve.formDutyCode)
    }

    @Test
    fun timesAndDurationInDeviceZone() {
        val duty = planned("MBY135", PlannedKind.DUTY)

        assertEquals("15:32", duty.startTime(warsaw))
        assertEquals("02:56", duty.endTime(warsaw))
        assertEquals("15:32–02:56", duty.timeRange(warsaw))
        assertEquals(11 * 60 + 24, duty.durationMinutes)
        assertEquals("11 h 24 min", duty.displayDuration)
    }

    @Test
    fun displayDutyCodeOnSavedDuty() {
        val date = LocalDate.of(2026, 1, 1)
        assertNull(Duty(date = date, dutyCode = null).displayDutyCode)
        assertNull(Duty(date = date, dutyCode = "  ").displayDutyCode)
        assertEquals("Służba 135", Duty(date = date, dutyCode = "135").displayDutyCode)
        assertEquals("Rezerwa: Jazda EU-200", Duty(date = date, dutyCode = "Rezerwa: Jazda EU-200").displayDutyCode)
    }
}
