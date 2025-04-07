package com.example.eventplanner.ui.screens

import EventViewModel
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.eventplanner.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun EventScreen(viewModel: EventViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                date = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            },
            year,
            month,
            day
        )
    }

    // Firebase user
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid
    var displayName by remember { mutableStateOf("Loading...") }

    // Fetch user's name from Firestore
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
            Text(
                text = "Logged in as: $displayName",
                style = MaterialTheme.typography.bodyMedium
            )

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
                                title = ""
                                description = ""
                                date = ""
                                location = ""
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
                            Button(onClick = { datePickerDialog.show() }) {
                                Text(text = if (date.isEmpty()) "Select Date" else "Date: $date")
                            }
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
    val formattedDate = try {
        LocalDate.parse(event.date, DateTimeFormatter.ISO_DATE)
            .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    } catch (e: Exception) {
        event.date // fallback
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Title: ${event.title}", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Description: ${event.description}")
            Text(text = "Date: $formattedDate")
            Text(text = "Location: ${event.location}")
            Text(text = "Organizer: ${event.organizerName}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
