package pl.raillog.app

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.raillog.app.core.database.dao.DutyDao
import pl.raillog.app.core.database.dao.RecentVehicleTuple
import pl.raillog.app.core.database.dao.VehicleCountTuple
import pl.raillog.app.core.database.entity.DutyEntity
import pl.raillog.app.core.util.AutoBackupStore
import pl.raillog.app.core.util.BackupManager
import pl.raillog.app.data.repository.DutyRepositoryImpl
import java.time.LocalDate

private class FakeDutyDao(initial: List<DutyEntity>) : DutyDao {
    var data: List<DutyEntity> = initial

    override fun getAllDuties(): Flow<List<DutyEntity>> = flowOf(data)
    override fun getDutyById(id: Long): Flow<DutyEntity?> = flowOf(data.firstOrNull { it.id == id })
    override suspend fun getDutyByIdDirect(id: Long): DutyEntity? = data.firstOrNull { it.id == id }
    override fun getLatestDuty(): Flow<DutyEntity?> = flowOf(data.firstOrNull())
    override suspend fun insertDuty(duty: DutyEntity): Long {
        data = data + duty
        return duty.id
    }
    override suspend fun insertAll(duties: List<DutyEntity>) {
        data = data + duties
    }
    override suspend fun updateDuty(duty: DutyEntity) {
        data = data.map { if (it.id == duty.id) duty else it }
    }
    override suspend fun deleteDuty(duty: DutyEntity) {
        data = data.filter { it.id != duty.id }
    }
    override suspend fun deleteDutyById(id: Long) {
        data = data.filter { it.id != id }
    }
    override suspend fun deleteAllDuties() {
        data = emptyList()
    }
    override suspend fun replaceAllDuties(duties: List<DutyEntity>) {
        data = duties
    }
    override fun getFrequentVehicleSeries(limit: Int): Flow<List<String>> = flowOf(emptyList())
    override fun getRecentVehicles(limit: Int): Flow<List<RecentVehicleTuple>> = flowOf(emptyList())
    override fun getVehicleSeriesStatistics(): Flow<List<VehicleCountTuple>> = flowOf(emptyList())
    override suspend fun getAllDutiesSnapshot(): List<DutyEntity> = data
}

class RestoreBackupTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val oldDuties = listOf(
        DutyEntity(id = 1, date = LocalDate.of(2026, 1, 1), vehicleSeries = "EU07"),
        DutyEntity(id = 2, date = LocalDate.of(2026, 1, 2), vehicleSeries = "ET22")
    )
    private val newDuties = listOf(
        DutyEntity(id = 10, date = LocalDate.of(2026, 5, 5), vehicleSeries = "EN57")
    )

    private fun backupDir() = java.io.File(tmp.root, "auto")

    @Test
    fun invalidJsonLeavesDataAndCreatesNoAutoBackup() = runBlocking {
        val dao = FakeDutyDao(oldDuties)
        val store = AutoBackupStore(backupDir())
        val repo = DutyRepositoryImpl(dao, store)

        val result = repo.restoreBackupJson("to nie jest json", overwrite = true)

        assertTrue(result.isFailure)
        assertEquals(oldDuties, dao.data)
        assertNull(store.latest())
    }

    @Test
    fun overwriteSavesOldDataToAutoBackupAndStoresNew() = runBlocking {
        val dao = FakeDutyDao(oldDuties)
        val store = AutoBackupStore(backupDir())
        val repo = DutyRepositoryImpl(dao, store)

        val result = repo.restoreBackupJson(BackupManager.createBackupJson(newDuties), overwrite = true)

        assertEquals(1, result.getOrNull()?.count)
        assertTrue(result.getOrNull()!!.autoBackupCreated)
        assertEquals(newDuties, dao.data)
        val saved = BackupManager.parseBackupJson(store.latest()!!.readText()).getOrThrow()
        assertEquals(oldDuties, saved.duties)
    }

    @Test
    fun failedAutoBackupKeepsOldData() = runBlocking {
        val dao = FakeDutyDao(oldDuties)
        val notADir = tmp.newFile("zwykly_plik")
        val repo = DutyRepositoryImpl(dao, AutoBackupStore(notADir))

        val result = repo.restoreBackupJson(BackupManager.createBackupJson(newDuties), overwrite = true)

        assertTrue(result.isFailure)
        assertEquals(oldDuties, dao.data)
    }

    @Test
    fun restoreLatestAutoBackupRestoresOldDataAndDeletesFile() = runBlocking {
        val dao = FakeDutyDao(oldDuties)
        val store = AutoBackupStore(backupDir())
        val repo = DutyRepositoryImpl(dao, store)
        repo.restoreBackupJson(BackupManager.createBackupJson(newDuties), overwrite = true)
        val file = store.latest()!!

        val result = repo.restoreLatestAutoBackup()

        assertEquals(2, result.getOrNull()?.count)
        assertEquals(oldDuties, dao.data)
        assertFalse(file.exists())
    }

    @Test
    fun restoreLatestAutoBackupSavesCurrentStateAsNewBackup() = runBlocking {
        val dao = FakeDutyDao(oldDuties)
        var now = 1000L
        val store = AutoBackupStore(backupDir(), clock = { now })
        val repo = DutyRepositoryImpl(dao, store)
        repo.restoreBackupJson(BackupManager.createBackupJson(newDuties), overwrite = true)
        now = 2000L

        val result = repo.restoreLatestAutoBackup()

        assertTrue(result.getOrNull()!!.autoBackupCreated)
        val info = repo.latestAutoBackupInfo()!!
        assertEquals(2000L, info.timestamp)
        assertEquals(1, info.dutiesCount)
        val saved = BackupManager.parseBackupJson(store.latest()!!.readText()).getOrThrow()
        assertEquals(newDuties, saved.duties)
    }

    @Test
    fun restoreLatestAutoBackupWorksWhenRotationRemovedSourceFile() = runBlocking {
        val dao = FakeDutyDao(oldDuties)
        var now = 1000L
        val store = AutoBackupStore(backupDir(), maxFiles = 1, clock = { now })
        val repo = DutyRepositoryImpl(dao, store)
        repo.restoreBackupJson(BackupManager.createBackupJson(newDuties), overwrite = true)
        now = 2000L

        val result = repo.restoreLatestAutoBackup()

        assertEquals(2, result.getOrNull()?.count)
        assertEquals(oldDuties, dao.data)
        assertEquals(1, store.latest()?.let { 1 })
    }

    @Test
    fun restoreOnEmptyDatabaseCreatesNoAutoBackup() = runBlocking {
        val dao = FakeDutyDao(emptyList())
        val store = AutoBackupStore(backupDir())
        val repo = DutyRepositoryImpl(dao, store)

        val result = repo.restoreBackupJson(BackupManager.createBackupJson(newDuties), overwrite = true)

        assertEquals(false, result.getOrNull()?.autoBackupCreated)
        assertNull(store.latest())
    }

    @Test
    fun mergeSkipsDutiesIdenticalToExistingOnes() = runBlocking {
        val dao = FakeDutyDao(oldDuties)
        val repo = DutyRepositoryImpl(dao, AutoBackupStore(backupDir()))
        val incoming = oldDuties.take(1).map { it.copy(id = 77) } + newDuties

        val result = repo.restoreBackupJson(BackupManager.createBackupJson(incoming), overwrite = false)

        val outcome = result.getOrThrow()
        assertEquals(1, outcome.count)
        assertEquals(1, outcome.skippedDuplicates)
        assertEquals(3, dao.data.size)
    }
}
