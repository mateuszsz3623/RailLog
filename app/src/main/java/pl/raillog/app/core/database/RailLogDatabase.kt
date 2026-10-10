package pl.raillog.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pl.raillog.app.core.database.dao.DutyDao
import pl.raillog.app.core.database.entity.DutyEntity
import pl.raillog.app.core.util.VehicleStringParser

@Database(
    entities = [DutyEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RailLogDatabase : RoomDatabase() {

    abstract fun dutyDao(): DutyDao

    companion object {
        @Volatile
        private var INSTANCE: RailLogDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE duties ADD COLUMN startTime TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE duties ADD COLUMN endTime TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE duties ADD COLUMN dutyDurationMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE duties ADD COLUMN legs TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Normalize existing vehicle data in database: separate series/type and instance number
                try {
                    val cursor = db.query("SELECT id, vehicleSeries, vehicleNumber FROM duties")
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(0)
                        val series = cursor.getString(1)
                        val number = cursor.getString(2)
                        if (!series.isNullOrBlank()) {
                            val (normSeries, normNumber) = VehicleStringParser.normalizeSeriesAndNumber(series, number)
                            if (normSeries != series || normNumber != (number ?: "")) {
                                db.execSQL(
                                    "UPDATE duties SET vehicleSeries = ?, vehicleNumber = ? WHERE id = ?",
                                    arrayOf(normSeries.ifBlank { null }, normNumber.ifBlank { null }, id)
                                )
                            }
                        }
                    }
                    cursor.close()
                } catch (e: Exception) {
                    // Safe fallback: normalization is also handled dynamically in domain mappers
                }
            }
        }

        fun getDatabase(context: Context): RailLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RailLogDatabase::class.java,
                    "raillog.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
