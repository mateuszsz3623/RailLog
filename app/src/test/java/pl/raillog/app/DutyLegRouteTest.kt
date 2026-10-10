package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyLeg
import java.time.LocalDate

class DutyLegRouteTest {

    @Test
    fun testDisplayRouteMultiLegsChained() {
        val legs = listOf(
            DutyLeg("Bydgoszcz", "Gdynia", "3560", "EU160", "26"),
            DutyLeg("Gdynia", "Piła", "7630", "EP09", "047")
        )
        val duty = Duty(
            date = LocalDate.of(2026, 10, 5),
            legs = legs
        )

        assertEquals("Bydgoszcz → Gdynia → Piła", duty.displayRoute)
        assertEquals("3560, 7630", duty.displayTrains)
        assertEquals("EU160 • EP09", duty.displayVehiclesSummary)
        assertEquals("EU160-26 • EP09-047", duty.displayAllVehicles)
    }

    @Test
    fun testNormalizedVehiclesSummaryDeduplication() {
        // As specifically requested in the prompt:
        // For legs handled by ED160-26, ED160-01, and ET42-045, display "ED160 • ET42"
        val legs = listOf(
            DutyLeg("Warszawa", "Koluszki", "101", "ED160", "26"),
            DutyLeg("Koluszki", "Łódź", "102", "ED160", "01"),
            DutyLeg("Łódź", "Kutno", "103", "ET42", "045")
        )
        val duty = Duty(
            date = LocalDate.of(2026, 10, 5),
            legs = legs
        )

        assertEquals(listOf("ED160", "ET42"), duty.vehicleTypes)
        assertEquals("ED160 • ET42", duty.displayVehiclesSummary)
        assertEquals("ED160-26 • ED160-01 • ET42-045", duty.displayAllVehicles)
    }

    @Test
    fun testDisplayRouteDisjointLegs() {
        val legs = listOf(
            DutyLeg("Bydgoszcz", "Toruń", "111", "EU200", "001"),
            DutyLeg("Inowrocław", "Poznań", "222", "EU200", "001")
        )
        val duty = Duty(
            date = LocalDate.of(2026, 10, 5),
            legs = legs
        )

        assertEquals("Bydgoszcz → Toruń → Inowrocław → Poznań", duty.displayRoute)
    }
}
