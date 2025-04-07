package com.example.eventplanner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventplanner.data.model.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Title: ${event.title}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Description: ${event.description}")
            Text(text = "Date: $formattedDate")
            Text(text = "Location: ${event.location}")
            Text(text = "Organizer: ${event.organizerName}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
