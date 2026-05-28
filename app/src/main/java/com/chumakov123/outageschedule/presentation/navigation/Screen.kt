package com.chumakov123.outageschedule.presentation.navigation

sealed class Screen(
    val route: String
) {
    data object AllOutages : Screen("all_outages")
    data object TrackedPlaces : Screen("tracked_places")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Onboarding : Screen("onboarding")
}