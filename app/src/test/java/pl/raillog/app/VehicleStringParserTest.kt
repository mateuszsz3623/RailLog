package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.raillog.app.core.util.VehicleStringParser

class VehicleStringParserTest {

    @Test
    fun testParseHyphenatedVehicle() {
        val parsed = VehicleStringParser.parse("EU200-005")
        assertEquals("EU200", parsed.series)
        assertEquals("005", parsed.number)
    }

    @Test
    fun testParseSpacedVehicle() {
        val parsed = VehicleStringParser.parse("EU160 014")
        assertEquals("EU160", parsed.series)
        assertEquals("014", parsed.number)
    }

    @Test
    fun testParseSlashVehicle() {
        val parsed = VehicleStringParser.parse("EP09/046")
        assertEquals("EP09", parsed.series)
        assertEquals("046", parsed.number)
    }

    @Test
    fun testParseSeriesOnly() {
        val parsed = VehicleStringParser.parse("ED250")
        assertEquals("ED250", parsed.series)
        assertEquals("", parsed.number)
    }

    @Test
    fun testExtractVehicleTypeCriticalRequirement() {
        // ED160-26, ED160-01 and ED160-99 must be the single type ED160
        assertEquals("ED160", VehicleStringParser.extractVehicleType("ED160-26"))
        assertEquals("ED160", VehicleStringParser.extractVehicleType("ED160-01"))
        assertEquals("ED160", VehicleStringParser.extractVehicleType("ED160-99"))
        assertEquals("ED160", VehicleStringParser.extractVehicleType("ED160"))
        assertEquals("ED160", VehicleStringParser.extractVehicleType("ED160 26"))

        // ET42-045 and ET42 instances must be ET42
        assertEquals("ET42", VehicleStringParser.extractVehicleType("ET42-045"))
        assertEquals("ET42", VehicleStringParser.extractVehicleType("ET42"))
        assertEquals("ET42", VehicleStringParser.extractVehicleType("ET42 045"))
        assertEquals("ET42", VehicleStringParser.extractVehicleType("ET 42-045"))
    }

    @Test
    fun testNormalizeSeriesAndNumber() {
        val (s1, n1) = VehicleStringParser.normalizeSeriesAndNumber("ED160-26", "")
        assertEquals("ED160", s1)
        assertEquals("26", n1)

        val (s2, n2) = VehicleStringParser.normalizeSeriesAndNumber("ED160", "26")
        assertEquals("ED160", s2)
        assertEquals("26", n2)

        val (s3, n3) = VehicleStringParser.normalizeSeriesAndNumber("ET42-045", "")
        assertEquals("ET42", s3)
        assertEquals("045", n3)

        val (s4, n4) = VehicleStringParser.normalizeSeriesAndNumber("ED160-26", "26")
        assertEquals("ED160", s4)
        assertEquals("26", n4)
    }

    @Test
    fun testFormatTypesSummaryWithoutDuplicates() {
        // As requested in prompt: ED160-26, ED160-01 and ET42-045 -> "ED160 • ET42"
        val input = listOf("ED160-26", "ED160-01", "ET42-045")
        val summary = VehicleStringParser.formatTypesSummary(input)
        assertEquals("ED160 • ET42", summary)

        // Single vehicle
        assertEquals("ED160", VehicleStringParser.formatTypesSummary(listOf("ED160-26")))
        assertEquals("ET42", VehicleStringParser.formatTypesSummary(listOf("ET42")))
    }

    @Test
    fun testFormatVehicle() {
        assertEquals("EU200-005", VehicleStringParser.format("EU200", "005"))
        assertEquals("EU160", VehicleStringParser.format("EU160", null))
        assertEquals("005", VehicleStringParser.format(null, "005"))
        assertEquals("—", VehicleStringParser.format(null, null))
    }

    @Test
    fun testFormatOvertime() {
        assertEquals("+1 h 20 min", VehicleStringParser.formatOvertime(80))
        assertEquals("+45 min", VehicleStringParser.formatOvertime(45))
        assertEquals("+2 h", VehicleStringParser.formatOvertime(120))
        assertEquals("Brak", VehicleStringParser.formatOvertime(0))
        assertEquals("Cała służba", VehicleStringParser.formatOvertime(0, isWholeDuty = true))
    }
}
