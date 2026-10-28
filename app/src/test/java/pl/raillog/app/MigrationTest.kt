package pl.raillog.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.raillog.app.core.database.RailLogDatabase
import java.io.File
import java.time.LocalDate

/**
 * Migracje sprawdzane na surowym SQL (bez MigrationTestHelper). Room sam waliduje schemat
 * po migracji, więc otwarcie bazy i odczyt przechodzą tylko przy poprawnej migracji.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    private lateinit var context: Context
    private lateinit var dbFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbFile = context.getDatabasePath(DB_NAME)
        dbFile.parentFile?.mkdirs()
        context.deleteDatabase(DB_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DB_NAME)
    }

    private fun createDutiesTable(db: SQLiteDatabase, extraColumn: Boolean) {
        db.execSQL(
            "CREATE TABLE `duties` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`date` TEXT NOT NULL, `vehicleSeries` TEXT, `vehicleNumber` TEXT, " +
                "`startTime` TEXT, `endTime` TEXT, `dutyDurationMinutes` INTEGER NOT NULL, " +
                "`legs` TEXT NOT NULL, `routeSegments` TEXT NOT NULL, `trainNumbers` TEXT NOT NULL, " +
                "`overtimeMinutes` INTEGER NOT NULL, `isWholeDutyOvertime` INTEGER NOT NULL, " +
                "`notes` TEXT, `additionalNotes` TEXT, `createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL" + (if (extraColumn) ", `extraCol` TEXT" else "") + ")"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_duties_date` ON `duties` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_duties_vehicleSeries` ON `duties` (`vehicleSeries`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_duties_createdAt` ON `duties` (`createdAt`)")
    }

    private fun insertDuty(db: SQLiteDatabase, date: String, series: String, notes: String) {
        db.execSQL(
            "INSERT INTO duties (date, vehicleSeries, vehicleNumber, startTime, endTime, dutyDurationMinutes, " +
                "legs, routeSegments, trainNumbers, overtimeMinutes, isWholeDutyOvertime, notes, additionalNotes, " +
                "createdAt, updatedAt) VALUES (?, ?, '001', '06:00', '14:00', 480, '[]', '[\"A\",\"B\"]', " +
                "'[\"123\"]', 20, 0, ?, NULL, 1000, 2000)",
            arrayOf<Any?>(date, series, notes)
        )
    }

    /** Baza „v4": schemat duties jak w v3 (+ opcjonalna kolumna), obca tabela foo, user_version = 4. */
    private fun createVersion4Database(extraColumn: Boolean) {
        val raw = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        try {
            createDutiesTable(raw, extraColumn)
            raw.execSQL("CREATE TABLE `foo` (`id` INTEGER PRIMARY KEY, `x` TEXT)")
            insertDuty(raw, "2026-01-10", "EU07", "pierwsza")
            insertDuty(raw, "2026-02-11", "ET22", "druga")
            raw.execSQL("PRAGMA user_version = 4")
        } finally {
            raw.close()
        }
    }

    private fun openRoom(): RailLogDatabase =
        Room.databaseBuilder(context, RailLogDatabase::class.java, DB_NAME)
            .addMigrations(
                RailLogDatabase.MIGRATION_1_2,
                RailLogDatabase.MIGRATION_2_3,
                RailLogDatabase.MIGRATION_4_3
            )
            .allowMainThreadQueries()
            .build()

    private fun assertDutiesKept(db: RailLogDatabase) = runBlocking {
        val duties = db.dutyDao().getAllDutiesSnapshot()
        assertEquals(2, duties.size)
        assertEquals(LocalDate.of(2026, 1, 10), duties[0].date)
        assertEquals("EU07", duties[0].vehicleSeries)
        assertEquals("pierwsza", duties[0].notes)
        assertEquals(listOf("A", "B"), duties[0].routeSegments)
        assertEquals(listOf("123"), duties[0].trainNumbers)
        assertEquals(480, duties[0].dutyDurationMinutes)
        assertEquals(20, duties[0].overtimeMinutes)
        assertEquals(LocalDate.of(2026, 2, 11), duties[1].date)
        assertEquals("ET22", duties[1].vehicleSeries)
        assertEquals("druga", duties[1].notes)
    }

    @Test
    fun migration4to3KeepsDataWhenTableHasExtraColumn() {
        createVersion4Database(extraColumn = true)
        val db = openRoom()
        try {
            assertDutiesKept(db)
        } finally {
            db.close()
        }
    }

    @Test
    fun migration4to3KeepsDataWhenOnlyVersionAndForeignTableDiffer() {
        createVersion4Database(extraColumn = false)
        val db = openRoom()
        try {
            assertDutiesKept(db)
        } finally {
            db.close()
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
