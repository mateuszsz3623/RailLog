package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyLeg
import pl.raillog.app.core.util.VehicleStringParser
import java.time.LocalDate

class VehicleKnowledgeTest {

    @Test
    fun testVehicleHistoryGroupingIgnoresInstanceNumbers() {
        // Create 3 duties on different dates with ED160-26, ED160-01, ED160-99
        val d1 = Duty(
            id = 1,
            date = LocalDate.of(2026, 10, 1),
            legs = listOf(DutyLeg("Warszawa", "Gdańsk", "101", "ED160", "26"))
        )
        val d2 = Duty(
            id = 2,
            date = LocalDate.of(2026, 10, 3),
            legs = listOf(DutyLeg("Gdańsk", "Gdynia", "102", "ED160", "01"))
        )
        val d3 = Duty(
            id = 3,
            date = LocalDate.of(2026, 10, 5),
            legs = listOf(DutyLeg("Gdynia", "Warszawa", "103", "ED160", "99"))
        )
        val d4 = Duty(
            id = 4,
            date = LocalDate.of(2026, 10, 4),
            legs = listOf(DutyLeg("Katowice", "Kraków", "201", "ET42", "045"))
        )

        val duties = listOf(d1, d2, d3, d4)

        // Grouping logic as in DutyRepositoryImpl
        val vehicleTypeMap = mutableMapOf<String, MutableList<Duty>>()
        duties.forEach { duty ->
            duty.vehicleTypes.forEach { type ->
                if (type.isNotBlank() && type != "—") {
                    val list = vehicleTypeMap.getOrPut(type) { mutableListOf() }
                    list.add(duty)
                }
            }
        }

        // Exactly 2 vehicle types in the system: ED160 and ET42
        assertEquals(2, vehicleTypeMap.keys.size)
        assertTrue(vehicleTypeMap.containsKey("ED160"))
        assertTrue(vehicleTypeMap.containsKey("ET42"))
        assertFalse(vehicleTypeMap.containsKey("ED160-26"))
        assertFalse(vehicleTypeMap.containsKey("ED160-01"))
        assertFalse(vehicleTypeMap.containsKey("ET42-045"))

        // ED160 group contains all 3 duties
        val ed160Duties = vehicleTypeMap["ED160"]!!
        assertEquals(3, ed160Duties.size)

        // Latest trip date is 2026-10-05
        val latestDate = ed160Duties.maxOf { it.date }
        assertEquals(LocalDate.of(2026, 10, 5), latestDate)
    }

    @Test
    fun testNormalizingOlderUnformattedData() {
        // Test an older entry where series contains combined "ED160-26" and number is null
        val duty = Duty(
            id = 10,
            date = LocalDate.of(2026, 9, 20),
            vehicleSeries = "ED160-26",
            vehicleNumber = null
        )

        assertEquals(listOf("ED160"), duty.vehicleTypes)
        assertEquals("ED160", duty.displayVehiclesSummary)
    }
}
