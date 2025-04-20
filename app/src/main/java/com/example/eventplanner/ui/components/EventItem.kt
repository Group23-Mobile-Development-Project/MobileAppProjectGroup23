package com.example.eventplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventplanner.data.model.Event

@Composable
fun EventItem(
    event: Event,
    onClick: () -> Unit,
    canDelete: Boolean = false,
    canEdit: Boolean = false,  // Add canEdit parameter
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null  // Add onEdit parameter
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Text(text = event.date)
            Text(text = event.location)

            // Conditionally display the Delete button
            if (canDelete && onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }

            // Conditionally display the Edit button
            if (canEdit && onEdit != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Edit")
                }
            }
        }
    }
}
