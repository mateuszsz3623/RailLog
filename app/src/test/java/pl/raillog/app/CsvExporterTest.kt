package pl.raillog.app

import org.junit.Assert.assertTrue
import org.junit.Test
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyLeg
import pl.raillog.app.core.util.CsvExporter
import java.time.LocalDate

class CsvExporterTest {

    @Test
    fun testExportToCsvContainsBomAndSemicolons() {
        val duty = Duty(
            id = 1,
            date = LocalDate.of(2026, 10, 5),
            vehicleSeries = "EU200",
            vehicleNumber = "005",
            startTime = "06:00",
            endTime = "14:20",
            dutyDurationMinutes = 500,
            legs = listOf(
                DutyLeg(
                    startStation = "Bydgoszcz Główna",
                    endStation = "Warszawa Wschodnia",
                    trainNumber = "IC 1234",
                    vehicleSeries = "EU200",
                    vehicleNumber = "005"
                )
            ),
            overtimeMinutes = 80,
            isWholeDutyOvertime = false,
            notes = "Przejazd spokojny, opóźnienie 8 min",
            additionalNotes = "Notatka prywatna"
        )

        val csv = CsvExporter.exportToCsv(listOf(duty))

        // Check for UTF-8 Byte Order Mark
        assertTrue("CSV must start with UTF-8 BOM", csv.startsWith("\uFEFF"))
        // Check for Polish semicolon delimiter
        assertTrue("CSV must contain semicolon delimiter", csv.contains(";"))
        // Check contents
        assertTrue(csv.contains("EU200"))
        assertTrue(csv.contains("005"))
        assertTrue(csv.contains("Bydgoszcz Główna → Warszawa Wschodnia"))
        assertTrue(csv.contains("IC 1234"))
        assertTrue(csv.contains("+1 h 20 min"))
        assertTrue(csv.contains("06:00"))
        assertTrue(csv.contains("14:20"))
        assertTrue(csv.contains("8 h 20 min"))
    }
}
