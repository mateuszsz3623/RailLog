package pl.raillog.app.domain.model

enum class DutyFilterType {
    ALL,
    OVERTIME_ONLY,
    THIS_MONTH
}

data class DutyFilter(
    val query: String = "",
    val filterType: DutyFilterType = DutyFilterType.ALL,
    val selectedSeries: String? = null
)
