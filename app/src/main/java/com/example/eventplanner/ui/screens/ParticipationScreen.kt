package com.example.eventplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventplanner.viewmodel.EventViewModel
import com.example.eventplanner.data.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipationScreen(viewModel: EventViewModel = viewModel()) {
    val userId = viewModel.currentUser?.uid

    LaunchedEffect(userId) {
        viewModel.fetchAllEvents() // Load all events
    }

    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val attendingEvents = events.filter { event ->
        event.attendees.any { it.userId == userId && it.status == "attending" }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Participation") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }

                attendingEvents.isEmpty() -> {
                    Text(
                        "You are not attending any events yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(attendingEvents) { event ->
                            ParticipationEventCard(event = event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipationEventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: ${event.date}")
            Text("Location: ${event.location}")
            Text("Organizer: ${event.organizerName}")
        }
    }
}
