package pl.raillog.app.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.raillog.app.core.database.dao.DutyDao
import pl.raillog.app.core.database.entity.DutyEntity
import pl.raillog.app.core.util.AutoBackupStore
import pl.raillog.app.core.util.BackupManager
import pl.raillog.app.core.util.CsvExporter
import pl.raillog.app.core.util.VehicleStringParser
import pl.raillog.app.domain.model.Duty
import pl.raillog.app.domain.model.DutyFilter
import pl.raillog.app.domain.model.DutyFilterType
import pl.raillog.app.domain.model.MonthStatItem
import pl.raillog.app.domain.model.RouteStatItem
import pl.raillog.app.domain.model.SeriesStatItem
import pl.raillog.app.domain.model.StationKnowledge
import pl.raillog.app.domain.model.StatisticsData
import pl.raillog.app.domain.model.VehicleKnowledge
import pl.raillog.app.domain.model.toDomain
import pl.raillog.app.domain.model.toEntity
import pl.raillog.app.domain.repository.AutoBackupInfo
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.domain.repository.RestoreOutcome
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class DutyRepositoryImpl(
    private val dutyDao: DutyDao,
    private val autoBackupStore: AutoBackupStore
) : DutyRepository {

    override fun getAllDuties(): Flow<List<Duty>> {
        return dutyDao.getAllDuties().map { list -> list.map { it.toDomain() } }
    }

    override fun getFilteredDuties(filter: DutyFilter): Flow<List<Duty>> {
        val baseFlow = if (filter.query.isBlank()) {
            dutyDao.getAllDuties()
        } else {
            dutyDao.searchDuties(filter.query.trim())
        }

        return baseFlow.map { list ->
            val now = LocalDate.now()
            list.filter { entity ->
                // Apply filterType
                val matchesType = when (filter.filterType) {
                    DutyFilterType.ALL -> true
                    DutyFilterType.OVERTIME_ONLY -> entity.overtimeMinutes > 0 || entity.isWholeDutyOvertime
                    DutyFilterType.THIS_MONTH -> entity.date.year == now.year && entity.date.monthValue == now.monthValue
                }

                // Apply selectedSeries
                val matchesSeries = if (filter.selectedSeries.isNullOrBlank()) {
                    true
                } else {
                    entity.vehicleSeries?.equals(filter.selectedSeries, ignoreCase = true) == true
                }

                matchesType && matchesSeries
            }.map { it.toDomain() }
        }
    }

    override fun getDutyById(id: Long): Flow<Duty?> {
        return dutyDao.getDutyById(id).map { it?.toDomain() }
    }

    override suspend fun getDutyByIdDirect(id: Long): Duty? {
        return dutyDao.getDutyByIdDirect(id)?.toDomain()
    }

    override fun getLatestDuty(): Flow<Duty?> {
        return dutyDao.getLatestDuty().map { it?.toDomain() }
    }

    override suspend fun saveDuty(duty: Duty): Long {
        val entity = duty.toEntity()
        return if (entity.id == 0L) {
            dutyDao.insertDuty(entity)
        } else {
            dutyDao.updateDuty(entity)
            entity.id
        }
    }

    override suspend fun deleteDuty(id: Long) {
        dutyDao.deleteDutyById(id)
    }

    override fun getFrequentVehicleSeries(): Flow<List<String>> {
        return dutyDao.getAllDuties().map { list ->
            val typeCounts = mutableMapOf<String, Int>()
            list.forEach { entity ->
                val duty = entity.toDomain()
                duty.vehicleTypes.forEach { type ->
                    if (type.isNotBlank() && type != "—") {
                        typeCounts[type] = (typeCounts[type] ?: 0) + 1
                    }
                }
            }
            typeCounts.entries.sortedByDescending { it.value }.map { it.key }.take(6)
        }
    }

    override fun getRecentVehicles(): Flow<List<String>> {
        return dutyDao.getAllDuties().map { list ->
            val vehicles = mutableListOf<String>()
            list.forEach { entity ->
                val duty = entity.toDomain()
                duty.vehicleTypes.forEach { v ->
                    if (v.isNotBlank() && v != "—" && !vehicles.contains(v)) {
                        vehicles.add(v)
                    }
                }
            }
            vehicles.take(8)
        }
    }

    override fun getRecentRoutes(): Flow<List<List<String>>> {
        return dutyDao.getAllDuties().map { list ->
            list.map { entity ->
                val duty = entity.toDomain()
                val stations = mutableListOf<String>()
                duty.legs.forEach { leg ->
                    if (stations.isEmpty() && leg.startStation.isNotBlank()) stations.add(leg.startStation)
                    if (leg.endStation.isNotBlank()) stations.add(leg.endStation)
                }
                stations
            }
            .filter { it.size >= 2 }
            .distinct()
            .take(4)
        }
    }

    override fun getStatistics(selectedMonth: YearMonth?): Flow<StatisticsData> {
        val polishMonthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl"))
        val now = LocalDate.now()

        return dutyDao.getAllDuties().map { list ->
            val allDuties = list.map { it.toDomain() }

            // Filter by selectedMonth if specified
            val targetDuties = if (selectedMonth != null) {
                allDuties.filter { it.date.year == selectedMonth.year && it.date.monthValue == selectedMonth.monthValue }
            } else {
                allDuties
            }

            val totalDuties = targetDuties.size
            val totalOvertimeMinutes = targetDuties.sumOf { it.overtimeMinutes }

            val currentMonthList = allDuties.filter {
                it.date.year == now.year && it.date.monthValue == now.monthValue
            }
            val currentMonthDuties = currentMonthList.size
            val currentMonthOvertime = currentMonthList.sumOf { it.overtimeMinutes }

            // Series ranking
            val seriesGroups = targetDuties.flatMap { duty ->
                val listSeries = mutableListOf<String>()
                duty.vehicleSeries?.takeIf { it.isNotBlank() }?.let { listSeries.add(it.trim().uppercase()) }
                duty.legs.forEach { leg ->
                    leg.vehicleSeries?.takeIf { it.isNotBlank() }?.let { listSeries.add(it.trim().uppercase()) }
                }
                listSeries.distinct()
            }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }

            val seriesRanking = seriesGroups.map { (series, count) ->
                SeriesStatItem(
                    series = series,
                    count = count,
                    percentage = if (totalDuties > 0) count.toFloat() / totalDuties.toFloat() else 0f
                )
            }

            // Route ranking
            val routeGroups = targetDuties.map { it.displayRoute }
                .filter { it.isNotBlank() && it != "—" }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            val topRoutes = routeGroups.map { (route, count) ->
                RouteStatItem(route = route, count = count)
            }

            // Monthly history for trend view
            val monthGroups = allDuties.groupBy { "${it.date.year}-${it.date.monthValue.toString().padStart(2, '0')}" }
                .toSortedMap(compareByDescending { it })

            val monthlyHistory = monthGroups.map { (yearMonth, monthDuties) ->
                val sampleDate = monthDuties.first().date
                MonthStatItem(
                    yearMonth = yearMonth,
                    displayMonth = sampleDate.format(polishMonthFormatter).replaceFirstChar { it.uppercase() },
                    dutiesCount = monthDuties.size,
                    overtimeMinutes = monthDuties.sumOf { it.overtimeMinutes }
                )
            }

            StatisticsData(
                totalDuties = totalDuties,
                totalOvertimeMinutes = totalOvertimeMinutes,
                currentMonthDuties = currentMonthDuties,
                currentMonthOvertimeMinutes = currentMonthOvertime,
                seriesRanking = seriesRanking,
                topRoutes = topRoutes,
                monthlyHistory = monthlyHistory
            )
        }
    }

    override fun getStationKnowledge(query: String): Flow<List<StationKnowledge>> {
        val today = LocalDate.now()
        val q = query.trim()

        return dutyDao.getAllDuties().map { entityList ->
            val duties = entityList.map { it.toDomain() }
            val stationMap = mutableMapOf<String, MutableList<Duty>>()

            duties.forEach { duty ->
                val stationsInDuty = mutableSetOf<String>()
                duty.legs.forEach { leg ->
                    if (leg.startStation.isNotBlank()) stationsInDuty.add(leg.startStation.trim())
                    if (leg.endStation.isNotBlank()) stationsInDuty.add(leg.endStation.trim())
                }
                stationsInDuty.forEach { station ->
                    val list = stationMap.getOrPut(station) { mutableListOf() }
                    list.add(duty)
                }
            }

            stationMap.map { (stationName, dutyList) ->
                val sortedDuties = dutyList.sortedByDescending { it.date }
                val lastDate = sortedDuties.first().date
                val daysAgo = ChronoUnit.DAYS.between(lastDate, today).coerceAtLeast(0)
                StationKnowledge(
                    stationName = stationName,
                    lastDate = lastDate,
                    daysAgo = daysAgo,
                    totalTrips = sortedDuties.size,
                    duties = sortedDuties
                )
            }
            .filter { if (q.isBlank()) true else it.stationName.contains(q, ignoreCase = true) }
            .sortedBy { it.daysAgo }
        }
    }

    override fun getVehicleKnowledge(query: String): Flow<List<VehicleKnowledge>> {
        val today = LocalDate.now()
        val q = query.trim()

        return dutyDao.getAllDuties().map { entityList ->
            val duties = entityList.map { it.toDomain() }
            val vehicleTypeMap = mutableMapOf<String, MutableList<Duty>>()

            duties.forEach { duty ->
                duty.vehicleTypes.forEach { type ->
                    if (type.isNotBlank() && type != "—") {
                        val list = vehicleTypeMap.getOrPut(type) { mutableListOf() }
                        list.add(duty)
                    }
                }
            }

            vehicleTypeMap.map { (vType, dutyList) ->
                val sortedDuties = dutyList.sortedByDescending { it.date }
                val lastDate = sortedDuties.first().date
                val daysAgo = ChronoUnit.DAYS.between(lastDate, today).coerceAtLeast(0)
                VehicleKnowledge(
                    vehicleType = vType,
                    lastDate = lastDate,
                    daysAgo = daysAgo,
                    totalDuties = sortedDuties.size,
                    duties = sortedDuties
                )
            }
            .filter { if (q.isBlank()) true else it.vehicleType.contains(q, ignoreCase = true) }
            .sortedBy { it.daysAgo }
        }
    }

    override suspend fun exportCsv(): String {
        val duties = dutyDao.getAllDutiesSnapshot().map { it.toDomain() }
        return CsvExporter.exportToCsv(duties)
    }

    override suspend fun exportBackupJson(): String {
        val duties = dutyDao.getAllDutiesSnapshot()
        return BackupManager.createBackupJson(duties)
    }

    override suspend fun restoreBackupJson(jsonString: String, overwrite: Boolean): Result<RestoreOutcome> {
        return try {
            val payload = BackupManager.parseBackupJson(jsonString).getOrElse { return Result.failure(it) }
            if (overwrite) {
                val backedUp = saveEmergencyBackupIfNeeded()
                dutyDao.replaceAllDuties(payload.duties)
                Result.success(RestoreOutcome(payload.duties.size, backedUp))
            } else {
                // Reset ID do 0 przy scalaniu, aby uniknąć kolizji
                val cleanedList = payload.duties.map { it.copy(id = 0) }
                dutyDao.insertAll(cleanedList)
                Result.success(RestoreOutcome(cleanedList.size, false))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Zapisuje bieżący stan jako kopię awaryjną (gdy baza nie jest pusta). Błąd zapisu = wyjątek, dane nietknięte. */
    private suspend fun saveEmergencyBackupIfNeeded(): Boolean {
        val current = dutyDao.getAllDutiesSnapshot()
        if (current.isEmpty()) return false
        withContext(Dispatchers.IO) {
            autoBackupStore.save(BackupManager.createBackupJson(current))
        }
        return true
    }

    override suspend fun restoreLatestAutoBackup(): Result<RestoreOutcome> {
        return try {
            val file = withContext(Dispatchers.IO) { autoBackupStore.latest() }
                ?: return Result.failure(Exception("Brak kopii awaryjnej do przywrócenia"))
            // Treść w pamięci przed nową kopią: rotacja mogłaby usunąć plik źródłowy.
            val json = withContext(Dispatchers.IO) { file.readText(Charsets.UTF_8) }
            val payload = BackupManager.parseBackupJson(json).getOrElse { return Result.failure(it) }
            val backedUp = saveEmergencyBackupIfNeeded()
            dutyDao.replaceAllDuties(payload.duties)
            withContext(Dispatchers.IO) { if (file.exists()) autoBackupStore.delete(file) }
            Result.success(RestoreOutcome(payload.duties.size, backedUp))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun latestAutoBackupInfo(): AutoBackupInfo? = withContext(Dispatchers.IO) {
        val file = autoBackupStore.latest() ?: return@withContext null
        val count = BackupManager.parseBackupJson(file.readText(Charsets.UTF_8)).getOrNull()?.dutiesCount ?: 0
        AutoBackupInfo(autoBackupStore.timestampOf(file), count)
    }
}
