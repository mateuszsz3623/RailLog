package pl.raillog.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.raillog.app.core.util.IcsEvent
import pl.raillog.app.domain.model.PlannedDuty
import java.time.LocalDate
import java.time.ZoneId

data class PlanImportSummary(val count: Int, val from: LocalDate, val to: LocalDate)

interface PlanRepository {
    fun getPlannedDuties(): Flow<List<PlannedDuty>>
    suspend fun getPlannedById(id: Long): PlannedDuty?

    /** Podmienia plan w zakresie dat wczytanych zdarzeń. Null, gdy brak zdarzeń. Zapisanych służb nie rusza. */
    suspend fun importPlan(events: List<IcsEvent>, zone: ZoneId = ZoneId.systemDefault()): PlanImportSummary?
    suspend fun clearPlan()
}
