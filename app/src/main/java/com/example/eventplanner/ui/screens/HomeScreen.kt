package com.example.eventplanner.ui.screens

import EventViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventplanner.data.model.Event
import com.example.eventplanner.ui.components.AddEventDialog
import com.example.eventplanner.ui.components.EventItem

@Composable
fun HomeScreen(viewModel: EventViewModel = viewModel()) {
    val events by viewModel.events.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") } // Store the selected date

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No events yet.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(events) { event ->
                        EventItem(event) // Use the shared EventItem composable
                    }
                }
            }

            if (showDialog) {
                AddEventDialog(
                    selectedDate = selectedDate, // Pass selectedDate here
                    onDismiss = { showDialog = false },
                    onAdd = { title, description, date, location ->
                        viewModel.createEvent(title, description, date, location)
                        showDialog = false
                    }
                )
            }
        }
    }
}
