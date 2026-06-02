package com.chumakov123.outageschedule.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.chumakov123.outageschedule.R

data class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.AllOutages.route,
        titleRes = R.string.nav_outages,
        icon = Icons.Outlined.Bolt
    ),
    BottomNavItem(
        route = Screen.TrackedPlaces.route,
        titleRes = R.string.nav_my_addresses,
        icon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Settings.route,
        titleRes = R.string.nav_settings,
        icon = Icons.Outlined.Settings
    )
)