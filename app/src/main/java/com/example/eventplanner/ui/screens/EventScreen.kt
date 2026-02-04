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
import com.example.eventplanner.data.model.Event
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel = viewModel()
) {
    val events by eventViewModel.events.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Firebase user
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid
    var displayName by remember { mutableStateOf("Loading...") }

    // Fetch display name
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

    // Fetch user events
    LaunchedEffect(Unit) {
        eventViewModel.fetchUserEvents()
    }

    // DatePickerDialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = dateFormatter.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
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
            Text(
                text = "Logged in as: $displayName",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        canEdit = isOrganizer,  // Allow editing if the user is the organizer
                        onDelete = {
                            event.id?.let { eventId ->
                                eventViewModel.deleteEvent(eventId)
                            }
                        },
                        onEdit = {
                            event.id?.let {
                                navController.navigate("editEvent/$it") // Navigate to Edit Event screen
                            }
                        }
                    )
                }
            }

            // Dialog with working calendar
            if (showDialog) {
                AddEventDialog(
                    selectedDate = selectedDate,
                    onDismiss = {
                        showDialog = false
                        selectedDate = ""
                    },
                    onAdd = { title, description, date, location, ticketingEnabled, ticketType, priceCents ->
                        val event = Event(
                            id = "",
                            title = title,
                            description = description,
                            date = date,
                            location = location,
                            organizerId = uid ?: "",
                            organizerName = displayName,
                            attendees = mutableListOf(),
                            ticketingEnabled = ticketingEnabled,
                            ticketType = ticketType,
                            priceCents = priceCents
                        )
                        eventViewModel.createEvent(event)
                        showDialog = false
                        selectedDate = ""
                    },
                    onDateClick = {
                        datePickerDialog.show()
                    }
                )
            }
        }
    }
}