package com.chumakov123.outageschedule.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chumakov123.outageschedule.presentation.screen.alloutages.AllOutagesScreen
import com.chumakov123.outageschedule.presentation.screen.history.HistoryScreen
import com.chumakov123.outageschedule.presentation.screen.settings.SettingsScreen
import com.chumakov123.outageschedule.presentation.screen.trackedplaces.TrackedPlacesScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AllOutages.route,
        modifier = modifier
    ) {
        composable(Screen.AllOutages.route) {
            AllOutagesScreen()
        }

        composable(Screen.TrackedPlaces.route) {
            TrackedPlacesScreen()
        }

        composable(Screen.History.route) {
            HistoryScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}