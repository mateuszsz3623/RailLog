package pl.raillog.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import pl.raillog.app.core.designsystem.GriffinRed
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.presentation.calendar.CalendarScreen
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
import pl.raillog.app.presentation.statistics.StatisticsViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class BottomNavItem(val screen: Screen, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Start : BottomNavItem(Screen.Dashboard, "Start", Icons.Default.Home)
    object Duties : BottomNavItem(Screen.History, "Służby", Icons.Default.Train)
    object Stats : BottomNavItem(Screen.Statistics, "Statystyki", Icons.Default.BarChart)
    object Settings : BottomNavItem(Screen.Settings, "Ustawienia", Icons.Default.Settings)
}

@Composable
fun RailLogNavHost(
    repository: DutyRepository,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
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
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
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
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(text = item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GriffinRed,
                                selectedTextColor = GriffinRed,
                                indicatorColor = GriffinRed.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
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
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))
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
                    }
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
                val vm: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory(repository))
                StatisticsScreen(viewModel = vm)
            }

            // Settings
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository))
                SettingsScreen(
                    viewModel = vm,
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = onThemeChanged
                )
            }

            // Calendar
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    repository = repository,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDutyDetail = { dutyId ->
                        navController.navigate(Screen.DutyDetail.createRoute(dutyId))
                    }
                )
            }

            // Duty Form (New & Edit)
            composable(
                route = "duty_form?dutyId={dutyId}&prefilledVehicle={prefilledVehicle}",
                arguments = listOf(
                    navArgument("dutyId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("prefilledVehicle") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val dutyId = backStackEntry.arguments?.getLong("dutyId") ?: 0L
                val rawPrefilled = backStackEntry.arguments?.getString("prefilledVehicle")
                val prefilled = if (rawPrefilled != null) {
                    URLDecoder.decode(rawPrefilled, StandardCharsets.UTF_8.toString())
                } else null

                val vm: DutyFormViewModel = viewModel(
                    factory = DutyFormViewModel.Factory(repository, dutyId, prefilled)
                )

                DutyFormScreen(
                    viewModel = vm,
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
                    }
                )
            }
        }
    }
}
