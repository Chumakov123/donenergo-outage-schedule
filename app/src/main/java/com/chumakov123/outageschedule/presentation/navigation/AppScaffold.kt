package com.chumakov123.outageschedule.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chumakov123.outageschedule.data.work.InitialDataSyncScheduler
import com.chumakov123.outageschedule.data.work.OutageSyncScheduler
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppScaffold(
    viewModel: AppEntryViewModel = koinViewModel()
) {
    val entryState by viewModel.state.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(entryState.isLoading, entryState.isOnboardingCompleted) {
        if (!entryState.isLoading && entryState.isOnboardingCompleted) {
            OutageSyncScheduler.schedule(context)
        }

        if (!entryState.isLoading && !entryState.isOnboardingCompleted) {
            InitialDataSyncScheduler.schedule(context)
        }
    }

    if (entryState.isLoading) {
        ScreenContainer { }
        return
    }

    val startDestination =
        if (entryState.isOnboardingCompleted) {
            Screen.AllOutages.route
        } else {
            Screen.Onboarding.route
        }

    Scaffold(
        bottomBar = {
            if (entryState.isOnboardingCompleted) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { item ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(
                                        navController.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(item.title)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}