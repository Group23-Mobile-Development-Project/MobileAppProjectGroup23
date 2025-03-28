package com.example.eventplanner.ui.navigation

import androidx.annotation.DrawableRes
import com.example.eventplanner.R

class BottomNavScreen {
    // Sealed class for Bottom Navigation Screens
    sealed class BottomNavScreen(val route: String, val title: String, @DrawableRes val icon: Int) {
        object Home : BottomNavScreen("home", "Home", R.drawable.ic_home)
        object Profile : BottomNavScreen("profile", "Profile", R.drawable.ic_profile)
        object Settings : BottomNavScreen("settings", "Settings", R.drawable.ic_settings)
    }

    // List of all Bottom Navigation items
    val bottomNavItems = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.Profile,
        BottomNavScreen.Settings
    )

}