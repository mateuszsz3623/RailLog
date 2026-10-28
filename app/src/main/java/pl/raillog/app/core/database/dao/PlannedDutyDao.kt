package pl.raillog.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pl.raillog.app.core.database.entity.PlannedDutyEntity
import java.time.LocalDate

@Dao
interface PlannedDutyDao {

    @Query("SELECT * FROM planned_duties ORDER BY date ASC, startAt ASC")
    fun getAll(): Flow<List<PlannedDutyEntity>>

    @Query("SELECT * FROM planned_duties WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PlannedDutyEntity?

    @Query("DELETE FROM planned_duties WHERE date >= :from AND date <= :to")
    suspend fun deleteRange(from: LocalDate, to: LocalDate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PlannedDutyEntity>)

    @Transaction
    suspend fun replaceRange(from: LocalDate, to: LocalDate, items: List<PlannedDutyEntity>) {
        deleteRange(from, to)
        insertAll(items)
    }

    @Query("DELETE FROM planned_duties")
    suspend fun clearAll()
}
