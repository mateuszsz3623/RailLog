package pl.raillog.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.raillog.app.core.database.dao.PlannedDutyDao
import pl.raillog.app.core.database.entity.PlannedDutyEntity
import pl.raillog.app.core.util.IcsEvent
import pl.raillog.app.domain.model.PlannedDuty
import pl.raillog.app.domain.model.PlannedKind
import pl.raillog.app.domain.model.toDomain
import pl.raillog.app.domain.repository.PlanImportSummary
import pl.raillog.app.domain.repository.PlanRepository
import java.time.ZoneId

class PlanRepositoryImpl(
    private val dao: PlannedDutyDao,
    private val clock: () -> Long = System::currentTimeMillis
) : PlanRepository {

    override fun getPlannedDuties(): Flow<List<PlannedDuty>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getPlannedById(id: Long): PlannedDuty? = dao.getById(id)?.toDomain()

    override suspend fun importPlan(events: List<IcsEvent>, zone: ZoneId): PlanImportSummary? {
        if (events.isEmpty()) return null
        val importedAt = clock()
        val items = events.map { event ->
            PlannedDutyEntity(
                uid = event.uid,
                code = event.summary,
                kind = if (event.description.equals("Rezerwa", ignoreCase = true)) {
                    PlannedKind.RESERVE.dbValue
                } else {
                    PlannedKind.DUTY.dbValue
                },
                date = event.start.atZone(zone).toLocalDate(),
                startAt = event.start.toEpochMilli(),
                endAt = event.end.toEpochMilli(),
                importedAt = importedAt
            )
        }
        val from = items.minOf { it.date }
        val to = items.maxOf { it.date }
        dao.replaceRange(from, to, items)
        return PlanImportSummary(items.size, from, to)
    }

    override suspend fun clearPlan() {
        dao.clearAll()
    }
}
