package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.raillog.app.core.database.entity.DutyEntity
import pl.raillog.app.core.util.BackupManager
import java.time.LocalDate

class BackupManagerTest {

    @Test
    fun testBackupAndRestoreRoundTrip() {
        val duties = listOf(
            DutyEntity(
                id = 1,
                date = LocalDate.of(2026, 10, 5),
                vehicleSeries = "EU200",
                vehicleNumber = "005",
                routeSegments = listOf("Bydgoszcz", "Warszawa"),
                trainNumbers = listOf("IC 1234"),
                overtimeMinutes = 90
            ),
            DutyEntity(
                id = 2,
                date = LocalDate.of(2026, 10, 6),
                vehicleSeries = "EU160",
                vehicleNumber = "012",
                routeSegments = listOf("Warszawa", "Gdynia"),
                trainNumbers = listOf("IC 5612"),
                isWholeDutyOvertime = true
            )
        )

        val json = BackupManager.createBackupJson(duties)
        assertTrue("JSON must contain TaktRail appName", json.contains("TaktRail"))

        val result = BackupManager.parseBackupJson(json)
        assertTrue("Parsing backup should succeed", result.isSuccess)

        val payload = result.getOrThrow()
        assertEquals(2, payload.dutiesCount)
        assertEquals("EU200", payload.duties[0].vehicleSeries)
        assertEquals("005", payload.duties[0].vehicleNumber)
        assertEquals(90, payload.duties[0].overtimeMinutes)
        assertEquals(true, payload.duties[1].isWholeDutyOvertime)

        // Verify backwards compatibility with legacy RailLog backup
        val legacyJson = json.replace("TaktRail", "RailLog")
        val legacyResult = BackupManager.parseBackupJson(legacyJson)
        assertTrue("Parsing legacy RailLog backup should succeed", legacyResult.isSuccess)
        assertEquals(2, legacyResult.getOrThrow().dutiesCount)
    }
}
