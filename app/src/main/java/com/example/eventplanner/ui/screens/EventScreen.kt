package com.example.eventplanner.ui.screens

import EventViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.eventplanner.data.model.Event
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EventScreen(viewModel: EventViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    // Get logged-in user's UID
    val user = FirebaseAuth.getInstance().currentUser
    val organizerId = user?.uid ?: "Unknown"

    // Collect events from Firestore
    val events by viewModel.events.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Event")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Logged in as: $organizerId", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(events) { event ->
                    EventItem(event)
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            if (title.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty() && location.isNotEmpty()) {
                                viewModel.createEvent(title, description, date, location)
                                showDialog = false
                            }
                        }) {
                            Text("Add Event")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Add Event") },
                    text = {
                        Column {
                            TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = date, onValueChange = { date = it }, label = { Text("Date") })
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Title: ${event.title}", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Description: ${event.description}")
            Text(text = "Date: ${event.date}")
            Text(text = "Location: ${event.location}")
            Text(text = "Organizer: ${event.organizerName}", style = MaterialTheme.typography.bodySmall) // ✅ Now showing name!
        }
    }
}
