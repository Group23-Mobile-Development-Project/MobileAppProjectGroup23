package com.example.eventplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import com.example.eventplanner.screens.LoginScreen
import com.example.eventplanner.ui.screens.HomeScreen
import com.example.eventplanner.screens.SignupScreen
import com.example.eventplanner.ui.components.BottomNavBar
import androidx.compose.material3.*

@Composable
fun EventsScreen(navController: NavHostController) {
    // Placeholder UI for the EventsScreen
    Text("Events Screen is under construction.")
}

@Composable
fun ParticipationScreen(navController: NavHostController) {
    // Placeholder UI for the EventsScreen
    Text("Participation Screen is under construction.")
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    // Placeholder UI for the EventsScreen
    Text("profile Screen is under construction.")
}

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Only show the BottomNavBar if the current route is not 'login' or 'signup'
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute != "login" && currentRoute != "signup") {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(navController)
            }
            composable("signup") {
                SignupScreen(navController)
            }
            composable("home") {
                HomeScreen(navController)
            }
            composable("events") {
                EventsScreen(navController)  // Define your EventsScreen composable here
            }
            composable("participation") {
                ParticipationScreen(navController)  // You need to create this screen
            }

            composable("profile") {
                ProfileScreen(navController)  // You need to create this screen
            }
            // Add other composable screens as needed
        }
    }
}
