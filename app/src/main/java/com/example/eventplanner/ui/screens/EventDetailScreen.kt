package com.example.eventplanner.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eventplanner.R
import com.example.eventplanner.data.model.Event
import com.example.eventplanner.data.model.PaymentStatus
import com.example.eventplanner.data.model.Ticket
import com.example.eventplanner.data.model.TicketType
import com.example.eventplanner.viewmodel.EventViewModel
import com.example.eventplanner.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    navController: NavHostController? = null,
    viewModel: EventViewModel = viewModel(),
    ticketViewModel: TicketViewModel = viewModel()
) {
    LaunchedEffect(eventId) {
        viewModel.fetchEventById(eventId)
        ticketViewModel.start(eventId)
    }

    val event by viewModel.selectedEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                event != null -> EventDetailContent(
                    event = event!!,
                    navController = navController,
                    viewModel = viewModel,
                    ticketViewModel = ticketViewModel
                )
                else -> Text("No event found.")
            }
        }
    }
}

@Composable
fun EventDetailContent(
    event: Event,
    navController: NavHostController?,
    viewModel: EventViewModel,
    ticketViewModel: TicketViewModel
) {
    val userId = viewModel.currentUser?.uid
    val isUserAttending = event.attendees.any { it.userId == userId && it.status == "attending" }
    val attendingCount = event.attendees.count { it.status == "attending" }

    val context = LocalContext.current

    val ticket by ticketViewModel.ticket.collectAsState()
    val ticketingEnabled = event.ticketingEnabled
    val paymentRequired = event.ticketingEnabled && event.ticketType == TicketType.PAID.value
    val ticketStatusText = ticketStatusText(ticket, ticketingEnabled, paymentRequired)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = "Date: ${event.date}", fontSize = 16.sp)
            Text(text = "Location: ${event.location}", fontSize = 16.sp)
            Text(text = "Organizer: ${event.organizerName}", fontSize = 16.sp)
            Text(text = "Description:\n${event.description}", fontSize = 16.sp)
            Text(text = "Attendees: $attendingCount", fontSize = 15.sp)

            val attendingUsers = event.attendees.filter { it.status == "attending" }
            if (attendingUsers.isNotEmpty()) {
                Text("Attending Users:", fontSize = 16.sp)
                attendingUsers.forEach { attendee ->
                    Text("- ${attendee.userName}", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Ticket: $ticketStatusText", fontSize = 16.sp)

            if (ticketingEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (ticket == null) {
                        Button(
                            onClick = { ticketViewModel.createOrGetTicket(event.id, paymentRequired) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (paymentRequired) "Get ticket" else "Get free ticket")
                        }
                    }

                    Button(
                        onClick = { navController?.navigate("myTicket/${event.id}") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("My ticket")
                    }
                }
            }
        }

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (!isUserAttending) {
                            viewModel.updateRSVPStatus(event.id, "attending")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isUserAttending
                ) {
                    Text("Attending")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (isUserAttending) {
                            viewModel.updateRSVPStatus(event.id, "not attending")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = isUserAttending
                ) {
                    Text("Not Attending")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (event.location.isNotEmpty()) {
                        openMap(event.location, context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View on Map")
            }
        }
    }
}

private fun ticketStatusText(
    ticket: Ticket?,
    ticketingEnabled: Boolean,
    eventPaymentRequired: Boolean
): String {
    if (!ticketingEnabled) return "no ticket"

    if (ticket == null) {
        return if (eventPaymentRequired) "needs payment" else "free ticket"
    }

    if (ticket.checkedInAt != null) return "checked-in"

    return if (ticket.paymentRequired) {
        if (ticket.paymentStatus == PaymentStatus.CONFIRMED.value) "confirmed" else "needs payment"
    } else {
        "confirmed"
    }
}

private fun openMap(location: String, context: Context) {
    val locationQuery = Uri.encode(location)
    val gmmIntentUri = Uri.parse("geo:0,0?q=$locationQuery")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    Log.d("MapLocation", "Location passed: $location")

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        val fallbackIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        context.startActivity(fallbackIntent)
    }
}
