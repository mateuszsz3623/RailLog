package pl.raillog.app

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.raillog.app.core.network.LiveTrain
import pl.raillog.app.core.network.PlkApiClient
import pl.raillog.app.core.network.PlkHttp
import pl.raillog.app.core.network.PlkReply
import pl.raillog.app.core.network.PlkResult
import pl.raillog.app.core.network.TrainSearch
import java.io.File
import java.io.IOException
import java.time.LocalDate

class PlkTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val day = LocalDate.of(2026, 10, 28)

    private val scheduleJson = """
    {
      "routes": [
        {
          "scheduleId": 2026, "orderId": 801294767, "name": "ZAMOYSKI", "carrierCode": "IC",
          "nationalNumber": "8034", "commercialCategorySymbol": "IC",
          "operatingDates": ["2026-10-28"],
          "stations": [
            {"stationId": 4101, "orderNumber": 1, "departureTrainNumber": "8034", "departureTime": "05:38:00", "departurePlatform": "1", "departureTrack": "4"},
            {"stationId": 3004, "orderNumber": 13, "arrivalTrainNumber": "8034", "arrivalTime": "07:48:00", "departureTrainNumber": "8035", "departureTime": "07:49:00"},
            {"stationId": 5000, "orderNumber": 20, "arrivalTrainNumber": "8035", "arrivalTime": "09:10:00"}
          ]
        },
        {
          "scheduleId": 2026, "orderId": 111, "name": "INNY", "carrierCode": "IC",
          "nationalNumber": "5622", "operatingDates": ["2026-10-27"],
          "stations": [{"stationId": 4101, "orderNumber": 1, "departureTrainNumber": "5622", "departureTime": "10:00:00"}]
        }
      ],
      "dictionaries": {"stations": {"4101": {"id": 4101, "name": "Stacja A"}, "3004": {"id": 3004, "name": "Stacja B"}, "5000": {"id": 5000, "name": "Stacja C"}}}
    }
    """.trimIndent()

    @Test
    fun numberPairCoversBothDirections() {
        assertEquals(setOf("5622", "5623"), TrainSearch.numberPair("5622"))
        assertEquals(setOf("5622", "5623"), TrainSearch.numberPair("IC 5623"))
        assertEquals(null, TrainSearch.numberPair("abc"))
    }

    @Test
    fun findsTrainByChangedNumber() {
        listOf("8034", "8035").forEach { input ->
            val found = TrainSearch.find(scheduleJson, input, day)
            assertEquals(input, 1, found.size)
            assertEquals("ZAMOYSKI", found[0].name)
            assertEquals("Stacja A", found[0].firstStation)
            assertEquals("Stacja C", found[0].lastStation)
            assertEquals("1", found[0].stops[0].platform)
        }
    }

    @Test
    fun respectsOperatingDate() {
        assertTrue(TrainSearch.find(scheduleJson, "5622", day).isEmpty())
        assertEquals(1, TrainSearch.find(scheduleJson, "5623", day.minusDays(1)).size)
    }

    @Test
    fun liveParserAcceptsDirectAndWrappedObjects() {
        val train = """{"trainStatus":"P","stations":[{"stationId":1,"plannedSequenceNumber":1,"actualSequenceNumber":1,"plannedDeparture":"2026-10-28T14:00:00","actualDeparture":"2026-10-28T14:12:00","departureDelayMinutes":12,"isConfirmed":true,"isCancelled":false}]}"""
        listOf(train, """{"trains":[$train]}""", """{"train":$train}""").forEach {
            val live = LiveTrain.parse(it, mapOf(1 to "Stacja A"))
            assertNotNull(live)
            assertEquals("Opóźniony +12 min", live!!.statusText)
            assertEquals("Stacja A", live.lastConfirmed?.name)
        }
    }

    @Test
    fun liveParserComputesDelayWhenMissing() {
        val json = """{"trainStatus":"P","stations":[{"stationId":1,"actualSequenceNumber":1,"plannedArrival":"2026-10-28T14:00:00","actualArrival":"2026-10-28T14:03:00","isConfirmed":true}]}"""

        val live = LiveTrain.parse(json)!!

        assertEquals(3, live.stops[0].arrivalDelayMinutes)
        assertEquals("Opóźniony +3 min", live.statusText)
    }

    @Test
    fun statusTexts() {
        assertEquals("Odwołany", LiveTrain("X", emptyList()).statusText)
        assertEquals("Nie wyruszył", LiveTrain("S", emptyList()).statusText)
        assertEquals("Zakończył kurs", LiveTrain("F", emptyList()).statusText)
        assertEquals("Planowo", LiveTrain("P", emptyList()).statusText)
    }

    private class FakeHttp(private val reply: () -> PlkReply) : PlkHttp {
        var calls = 0
        override suspend fun get(url: String): PlkReply {
            calls++
            return reply()
        }
    }

    private fun client(http: PlkHttp, dir: File = tmp.root) = PlkApiClient(http, dir) { day }

    @Test
    fun scheduleIsCachedForTheDay() = runBlocking {
        val http = FakeHttp { PlkReply(200, scheduleJson, 90) }
        val client = client(http)

        val first = client.scheduleJson(day)
        val second = client.scheduleJson(day)

        assertTrue(first is PlkResult.Success)
        assertTrue(second is PlkResult.Success)
        assertEquals(1, http.calls)
    }

    @Test
    fun mapsHttpErrorsToMessages() = runBlocking {
        suspend fun message(code: Int) =
            (client(FakeHttp { PlkReply(code, "", null) }, tmp.newFolder()).liveTrainJson(1, 2, day) as PlkResult.Failure).message

        assertEquals("Klucz API nieprawidłowy", message(401))
        assertEquals("Klucz API nieprawidłowy", message(403))
        assertEquals("Wyczerpany limit zapytań (100/h) — spróbuj później", message(429))
        assertEquals("Błąd serwera PLK (500)", message(500))
    }

    @Test
    fun noInternetIsReported() = runBlocking {
        val result = client(FakeHttp { throw IOException("offline") }, tmp.newFolder()).liveTrainJson(1, 2, day)

        assertEquals("Brak połączenia z internetem", (result as PlkResult.Failure).message)
    }

    @Test
    fun oldCacheFilesAreRemoved() = runBlocking {
        val dir = tmp.newFolder()
        val plk = File(dir, "plk").also { it.mkdirs() }
        val old = File(plk, "schedules_2026-10-20_IC.json").also { it.writeText("{}") }
        val client = client(FakeHttp { PlkReply(200, scheduleJson, null) }, dir)

        client.scheduleJson(day)

        assertTrue(!old.exists())
    }
}
