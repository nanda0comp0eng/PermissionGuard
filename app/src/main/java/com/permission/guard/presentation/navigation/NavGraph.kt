package com.permission.guard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.permission.guard.presentation.applist.AppListScreen
import com.permission.guard.presentation.applist.AppListViewModel
import com.permission.guard.presentation.dashboard.DashboardScreen
import com.permission.guard.presentation.dashboard.DashboardViewModel
import com.permission.guard.presentation.detail.AppDetailScreen
import com.permission.guard.presentation.detail.AppDetailViewModel
import com.permission.guard.presentation.history.HistoryScreen
import com.permission.guard.presentation.history.HistoryViewModel
import com.permission.guard.presentation.onboarding.OnboardingScreen
import com.permission.guard.presentation.settings.SettingsScreen
import com.permission.guard.presentation.settings.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onStartInitialScan = {
                    settingsViewModel.triggerManualScan()
                    settingsViewModel.schedulePeriodicScan(12) // Default 12 hour interval
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToAppList = { navController.navigate(Screen.AppList.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.AppList.route) {
            val appListViewModel: AppListViewModel = hiltViewModel()
            AppListScreen(
                viewModel = appListViewModel,
                onNavigateToDetail = { packageName ->
                    navController.navigate(Screen.AppDetail.createRoute(packageName))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            val appDetailViewModel: AppDetailViewModel = hiltViewModel()
            AppDetailScreen(
                viewModel = appDetailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
