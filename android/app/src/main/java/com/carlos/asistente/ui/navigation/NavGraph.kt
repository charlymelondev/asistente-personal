package com.carlos.asistente.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carlos.asistente.ui.screens.agenda.AgendaScreen
import com.carlos.asistente.ui.screens.detail.DetailScreen
import com.carlos.asistente.ui.screens.home.HomeScreen
import com.carlos.asistente.ui.components.UpdateChecker
import com.carlos.asistente.ui.screens.tasks.TasksScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Inicio", Icons.Default.Home)
    data object Agenda : Screen("agenda", "Agenda", Icons.Default.CalendarMonth)
    data object Tasks : Screen("tasks", "Tareas", Icons.Default.List)
}

val bottomNavItems = listOf(Screen.Home, Screen.Agenda, Screen.Tasks)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Hide bottom bar on detail screen
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    // Check for app updates on startup
    UpdateChecker()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onTaskClick = { taskId ->
                        navController.navigate("detail/$taskId")
                    }
                )
            }

            composable(Screen.Agenda.route) {
                AgendaScreen(
                    onTaskClick = { taskId ->
                        navController.navigate("detail/$taskId")
                    }
                )
            }

            composable(Screen.Tasks.route) {
                TasksScreen(
                    onTaskClick = { taskId ->
                        navController.navigate("detail/$taskId")
                    }
                )
            }

            composable(
                route = "detail/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                DetailScreen(
                    taskId = taskId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
