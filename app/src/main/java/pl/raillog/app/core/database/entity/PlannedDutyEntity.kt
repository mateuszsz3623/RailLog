package pl.raillog.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Służba z zaimportowanego planu (.ics). Tylko podpowiedź – nie jest częścią kopii JSON. */
@Entity(
    tableName = "planned_duties",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["date"])
    ]
)
data class PlannedDutyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uid: String,
    // Surowe SUMMARY z pliku, np. "MBY135" albo "Jazda EU-200"
    val code: String,
    // "SLUZBA" albo "REZERWA"
    val kind: String,
    // Dzień startu w strefie urządzenia
    val date: LocalDate,
    val startAt: Long,
    val endAt: Long,
    val importedAt: Long
)
