package com.example.eventplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventplanner.ui.components.EventItem
import com.example.eventplanner.viewmodel.EventViewModel

@Composable
fun HomeScreen(navController: NavHostController) {
    val eventViewModel: EventViewModel = viewModel()
    val events by eventViewModel.events.collectAsState(initial = emptyList())
    val isLoading by eventViewModel.isLoading.collectAsState()
    val error by eventViewModel.error.collectAsState()

    // Fetch events for the logged-in user when the screen is launched
    LaunchedEffect(Unit) {
        eventViewModel.fetchAllEvents()  // Ensure you're calling the correct method here
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Events", style = MaterialTheme.typography.titleLarge)

        if (isLoading) {
            CircularProgressIndicator()
        } else if (events.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(events) { event ->
                    EventItem(event = event) {
                        navController.navigate("eventDetail/${event.id}")
                    }
                }
            }
        } else {
            Text("No events available.", style = MaterialTheme.typography.bodyMedium)
        }

        // Display error message if there's any
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
