package com.example.eventplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eventplanner.viewmodel.EventViewModel
import com.example.eventplanner.data.model.Event

@Composable
fun EventDetailScreen(
    eventId: String,
    navController: NavHostController? = null, // optional, in case you want to add back navigation
    viewModel: EventViewModel = viewModel()
) {
    // Fetch the event when the screen is opened
    LaunchedEffect(eventId) {
        viewModel.fetchEventById(eventId)
    }

    val event by viewModel.selectedEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            event != null -> {
                EventDetailContent(event!!)
            }

            else -> {
                Text("No event found.")
            }
        }
    }
}

@Composable
fun EventDetailContent(event: Event) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = event.title, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Date: ${event.date}")
        Text(text = "Location: ${event.location}")
        Text(text = "Organizer: ${event.organizerName}")
        Text(text = "Description:\n${event.description}")
    }
}
