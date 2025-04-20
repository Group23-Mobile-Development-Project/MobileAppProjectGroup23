package com.example.eventplanner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventplanner.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    navController: NavController,
    viewModel: EventViewModel
) {
    // Collecting state for event data and loading status
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Fetch the event details when the screen is first shown
    LaunchedEffect(eventId) {
        viewModel.fetchEventById(eventId)
    }

    // If the event is successfully fetched
    selectedEvent?.let { event ->
        var title by remember { mutableStateOf(event.title) }
        var description by remember { mutableStateOf(event.description) }
        var location by remember { mutableStateOf(event.location) }
        var date by remember { mutableStateOf(event.date) }

        // Scaffold with a top bar and the form layout
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Edit Event") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title input field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Description input field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Location input field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date input field
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (e.g., 2025-04-27)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Save button to update the event details
                Button(
                    onClick = {
                        // Updating the event with new details
                        viewModel.updateEventDetails(
                            eventId = event.id,
                            updatedFields = mapOf(
                                "title" to title,
                                "description" to description,
                                "location" to location,
                                "date" to date
                            )
                        )
                        // Navigate back after saving changes
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    } ?: run {
        // Handling loading and error states
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Event not found.")
            }
        }
    }
}
