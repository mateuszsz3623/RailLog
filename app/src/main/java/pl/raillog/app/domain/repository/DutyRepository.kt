package pl.raillog.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyFilter
import pl.raillog.app.domain.model.StationKnowledge
import pl.raillog.app.domain.model.StatisticsData
import pl.raillog.app.domain.model.VehicleKnowledge
import java.time.YearMonth

data class RestoreOutcome(val count: Int, val autoBackupCreated: Boolean)

data class AutoBackupInfo(val timestamp: Long, val dutiesCount: Int)

interface DutyRepository {
    fun getAllDuties(): Flow<List<Duty>>
    fun getFilteredDuties(filter: DutyFilter): Flow<List<Duty>>
    fun getDutyById(id: Long): Flow<Duty?>
    suspend fun getDutyByIdDirect(id: Long): Duty?
    fun getLatestDuty(): Flow<Duty?>
    suspend fun saveDuty(duty: Duty): Long
    suspend fun deleteDuty(id: Long)
    fun getFrequentVehicleSeries(): Flow<List<String>>
    fun getRecentVehicles(): Flow<List<String>>
    fun getRecentRoutes(): Flow<List<List<String>>>
    fun getStatistics(selectedMonth: YearMonth? = null): Flow<StatisticsData>
    fun getStationKnowledge(query: String = ""): Flow<List<StationKnowledge>>
    fun getVehicleKnowledge(query: String = ""): Flow<List<VehicleKnowledge>>
    suspend fun exportCsv(): String
    suspend fun exportBackupJson(): String
    suspend fun restoreBackupJson(jsonString: String, overwrite: Boolean): Result<RestoreOutcome>
    suspend fun restoreLatestAutoBackup(): Result<RestoreOutcome>
    suspend fun latestAutoBackupInfo(): AutoBackupInfo?
}
