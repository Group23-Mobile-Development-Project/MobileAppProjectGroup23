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

@Composable
fun EventScreen(
    navController: NavHostController,
    eventViewModel: EventViewModel = viewModel()
) {
    val events by eventViewModel.events.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

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
                    EventItem(event = event) {
                        event.id?.let {
                            navController.navigate("eventDetail/${event.id}")
                        }
                    }
                }
            }


            if (showDialog) {
                val context = LocalContext.current
                val calendar = Calendar.getInstance()

                AddEventDialog(
                    selectedDate = selectedDate,
                    onDismiss = { showDialog = false },
                    onAdd = { title, description, date, location ->
                        eventViewModel.createEvent(title, description, date, location)
                        showDialog = false
                        selectedDate = ""
                    },
                    onDateClick = {
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                val date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                                selectedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            },
                            year, month, day
                        ).show()
                    }
                )
            }
        }
    }
}

