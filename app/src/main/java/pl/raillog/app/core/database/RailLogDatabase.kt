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

        // Eksperymentalny, nigdy niezcommitowany build z 26.10 podbił schemat bazy do wersji 4
        // na części urządzeń. Aplikacja zostaje przy wersji 3, a ta migracja (downgrade) sprowadza
        // bazę v4 z powrotem do dokładnego schematu v3, zachowując dane użytkownika (służby).
        val MIGRATION_4_3 = object : Migration(4, 3) {
            // Dokładna lista kolumn i SQL z wygenerowanej implementacji Rooma
            // (RailLogDatabase_Impl.createAllTables) — Room waliduje schemat po migracji.
            private val v3Columns = listOf(
                "id", "date", "vehicleSeries", "vehicleNumber", "startTime", "endTime",
                "dutyDurationMinutes", "legs", "routeSegments", "trainNumbers",
                "overtimeMinutes", "isWholeDutyOvertime", "notes", "additionalNotes",
                "createdAt", "updatedAt"
            )

            private val v3CreateTmpTableSql =
                "CREATE TABLE `duties_v3_tmp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`date` TEXT NOT NULL, `vehicleSeries` TEXT, `vehicleNumber` TEXT, " +
                    "`startTime` TEXT, `endTime` TEXT, `dutyDurationMinutes` INTEGER NOT NULL, " +
                    "`legs` TEXT NOT NULL, `routeSegments` TEXT NOT NULL, `trainNumbers` TEXT NOT NULL, " +
                    "`overtimeMinutes` INTEGER NOT NULL, `isWholeDutyOvertime` INTEGER NOT NULL, " +
                    "`notes` TEXT, `additionalNotes` TEXT, `createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL)"

            private val v3IndexSql = listOf(
                "CREATE INDEX IF NOT EXISTS `index_duties_date` ON `duties` (`date`)",
                "CREATE INDEX IF NOT EXISTS `index_duties_vehicleSeries` ON `duties` (`vehicleSeries`)",
                "CREATE INDEX IF NOT EXISTS `index_duties_createdAt` ON `duties` (`createdAt`)"
            )

            override fun migrate(db: SupportSQLiteDatabase) {
                // a) Usuń obce tabele ewentualnie dodane przez eksperyment v4
                val foreignTables = mutableListOf<String>()
                db.query(
                    "SELECT name FROM sqlite_master WHERE type = 'table' " +
                        "AND name NOT LIKE 'sqlite_%' " +
                        "AND name NOT IN ('duties', 'room_master_table')"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        foreignTables.add(cursor.getString(0))
                    }
                }
                for (table in foreignTables) {
                    db.execSQL("DROP TABLE IF EXISTS `${table.replace("`", "``")}`")
                }

                // b) Znormalizuj duties do dokładnego zestawu kolumn v3
                val existingColumns = mutableListOf<String>()
                db.query("PRAGMA table_info(duties)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        existingColumns.add(cursor.getString(nameIndex))
                    }
                }

                if (!existingColumns.toSet().containsAll(v3Columns)) {
                    val missing = v3Columns.filter { it !in existingColumns.toSet() }
                    throw IllegalStateException(
                        "MIGRATION_4_3: tabela duties nie zawiera kolumn wersji 3: $missing " +
                            "(znalezione kolumny: $existingColumns). " +
                            "Przerywam migrację, aby nie zgadywać nieznanego schematu."
                    )
                }

                if (existingColumns.toSet() != v3Columns.toSet()) {
                    // duties ma dodatkowe kolumny (nadzbiór v3) → odtwórz tabelę w schemacie v3,
                    // przenosząc wszystkie wiersze
                    db.execSQL("DROP TABLE IF EXISTS `duties_v3_tmp`")
                    db.execSQL(v3CreateTmpTableSql)
                    val columnList = v3Columns.joinToString(", ") { "`$it`" }
                    db.execSQL(
                        "INSERT INTO `duties_v3_tmp` ($columnList) SELECT $columnList FROM `duties`"
                    )
                    db.execSQL("DROP TABLE `duties`")
                    db.execSQL("ALTER TABLE `duties_v3_tmp` RENAME TO `duties`")
                } else {
                    // Kolumny już się zgadzają (najczęstszy przypadek: eksperyment zmienił
                    // tylko numer wersji) — usuń ewentualne dodatkowe indeksy na duties,
                    // aby walidacja schematu Rooma przeszła
                    val expectedIndices = setOf(
                        "index_duties_date",
                        "index_duties_vehicleSeries",
                        "index_duties_createdAt"
                    )
                    val extraIndices = mutableListOf<String>()
                    db.query("PRAGMA index_list(`duties`)").use { cursor ->
                        val nameIndex = cursor.getColumnIndexOrThrow("name")
                        val originIndex = cursor.getColumnIndexOrThrow("origin")
                        while (cursor.moveToNext()) {
                            // origin 'c' = indeks z CREATE INDEX ('u'/'pk' to automatyczne)
                            val name = cursor.getString(nameIndex)
                            if (cursor.getString(originIndex) == "c" && name !in expectedIndices) {
                                extraIndices.add(name)
                            }
                        }
                    }
                    for (indexName in extraIndices) {
                        db.execSQL("DROP INDEX IF EXISTS `${indexName.replace("`", "``")}`")
                    }
                }

                // c) Odtwórz indeksy v3 dokładnie tak, jak generuje je Room
                v3IndexSql.forEach { db.execSQL(it) }
            }
        }

        fun getDatabase(context: Context): RailLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RailLogDatabase::class.java,
                    "raillog.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_4_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
