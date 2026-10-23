package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.raillog.app.core.database.entity.DutyEntity
import pl.raillog.app.core.util.DutyTimeCalculator
import pl.raillog.app.core.util.VehicleStringParser
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyLeg
import pl.raillog.app.domain.model.toDomain
import pl.raillog.app.domain.model.toEntity
import java.time.LocalDate

class DutyFormLegVehicleMappingTest {

    @Test
    fun testMultiLegEachWithDistinctVehicle() {
        val legs = listOf(
            DutyLeg(startStation = "Warszawa Wschodnia", endStation = "Gdynia Główna", trainNumber = "EIC 1500", vehicleSeries = "EU160", vehicleNumber = "001"),
            DutyLeg(startStation = "Gdynia Główna", endStation = "Słupsk", trainNumber = "R 55301", vehicleSeries = "EP09", vehicleNumber = "046"),
            DutyLeg(startStation = "Słupsk", endStation = "Szczecin Główny", trainNumber = "IC 8300", vehicleSeries = "EU200", vehicleNumber = "005")
        )

        val duty = Duty(
            id = 1L,
            date = LocalDate.of(2026, 10, 23),
            startTime = "06:15",
            endTime = "14:45",
            legs = legs
        )

        assertEquals("Warszawa Wschodnia → Gdynia Główna → Słupsk → Szczecin Główny", duty.displayRoute)
        assertEquals("EIC 1500, R 55301, IC 8300", duty.displayTrains)
        assertEquals(listOf("EU160", "EP09", "EU200"), duty.vehicleTypes)
        assertEquals("EU160 • EP09 • EU200", duty.displayVehiclesSummary)
        assertEquals("EU160-001 • EP09-046 • EU200-005", duty.displayAllVehicles)

        // Convert to entity
        val entity = duty.toEntity()
        assertEquals(3, entity.legs.size)
        assertEquals("EU160", entity.legs[0].vehicleSeries)
        assertEquals("001", entity.legs[0].vehicleNumber)
        assertEquals("EP09", entity.legs[1].vehicleSeries)
        assertEquals("046", entity.legs[1].vehicleNumber)
        assertEquals("EU200", entity.legs[2].vehicleSeries)
        assertEquals("005", entity.legs[2].vehicleNumber)
        // Verify entity fallback vehicle series is preserved for database index / queries
        assertEquals("EU160", entity.vehicleSeries)
        assertEquals("001", entity.vehicleNumber)

        // Convert back to domain
        val mappedBack = entity.toDomain()
        assertEquals(3, mappedBack.legs.size)
        assertEquals("EU160", mappedBack.legs[0].vehicleSeries)
        assertEquals("EP09", mappedBack.legs[1].vehicleSeries)
        assertEquals("EU200", mappedBack.legs[2].vehicleSeries)
    }

    @Test
    fun testLegacySingleLegDutyVehicleMigration() {
        // Historical entity from RailLog v1: vehicle was stored on entity level, not on leg
        val legacyEntity = DutyEntity(
            id = 42L,
            date = LocalDate.of(2026, 9, 1),
            vehicleSeries = "EU160",
            vehicleNumber = "025",
            legs = listOf(
                DutyLeg(startStation = "Bydgoszcz", endStation = "Gdynia", trainNumber = "5310")
            )
        )

        val domain = legacyEntity.toDomain()
        assertEquals("EU160", domain.vehicleSeries)
        assertEquals("025", domain.vehicleNumber)
        assertEquals(1, domain.legs.size)

        // For a single leg with no vehicle, form logic maps legacy vehicle directly to the leg
        val formLegVehicle = if (!domain.legs[0].vehicleSeries.isNullOrBlank() || !domain.legs[0].vehicleNumber.isNullOrBlank()) {
            domain.legs[0].displayVehicle
        } else if (domain.legs.size == 1 && (!domain.vehicleSeries.isNullOrBlank() || !domain.vehicleNumber.isNullOrBlank())) {
            VehicleStringParser.format(domain.vehicleSeries, domain.vehicleNumber)
        } else ""

        assertEquals("EU160-025", formLegVehicle)
    }

    @Test
    fun testLegacyMultiLegDutyPreservesVehicleWithoutCorruptingLegs() {
        // Historical duty with 2 legs where vehicle was on duty level
        val legacyEntity = DutyEntity(
            id = 55L,
            date = LocalDate.of(2026, 8, 15),
            vehicleSeries = "EP09",
            vehicleNumber = "020",
            legs = listOf(
                DutyLeg(startStation = "Warszawa", endStation = "Dęblin", trainNumber = "1201"),
                DutyLeg(startStation = "Dęblin", endStation = "Lublin", trainNumber = "1203")
            )
        )

        val domain = legacyEntity.toDomain()
        // Duty keeps vehicle
        assertEquals("EP09", domain.vehicleSeries)
        assertEquals("020", domain.vehicleNumber)
        // Legs are not arbitrarily forced to have wrong vehicle
        assertNull(domain.legs[0].vehicleSeries)
        assertNull(domain.legs[1].vehicleSeries)
        // But summary still presents the duty vehicle!
        assertEquals("EP09", domain.displayVehiclesSummary)
        assertEquals("EP09-020", domain.displayAllVehicles)
    }

    @Test
    fun testShiftDurationAndMidnightCrossing() {
        // Normal shift within single day
        val dayShiftMins = DutyTimeCalculator.calculateDurationMinutes("06:00", "14:30")
        assertEquals(510, dayShiftMins) // 8h 30m
        assertEquals("8 h 30 min", DutyTimeCalculator.formatDuration(510))

        // Shift crossing midnight: 22:00 to 06:15
        val nightShiftMins = DutyTimeCalculator.calculateDurationMinutes("22:00", "06:15")
        assertEquals(495, nightShiftMins) // 8h 15m
        assertEquals("8 h 15 min", DutyTimeCalculator.formatDuration(495))

        // Shift crossing midnight: 23:30 to 07:45
        val nightShiftMins2 = DutyTimeCalculator.calculateDurationMinutes("23:30", "07:45")
        assertEquals(495, nightShiftMins2) // 8h 15m
    }

    @Test
    fun testWholeDutyOvertime() {
        val duty = Duty(
            date = LocalDate.of(2026, 10, 20),
            startTime = "22:00",
            endTime = "06:00",
            isWholeDutyOvertime = true
        )

        assertEquals(480, duty.calculatedDurationMinutes) // 8h 00m
        assertEquals("Cała służba (+8 h)", duty.displayOvertime)
    }
}
