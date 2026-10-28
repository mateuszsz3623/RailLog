package pl.raillog.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyLeg
import pl.raillog.app.domain.model.matchesQuery
import java.time.LocalDate

class DutyQueryFilterTest {

    private val duty = Duty(
        date = LocalDate.of(2026, 3, 3),
        legs = listOf(
            DutyLeg(
                startStation = "Łódź Kaliska",
                endStation = "Warszawa Zachodnia",
                trainNumber = "5622",
                vehicleSeries = "EU160",
                vehicleNumber = "025"
            )
        ),
        notes = "Opóźnienie przez awarię"
    )

    @Test
    fun blankQueryMatchesEverything() {
        assertTrue(duty.matchesQuery(""))
        assertTrue(duty.matchesQuery("   "))
    }

    @Test
    fun matchesStationsWithPolishLettersIgnoringCase() {
        assertTrue(duty.matchesQuery("Łódź"))
        assertTrue(duty.matchesQuery("łódź"))
        assertTrue(duty.matchesQuery("warszawa"))
    }

    @Test
    fun matchesTrainVehicleAndNotes() {
        assertTrue(duty.matchesQuery("5622"))
        assertTrue(duty.matchesQuery("eu160"))
        assertTrue(duty.matchesQuery("EU160-025"))
        assertTrue(duty.matchesQuery("awarię"))
    }

    @Test
    fun doesNotMatchUnrelatedText() {
        assertFalse(duty.matchesQuery("Kraków"))
        assertFalse(duty.matchesQuery("ET22"))
    }
}
