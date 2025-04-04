package com.example.eventplanner.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import com.example.eventplanner.ui.screens.LoginScreen
import com.example.eventplanner.ui.screens.SignupScreen
import com.example.eventplanner.ui.screens.HomeScreen
import com.example.eventplanner.ui.screens.EventScreen
import com.example.eventplanner.ui.screens.ProfileScreen
import com.example.eventplanner.ui.screens.ParticipationScreen
import com.example.eventplanner.ui.components.BottomNavBar
import androidx.compose.material3.*

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
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
                LoginScreen(navController)
            }
            composable("signup") {
                SignupScreen(navController)
            }
            composable("home") {
                HomeScreen(navController)
            }
            composable("events") {
                EventScreen()
            }
            composable("profile") {
                ProfileScreen(navController)
            }
            composable("participation") {
                ParticipationScreen()
            }
        }
    }
}
