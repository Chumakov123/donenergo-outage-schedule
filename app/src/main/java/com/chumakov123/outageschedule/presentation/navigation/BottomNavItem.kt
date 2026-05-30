package com.chumakov123.outageschedule.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.AllOutages.route,
        title = "Отключения",
        icon = Icons.Outlined.Bolt
    ),
    BottomNavItem(
        route = Screen.TrackedPlaces.route,
        title = "Мои адреса",
        icon = Icons.Outlined.Home
    ),
//    BottomNavItem(
//        route = Screen.History.route,
//        title = "Архив",
//        icon = Icons.Outlined.History
//    ),
    BottomNavItem(
        route = Screen.Settings.route,
        title = "Настройки",
        icon = Icons.Outlined.Settings
    )
)