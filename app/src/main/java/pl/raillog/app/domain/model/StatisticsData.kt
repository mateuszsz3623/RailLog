package pl.raillog.app.domain.model

data class SeriesStatItem(
    val series: String,
    val count: Int,
    val percentage: Float
)

data class RouteStatItem(
    val route: String,
    val count: Int
)

data class MonthStatItem(
    val yearMonth: String, // e.g. "2026-10"
    val displayMonth: String, // e.g. "Październik 2026"
    val dutiesCount: Int,
    val overtimeMinutes: Int
)

data class StatisticsData(
    val totalDuties: Int = 0,
    val totalOvertimeMinutes: Int = 0,
    val currentMonthDuties: Int = 0,
    val currentMonthOvertimeMinutes: Int = 0,
    val seriesRanking: List<SeriesStatItem> = emptyList(),
    val topRoutes: List<RouteStatItem> = emptyList(),
    val monthlyHistory: List<MonthStatItem> = emptyList()
)
