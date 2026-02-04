package com.example.eventplanner.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.eventplanner.ui.components.BottomNavBar
import com.example.eventplanner.ui.screens.EditEventScreen
import com.example.eventplanner.ui.screens.EventDetailScreen
import com.example.eventplanner.ui.screens.EventScreen
import com.example.eventplanner.ui.screens.HomeScreen
import com.example.eventplanner.ui.screens.LoginScreen
import com.example.eventplanner.ui.screens.MyTicketScreen
import com.example.eventplanner.ui.screens.ParticipationScreen
import com.example.eventplanner.ui.screens.ProfileScreen
import com.example.eventplanner.ui.screens.SignupScreen
import com.example.eventplanner.viewmodel.EventViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
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
                LoginScreen(navController = navController)
            }
            composable("signup") {
                SignupScreen(navController = navController)
            }
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("events") {
                EventScreen(navController = navController)
            }
            composable("profile") {
                ProfileScreen(navController = navController)
            }
            composable("participation") {
                ParticipationScreen(navController = navController)
            }
            composable("eventDetail/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    navController = navController
                )
            }
            composable("myTicket/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                MyTicketScreen(
                    eventId = eventId,
                    navController = navController
                )
            }
            composable("editEvent/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                val viewModel: EventViewModel = viewModel()
                EditEventScreen(
                    eventId = eventId,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
