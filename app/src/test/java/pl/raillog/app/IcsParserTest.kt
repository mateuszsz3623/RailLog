package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.raillog.app.core.util.IcsParser
import java.time.ZoneId

class IcsParserTest {

    private val warsaw = ZoneId.of("Europe/Warsaw")

    private val sample = listOf(
        "BEGIN:VCALENDAR",
        "VERSION:2.0",
        "BEGIN:VEVENT",
        "UID:TEST-1",
        "SUMMARY:MBY135",
        "DESCRIPTION:Sluzba",
        "LOCATION:MBY+ - MBY-",
        "DTSTART;TZID=Europe/Berlin:20261101T153200",
        "DTEND;TZID=Europe/Berlin:20261102T025600",
        "END:VEVENT",
        "BEGIN:VEVENT",
        "UID:TEST-2",
        "SUMMARY:Jazda EU-200",
        "DESCRIPTION:Rezerwa",
        "DTSTART;TZID=Europe/Berlin:20261103T090000",
        "DTEND;TZID=Europe/Berlin:20261103T190000",
        "END:VEVENT",
        "BEGIN:VEVENT",
        "UID:TEST-3",
        "SUMMARY:DW5",
        "DTSTART;VALUE=DATE:20261104",
        "DTEND;VALUE=DATE:20261105",
        "END:VEVENT",
        "BEGIN:VEVENT",
        "UID:TEST-4",
        "SUMMARY:MBY109A with a very long",
        "  folded name\\, ok",
        "DTSTART:20261105T060000Z",
        "DTEND:20261105T140000Z",
        "END:VEVENT",
        "END:VCALENDAR"
    ).joinToString("\r\n")

    @Test
    fun parsesOvernightDutyInLocalTime() {
        val events = IcsParser.parse(sample, warsaw)
        val duty = events.first { it.uid == "TEST-1" }

        assertEquals("MBY135", duty.summary)
        val start = duty.start.atZone(warsaw)
        val end = duty.end.atZone(warsaw)
        assertEquals("15:32", start.toLocalTime().toString())
        assertEquals("02:56", end.toLocalTime().toString())
        assertEquals(start.toLocalDate().plusDays(1), end.toLocalDate())
        assertEquals((11 * 60 + 24) * 60_000L, duty.end.toEpochMilli() - duty.start.toEpochMilli())
    }

    @Test
    fun parsesReserveWithoutLocation() {
        val reserve = IcsParser.parse(sample, warsaw).first { it.uid == "TEST-2" }

        assertEquals("Jazda EU-200", reserve.summary)
        assertEquals("Rezerwa", reserve.description)
    }

    @Test
    fun skipsAllDayEvents() {
        val events = IcsParser.parse(sample, warsaw)

        assertTrue(events.none { it.uid == "TEST-3" })
        assertEquals(3, events.size)
    }

    @Test
    fun unfoldsWrappedLinesAndUnescapesText() {
        val event = IcsParser.parse(sample, warsaw).first { it.uid == "TEST-4" }

        assertEquals("MBY109A with a very long folded name, ok", event.summary.replace("  ", " "))
    }

    @Test
    fun utcSuffixIsConvertedToDeviceTime() {
        val event = IcsParser.parse(sample, warsaw).first { it.uid == "TEST-4" }

        assertEquals("07:00", event.start.atZone(warsaw).toLocalTime().toString())
    }

    @Test
    fun unknownTimeZoneFallsBackToDeviceZone() {
        val text = "BEGIN:VEVENT\nUID:X\nSUMMARY:A1\nDTSTART;TZID=Nowhere/Land:20261101T100000\nDTEND;TZID=Nowhere/Land:20261101T110000\nEND:VEVENT"

        val event = IcsParser.parse(text, warsaw).single()

        assertEquals("10:00", event.start.atZone(warsaw).toLocalTime().toString())
    }

    @Test
    fun handlesLfOnlyLineEndings() {
        val events = IcsParser.parse(sample.replace("\r\n", "\n"), warsaw)

        assertEquals(3, events.size)
    }
}
