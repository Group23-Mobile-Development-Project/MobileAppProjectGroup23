package com.example.eventplanner.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eventplanner.ui.components.AddEventDialog
import com.example.eventplanner.ui.components.EventItem
import com.example.eventplanner.viewmodel.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import com.example.eventplanner.data.model.Event
@Composable
fun EventScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel = viewModel()
) {
    // Collect events from ViewModel
    val events by eventViewModel.events.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    // Get the current user info
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid
    var displayName by remember { mutableStateOf("Loading...") }

    // Fetch display name of the logged-in user
    LaunchedEffect(uid) {
        uid?.let {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { doc ->
                    displayName = doc.getString("name") ?: "Unknown"
                }
                .addOnFailureListener {
                    displayName = "Unknown"
                }
        }
    }

    // Fetch user events on screen launch
    LaunchedEffect(Unit) {
        eventViewModel.fetchUserEvents()
    }

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
            // Display the logged-in user's name
            Text(
                text = "Logged in as: $displayName",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display the list of events
            LazyColumn {
                items(events) { event ->
                    val isOrganizer = event.organizerId == uid
                    EventItem(
                        event = event,
                        onClick = {
                            event.id?.let {
                                navController.navigate("eventDetail/$it")
                            }
                        },
                        canDelete = isOrganizer,
                        onDelete = {
                            event.id?.let { eventId ->
                                eventViewModel.deleteEvent(eventId)
                            }
                        }
                    )
                }
            }


            // Add event dialog handling
            if (showDialog) {
                val context = LocalContext.current
                val calendar = Calendar.getInstance()

                AddEventDialog(
                    selectedDate = selectedDate,
                    onDismiss = { showDialog = false },
                    onAdd = { title, description, date, location ->
                        val event = Event(
                            id = "", // Empty for new events, Firestore will generate it
                            title = title,
                            description = description,
                            date = date,
                            location = location,
                            organizerId = uid ?: "",
                            organizerName = displayName,
                            attendees = mutableListOf()
                        )
                        eventViewModel.createEvent(event) // Pass the event object
                        showDialog = false
                        selectedDate = ""
                    },
                    onDateClick = {
                        // Date picker logic
                    }
                )

            }
        }
    }
}
