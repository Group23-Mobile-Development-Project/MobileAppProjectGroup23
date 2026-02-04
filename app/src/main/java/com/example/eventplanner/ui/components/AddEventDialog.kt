package com.example.eventplanner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventplanner.data.model.TicketType

@Composable
fun AddEventDialog(
    selectedDate: String,
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        description: String,
        date: String,
        location: String,
        ticketingEnabled: Boolean,
        ticketType: String,
        priceCents: Long
    ) -> Unit,
    onDateClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var ticketingEnabled by remember { mutableStateOf(false) }
    var ticketType by remember { mutableStateOf(TicketType.FREE.value) }
    var priceText by remember { mutableStateOf("") }

    fun priceToCents(text: String): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0L
        // allow "12", "12.5", "12.50"
        val normalized = trimmed.replace(',', '.')
        val value = normalized.toDoubleOrNull() ?: return 0L
        return (value * 100.0).toLong()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val priceCents = if (ticketingEnabled && ticketType == TicketType.PAID.value) {
                        priceToCents(priceText)
                    } else {
                        0L
                    }

                    onAdd(
                        title,
                        description,
                        selectedDate,
                        location,
                        ticketingEnabled,
                        ticketType,
                        priceCents
                    )
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = onDateClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (selectedDate.isNotEmpty()) "Date: $selectedDate" else "Select Event Date")
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable ticketing")
                    Switch(
                        checked = ticketingEnabled,
                        onCheckedChange = { enabled ->
                            ticketingEnabled = enabled
                            if (!enabled) {
                                ticketType = TicketType.FREE.value
                                priceText = ""
                            }
                        }
                    )
                }

                if (ticketingEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ticket type")

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(
                                selected = ticketType == TicketType.FREE.value,
                                onClick = {
                                    ticketType = TicketType.FREE.value
                                    priceText = ""
                                },
                                label = { Text("Free") }
                            )

                            FilterChip(
                                selected = ticketType == TicketType.PAID.value,
                                onClick = { ticketType = TicketType.PAID.value },
                                label = { Text("Paid") }
                            )
                        }

                        if (ticketType == TicketType.PAID.value) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = { Text("Price (EUR)") },
                                supportingText = { Text("Example: 9.99") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.padding(16.dp)
    )
}
