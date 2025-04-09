package com.example.eventplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddEventDialog(
    selectedDate: String,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit,
    onDateClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(title, description, selectedDate, location)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add Event") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Date: ${if (selectedDate.isNotEmpty()) selectedDate else "Select Event Date"}",
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { onDateClick() },
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
            }
        },
        modifier = Modifier.padding(16.dp)
    )
}
