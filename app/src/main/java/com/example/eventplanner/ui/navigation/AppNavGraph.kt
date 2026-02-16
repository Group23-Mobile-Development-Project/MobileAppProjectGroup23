package com.example.eventplanner.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.eventplanner.ui.screens.ParticipationScreen
import com.example.eventplanner.ui.screens.ProfileScreen
import com.example.eventplanner.ui.screens.SignupScreen
import com.example.eventplanner.viewmodel.EventViewModel
import com.example.eventplanner.ui.screens.OrganizerDashboardScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val showTopUi = currentRoute != "login" && currentRoute != "signup" && currentRoute != null

    val title = when {
        currentRoute == "home" -> "Home"
        currentRoute == "events" -> "My Events"
        currentRoute == "participation" -> "Participation"
        currentRoute == "profile" -> "Profile"
        currentRoute?.startsWith("eventDetail/") == true -> "Event Details"
        currentRoute?.startsWith("editEvent/") == true -> "Edit Event"
        currentRoute?.startsWith("myTicket/") == true -> "My Ticket"
        else -> "Event Planner"
    }

    val onLogout = {
        FirebaseAuth.getInstance().signOut()
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (showTopUi) {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open menu"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute != "login" && currentRoute != "signup") {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->

        // Drawer wraps the CONTENT area, but we pad the drawer sheet
        // so it does not go under TopAppBar or BottomNavBar.
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showTopUi,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(240.dp)
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                ) {
                    DrawerContent(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("home") { saveState = true }
                            }
                        },
                        onLogout = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        }
                    )
                }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable("login") { LoginScreen(navController = navController) }
                composable("signup") { SignupScreen(navController = navController) }
                composable("home") { HomeScreen(navController = navController) }
                composable("events") { EventScreen(navController = navController) }
                composable("profile") { ProfileScreen(navController = navController) }
                composable("participation") { ParticipationScreen(navController = navController) }

                composable("eventDetail/{eventId}") { entry ->
                    val eventId = entry.arguments?.getString("eventId") ?: ""
                    EventDetailScreen(eventId = eventId, navController = navController)
                }

                composable("editEvent/{eventId}") { entry ->
                    val eventId = entry.arguments?.getString("eventId") ?: ""
                    val vm: EventViewModel = viewModel()
                    EditEventScreen(eventId = eventId, navController = navController, viewModel = vm)
                }

                composable("organizerDashboard/{eventId}") { entry ->
                    val eventId = entry.arguments?.getString("eventId") ?: ""
                    OrganizerDashboardScreen(
                        eventId = eventId,
                        navController = navController
                    )
                }
                // keep this only if you use it
                // composable("myTicket/{eventId}") { entry ->
                //     val eventId = entry.arguments?.getString("eventId") ?: ""
                //     MyTicketScreen(eventId = eventId, navController = navController)
                // }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore("@")
        ?: "User"

    val items = listOf(
        "home" to "Home",
        "events" to "My Events",
        "participation" to "Participation",
        "profile" to "Profile"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 18.dp)
    ) {
        Text(text = "Hi $name", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(14.dp))

        items.forEach { (route, label) ->
            NavigationDrawerItem(
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout,
            icon = {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Logout"
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
