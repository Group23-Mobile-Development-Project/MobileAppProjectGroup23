package com.example.eventplanner.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eventplanner.R
import com.example.eventplanner.data.model.PaymentStatus
import com.example.eventplanner.data.model.Ticket
import com.example.eventplanner.data.model.TicketType
import com.example.eventplanner.utils.QrCodeUtil
import com.example.eventplanner.viewmodel.EventViewModel
import com.example.eventplanner.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketScreen(
    eventId: String,
    navController: NavHostController,
    eventViewModel: EventViewModel = viewModel(),
    ticketViewModel: TicketViewModel = viewModel()
) {
    LaunchedEffect(eventId) {
        eventViewModel.fetchEventById(eventId)
        ticketViewModel.start(eventId)
    }

    val event by eventViewModel.selectedEvent.collectAsState()
    val ticket by ticketViewModel.ticket.collectAsState()
    val isLoading by ticketViewModel.isLoading.collectAsState()
    val error by ticketViewModel.error.collectAsState()

    val ticketingEnabled = event?.ticketingEnabled == true
    val paymentRequired = if (event == null) {
        false
    } else {
        event!!.ticketingEnabled && event!!.ticketType == TicketType.PAID.value
    }

    val statusText = ticketStatusText(ticket, ticketingEnabled, paymentRequired)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My ticket") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Status: $statusText")

            if (!ticketingEnabled) {
                Text("This event does not use tickets.")
                return@Column
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            if (ticket == null) {
                Text("No ticket found for this event.")
                Button(
                    onClick = { ticketViewModel.createOrGetTicket(eventId, paymentRequired) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (paymentRequired) "Create ticket (payment needed)" else "Create free ticket")
                }
                return@Column
            }

            val token = ticketViewModel.getQrTokenFor(ticket!!)
            if (token.isNullOrBlank()) {
                Text("QR token is missing for this ticket.")
                Button(
                    onClick = { ticketViewModel.rotateQrToken(ticket!!.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Regenerate QR token")
                }
                return@Column
            }

            val payload = remember(ticket!!.id, ticket!!.eventId, token) {
                QrCodeUtil.buildPayload(ticketId = ticket!!.id, eventId = ticket!!.eventId, token = token)
            }

            val sizeDp = 240.dp
            val sizePx = with(LocalDensity.current) { sizeDp.roundToPx() }
            val qrBitmap = remember(payload) {
                QrCodeUtil.generateQrBitmap(payload, sizePx)
            }

            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Ticket QR code",
                modifier = Modifier.size(sizeDp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(onClick = { ticketViewModel.refresh() }) {
                Text("Refresh")
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
