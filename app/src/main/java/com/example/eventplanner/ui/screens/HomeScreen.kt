package com.example.eventplanner.ui.screens

import com.example.eventplanner.viewmodel.EventViewModel
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventplanner.ui.components.AddEventDialog
import com.example.eventplanner.ui.components.EventItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun HomeScreen(viewModel: EventViewModel = viewModel()) {
    val events by viewModel.events.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    // Fetch all events on screen load
    LaunchedEffect(Unit) {
        viewModel.fetchAllEvents()
    }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "📅 Upcoming Events",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
            }

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
                        // Modify the EventItem to include styles for text
                        EventItem(event)
                    }
                }
            }

            // Add Event Dialog
            if (showDialog) {
                val context = LocalContext.current
                val calendar = Calendar.getInstance()

                AddEventDialog(
                    selectedDate = selectedDate,
                    onDismiss = { showDialog = false },
                    onAdd = { title, description, date, location ->
                        viewModel.createEvent(title, description, date, location)
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
