package pl.raillog.app.presentation.navigation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import pl.raillog.app.core.designsystem.AppThemeMode
import pl.raillog.app.core.network.PlkApiClient
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.domain.repository.PlanRepository
import pl.raillog.app.presentation.calendar.CalendarScreen
import pl.raillog.app.presentation.calendar.CalendarViewModel
import pl.raillog.app.presentation.dashboard.DashboardScreen
import pl.raillog.app.presentation.dashboard.DashboardViewModel
import pl.raillog.app.presentation.detail.DutyDetailScreen
import pl.raillog.app.presentation.detail.DutyDetailViewModel
import pl.raillog.app.presentation.dutyform.DutyFormScreen
import pl.raillog.app.presentation.dutyform.DutyFormViewModel
import pl.raillog.app.presentation.history.HistoryScreen
import pl.raillog.app.presentation.history.HistoryViewModel
import pl.raillog.app.presentation.settings.SettingsScreen
import pl.raillog.app.presentation.settings.SettingsViewModel
import pl.raillog.app.presentation.statistics.StatisticsScreen
import pl.raillog.app.presentation.trainstatus.TrainStatusScreen
import pl.raillog.app.presentation.trainstatus.TrainStatusViewModel
import pl.raillog.app.presentation.statistics.StatisticsViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class BottomNavItem(val screen: Screen, val title: String, val icon: ImageVector) {
    object Start : BottomNavItem(Screen.Dashboard, "Start", Icons.Default.Home)
    object Duties : BottomNavItem(Screen.History, "Służby", Icons.Default.Train)
    object Stats : BottomNavItem(Screen.Statistics, "Statystyki", Icons.Default.BarChart)
    object Settings : BottomNavItem(Screen.Settings, "Ustawienia", Icons.Default.Settings)
}

@Composable
fun RailLogNavHost(
    repository: DutyRepository,
    planRepository: PlanRepository,
    plkClient: PlkApiClient?,
    themeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Start,
        BottomNavItem.Duties,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.History.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = if (isDark) 8.dp else 0.dp,
                    border = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.screen.route
                            CompactNavItem(
                                item = item,
                                selected = selected,
                                isDark = isDark,
                                onClick = {
                                    if (currentRoute != item.screen.route) {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository, planRepository))
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToNewDuty = { prefilledVehicle ->
                        val route = if (prefilledVehicle != null) {
                            val encoded = URLEncoder.encode(prefilledVehicle, StandardCharsets.UTF_8.toString())
                            "duty_form?dutyId=0&prefilledVehicle=$encoded"
                        } else {
                            Screen.DutyForm.createRoute(0L)
                        }
                        navController.navigate(route)
                    },
                    onNavigateToDutyDetail = { dutyId ->
                        navController.navigate(Screen.DutyDetail.createRoute(dutyId))
                    },
                    onSaveFromPlan = { plannedId ->
                        navController.navigate("duty_form?dutyId=0&plannedId=$plannedId")
                    },
                    onCheckTrain = if (plkClient != null) {
                        { number -> navController.navigate("train_status/$number") }
                    } else null
                )
            }

            // History
            composable(Screen.History.route) {
                val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(repository))
                HistoryScreen(
                    viewModel = vm,
                    onNavigateToDutyDetail = { dutyId ->
                        navController.navigate(Screen.DutyDetail.createRoute(dutyId))
                    },
                    onNavigateToCalendar = {
                        navController.navigate(Screen.Calendar.route)
                    }
                )
            }

            // Statistics
            composable(Screen.Statistics.route) {
                val vm: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory(repository, planRepository))
                StatisticsScreen(viewModel = vm)
            }

            // Settings
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository, planRepository))
                SettingsScreen(
                    viewModel = vm,
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged
                )
            }

            // Calendar
            composable(Screen.Calendar.route) {
                val vm: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(repository, planRepository))
                CalendarScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDutyDetail = { dutyId ->
                        navController.navigate(Screen.DutyDetail.createRoute(dutyId))
                    },
                    onSaveFromPlan = { plannedId ->
                        navController.navigate("duty_form?dutyId=0&plannedId=$plannedId")
                    }
                )
            }

            // Duty Form (New & Edit)
            composable(
                route = "duty_form?dutyId={dutyId}&prefilledVehicle={prefilledVehicle}&plannedId={plannedId}",
                arguments = listOf(
                    navArgument("dutyId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("prefilledVehicle") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("plannedId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val dutyId = backStackEntry.arguments?.getLong("dutyId") ?: 0L
                val plannedId = backStackEntry.arguments?.getLong("plannedId") ?: 0L
                val rawPrefilled = backStackEntry.arguments?.getString("prefilledVehicle")
                val prefilled = if (rawPrefilled != null) {
                    URLDecoder.decode(rawPrefilled, StandardCharsets.UTF_8.toString())
                } else null

                val vm: DutyFormViewModel = viewModel(
                    factory = DutyFormViewModel.factory(repository, planRepository, dutyId, prefilled, plannedId)
                )

                DutyFormScreen(
                    viewModel = vm,
                    onOpenTrainStatus = if (plkClient != null) {
                        { number, date -> navController.navigate("train_status/$number?date=$date") }
                    } else null,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.popBackStack()
                        scope.launch {
                            snackbarHostState.showSnackbar("Służba zapisana ✓")
                        }
                    }
                )
            }

            // Detail
            composable(
                route = Screen.DutyDetail.route,
                arguments = listOf(
                    navArgument("dutyId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val dutyId = backStackEntry.arguments?.getLong("dutyId") ?: 0L
                val vm: DutyDetailViewModel = viewModel(
                    factory = DutyDetailViewModel.Factory(repository, dutyId)
                )

                DutyDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate("duty_form?dutyId=$id&prefilledVehicle=")
                    },
                    onOpenTrainStatus = if (plkClient != null) {
                        { number, date -> navController.navigate("train_status/$number?date=$date") }
                    } else null
                )
            }

            // Podgląd pociągu na żywo (tylko gdy jest klucz API)
            if (plkClient != null) {
                composable(
                    route = "train_status/{number}?date={date}",
                    arguments = listOf(
                        navArgument("number") { type = NavType.StringType },
                        navArgument("date") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val number = backStackEntry.arguments?.getString("number").orEmpty()
                    val date = backStackEntry.arguments?.getString("date")?.let {
                        runCatching { java.time.LocalDate.parse(it) }.getOrNull()
                    }
                    val vm: TrainStatusViewModel = viewModel(
                        factory = TrainStatusViewModel.Factory(plkClient, number, date)
                    )
                    TrainStatusScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun CompactNavItem(
    item: BottomNavItem,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = if (selected) {
                activeColor.copy(alpha = if (isDark) 0.16f else 0.12f)
            } else {
                Color.Transparent
            },
            modifier = Modifier.size(width = 48.dp, height = 32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (selected) activeColor else inactiveColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) activeColor else inactiveColor
        )
    }
}
