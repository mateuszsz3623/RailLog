package pl.raillog.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.raillog.app.core.database.RailLogDatabase
import pl.raillog.app.core.database.dao.DutyDao
import pl.raillog.app.core.database.entity.DutyEntity
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DutyDaoTest {

    private lateinit var db: RailLogDatabase
    private lateinit var dao: DutyDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RailLogDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dutyDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun duty(date: LocalDate, series: String = "EU07") =
        DutyEntity(date = date, vehicleSeries = series, notes = "n-$date")

    @Test
    fun insertAndGetById() = runBlocking {
        val id = dao.insertDuty(duty(LocalDate.of(2026, 3, 1)))

        val loaded = dao.getDutyByIdDirect(id)

        assertNotNull(loaded)
        assertEquals(LocalDate.of(2026, 3, 1), loaded!!.date)
        assertEquals("EU07", loaded.vehicleSeries)
        assertEquals("n-2026-03-01", loaded.notes)
    }

    @Test
    fun replaceAllDutiesReplacesWholeContent() = runBlocking {
        dao.insertAll(listOf(duty(LocalDate.of(2026, 1, 1)), duty(LocalDate.of(2026, 1, 2))))

        dao.replaceAllDuties(listOf(duty(LocalDate.of(2026, 5, 5), "ET22")))

        val all = dao.getAllDutiesSnapshot()
        assertEquals(1, all.size)
        assertEquals("ET22", all.single().vehicleSeries)
    }

    @Test
    fun snapshotIsOrderedAscendingByDate() = runBlocking {
        dao.insertAll(
            listOf(
                duty(LocalDate.of(2026, 3, 10)),
                duty(LocalDate.of(2026, 1, 5)),
                duty(LocalDate.of(2026, 2, 7))
            )
        )

        val dates = dao.getAllDutiesSnapshot().map { it.date }

        assertEquals(
            listOf(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 7), LocalDate.of(2026, 3, 10)),
            dates
        )
    }

    @Test
    fun deleteDutyByIdRemovesOnlyThatDuty() = runBlocking {
        val first = dao.insertDuty(duty(LocalDate.of(2026, 1, 1)))
        val second = dao.insertDuty(duty(LocalDate.of(2026, 1, 2)))

        dao.deleteDutyById(first)

        assertNull(dao.getDutyByIdDirect(first))
        assertNotNull(dao.getDutyByIdDirect(second))
    }
}
