package com.example.eventplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eventplanner.ui.components.EventItem
import com.example.eventplanner.viewmodel.EventViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavHostController) {
    val eventViewModel: EventViewModel = viewModel()
    val events by eventViewModel.events.collectAsState(initial = emptyList())
    val isLoading by eventViewModel.isLoading.collectAsState()
    val error by eventViewModel.error.collectAsState()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Fetch all events when the screen is launched
    LaunchedEffect(Unit) {
        eventViewModel.fetchAllEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "All Events",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            events.isNotEmpty() -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(events) { event ->
                        val isOrganizer = event.organizerId == currentUserId
                        EventItem(
                            event = event,
                            onClick = {
                                navController.navigate("eventDetail/${event.id}")
                            },
                            canDelete = isOrganizer,
                            onDelete = {
                                event.id?.let { eventViewModel.deleteEvent(it) }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            else -> {
                Text("No events available.")
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
