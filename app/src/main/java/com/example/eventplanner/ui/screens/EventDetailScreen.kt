package com.example.eventplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eventplanner.viewmodel.EventViewModel
import com.example.eventplanner.data.model.Event
import com.example.eventplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    navController: NavHostController? = null,
    viewModel: EventViewModel = viewModel()
) {
    // Fetch the event when the screen is opened
    LaunchedEffect(eventId) {
        viewModel.fetchEventById(eventId)
    }

    val event by viewModel.selectedEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }

                event != null -> {
                    EventDetailContent(event = event!!, navController = navController, viewModel = viewModel)
                }

                else -> {
                    Text("No event found.")
                }
            }
        }
    }
}

@Composable
fun EventDetailContent(
    event: Event,
    navController: NavHostController?,
    viewModel: EventViewModel
) {
    val userId = viewModel.currentUser?.uid
    val isUserAttending = event.attendees.any { it.userId == userId && it.status == "attending" }

    val attendingCount = event.attendees.count { it.status == "attending" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = "Date: ${event.date}", fontSize = 16.sp)
            Text(text = "Location: ${event.location}", fontSize = 16.sp)
            Text(text = "Organizer: ${event.organizerName}", fontSize = 16.sp)
            Text(text = "Description:\n${event.description}", fontSize = 16.sp)
            Text(text = "Attendees: $attendingCount", fontSize = 16.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (!isUserAttending) {
                        viewModel.updateRSVPStatus(event.id, "attending")
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isUserAttending
            ) {
                Text("Attending")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    if (isUserAttending) {
                        viewModel.updateRSVPStatus(event.id, "not attending")
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = isUserAttending
            ) {
                Text("Not Attending")
            }
        }
    }
}
