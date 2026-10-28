package pl.raillog.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.raillog.app.core.database.entity.DutyEntity

data class RecentVehicleTuple(
    val vehicleSeries: String?,
    val vehicleNumber: String?
)

data class VehicleCountTuple(
    val vehicleSeries: String,
    val count: Int
)

@Dao
interface DutyDao {

    @Query("SELECT * FROM duties ORDER BY date DESC, id DESC")
    fun getAllDuties(): Flow<List<DutyEntity>>

    @Query("SELECT * FROM duties WHERE id = :id LIMIT 1")
    fun getDutyById(id: Long): Flow<DutyEntity?>

    @Query("SELECT * FROM duties WHERE id = :id LIMIT 1")
    suspend fun getDutyByIdDirect(id: Long): DutyEntity?

    @Query("SELECT * FROM duties ORDER BY date DESC, id DESC LIMIT 1")
    fun getLatestDuty(): Flow<DutyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuty(duty: DutyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(duties: List<DutyEntity>)

    @Update
    suspend fun updateDuty(duty: DutyEntity)

    @Delete
    suspend fun deleteDuty(duty: DutyEntity)

    @Query("DELETE FROM duties WHERE id = :id")
    suspend fun deleteDutyById(id: Long)

    @Query("DELETE FROM duties")
    suspend fun deleteAllDuties()

    @Transaction
    suspend fun replaceAllDuties(duties: List<DutyEntity>) {
        deleteAllDuties()
        insertAll(duties)
    }

    @Query("""
        SELECT vehicleSeries 
        FROM duties 
        WHERE vehicleSeries IS NOT NULL AND vehicleSeries != '' 
        GROUP BY vehicleSeries 
        ORDER BY COUNT(*) DESC 
        LIMIT :limit
    """)
    fun getFrequentVehicleSeries(limit: Int = 5): Flow<List<String>>

    @Query("""
        SELECT DISTINCT vehicleSeries, vehicleNumber 
        FROM duties 
        WHERE vehicleSeries IS NOT NULL AND vehicleSeries != '' 
        ORDER BY date DESC, id DESC 
        LIMIT :limit
    """)
    fun getRecentVehicles(limit: Int = 8): Flow<List<RecentVehicleTuple>>

    @Query("""
        SELECT vehicleSeries, COUNT(*) as count 
        FROM duties 
        WHERE vehicleSeries IS NOT NULL AND vehicleSeries != '' 
        GROUP BY vehicleSeries 
        ORDER BY count DESC
    """)
    fun getVehicleSeriesStatistics(): Flow<List<VehicleCountTuple>>

    @Query("SELECT * FROM duties ORDER BY date ASC, id ASC")
    suspend fun getAllDutiesSnapshot(): List<DutyEntity>
}
