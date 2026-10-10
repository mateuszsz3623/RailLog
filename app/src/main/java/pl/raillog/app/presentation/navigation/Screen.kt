package pl.raillog.app.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object Calendar : Screen("calendar")
    
    object DutyForm : Screen("duty_form?dutyId={dutyId}") {
        fun createRoute(dutyId: Long = 0L) = "duty_form?dutyId=$dutyId"
    }

    object DutyDetail : Screen("duty_detail/{dutyId}") {
        fun createRoute(dutyId: Long) = "duty_detail/$dutyId"
    }
}
