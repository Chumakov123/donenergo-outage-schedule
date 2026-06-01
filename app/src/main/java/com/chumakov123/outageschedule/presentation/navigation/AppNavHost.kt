package com.chumakov123.outageschedule.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chumakov123.outageschedule.presentation.screen.alloutages.AllOutagesScreen
import com.chumakov123.outageschedule.presentation.screen.onboarding.OnboardingScreen
import com.chumakov123.outageschedule.presentation.screen.settings.SettingsScreen
import com.chumakov123.outageschedule.presentation.screen.trackedplaces.TrackedPlacesScreen

private const val OPEN_TRACKED_PLACES_FORM_KEY = "open_tracked_places_form"

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.AllOutages.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AllOutages.route) {
            AllOutagesScreen(
                onOpenTrackedPlaces = { openForm ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(OPEN_TRACKED_PLACES_FORM_KEY, openForm)

                    navController.navigate(Screen.TrackedPlaces.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.TrackedPlaces.route) {
            val openFormOnEnter = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>(OPEN_TRACKED_PLACES_FORM_KEY) == true

            LaunchedEffect(openFormOnEnter) {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.remove<Boolean>(OPEN_TRACKED_PLACES_FORM_KEY)
            }

            TrackedPlacesScreen(openFormOnEnter = openFormOnEnter)
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}