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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.eventplanner.R
import com.example.eventplanner.data.repository.TicketTokenUtil
import com.example.eventplanner.utils.QrCodeUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private data class MyTicketUi(
    val docPath: String,
    val ticketId: String,
    val eventId: String,
    val userId: String,
    val userName: String?,
    val qrToken: String?,
    val checkedInAt: Any?,
    val bookedAt: Any?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketScreen(
    eventId: String,
    navController: NavHostController
) {
    val db = remember { FirebaseFirestore.getInstance() }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var ticket by remember { mutableStateOf<MyTicketUi?>(null) }
    var tokenWriteError by remember { mutableStateOf<String?>(null) }

    fun fetchTicket() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            error = "Please login first"
            isLoading = false
            ticket = null
            return
        }

        val uid = user.uid
        isLoading = true
        error = null
        tokenWriteError = null
        ticket = null

        val subRef = db.collection("events").document(eventId).collection("tickets").document(uid)
        val topLevelId = "${eventId}_${uid}"
        val topRef = db.collection("tickets").document(topLevelId)

        subRef.get()
            .addOnSuccessListener { subDoc ->
                if (subDoc.exists()) {
                    ticket = MyTicketUi(
                        docPath = subRef.path,
                        ticketId = subDoc.id,
                        eventId = subDoc.getString("eventId") ?: eventId,
                        userId = subDoc.getString("userId") ?: uid,
                        userName = subDoc.getString("userName"),
                        qrToken = subDoc.getString("qrToken") ?: subDoc.getString("token"),
                        checkedInAt = subDoc.get("checkedInAt"),
                        bookedAt = subDoc.get("bookedAt")
                    )
                    isLoading = false
                } else {
                    topRef.get()
                        .addOnSuccessListener { topDoc ->
                            if (topDoc.exists()) {
                                ticket = MyTicketUi(
                                    docPath = topRef.path,
                                    ticketId = topDoc.id,
                                    eventId = topDoc.getString("eventId") ?: eventId,
                                    userId = topDoc.getString("userId") ?: uid,
                                    userName = topDoc.getString("userName"),
                                    qrToken = topDoc.getString("qrToken") ?: topDoc.getString("token"),
                                    checkedInAt = topDoc.get("checkedInAt"),
                                    bookedAt = topDoc.get("bookedAt")
                                )
                            } else {
                                ticket = null
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { ex ->
                            error = ex.message ?: "Failed to load ticket"
                            isLoading = false
                        }
                }
            }
            .addOnFailureListener { ex ->
                error = ex.message ?: "Failed to load ticket"
                isLoading = false
            }
    }

    fun generateAndSaveToken(t: MyTicketUi) {
        val newToken = TicketTokenUtil.generateTokenUrlSafe()
        val ref = db.document(t.docPath)

        tokenWriteError = null

        ref.set(mapOf("qrToken" to newToken), SetOptions.merge())
            .addOnSuccessListener {
                ticket = t.copy(qrToken = newToken)
            }
            .addOnFailureListener { ex ->
                tokenWriteError = ex.message ?: "Failed to save qrToken"
            }
    }

    LaunchedEffect(eventId) { fetchTicket() }

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
            if (isLoading) {
                CircularProgressIndicator()
                return@Column
            }

            if (error != null) {
                Text("Error: ${error ?: "Unknown"}", color = MaterialTheme.colorScheme.error)
                Button(onClick = { fetchTicket() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Retry")
                }
                return@Column
            }

            val t = ticket
            if (t == null) {
                Text("No ticket found for this event.")
                TextButton(onClick = { fetchTicket() }) { Text("Refresh") }
                return@Column
            }

            Text("Ticket found")
            Text("Ticket id: ${t.ticketId}")
            Text("Name: ${t.userName ?: "-"}")
            Text("Checked in: ${if (t.checkedInAt != null) "yes" else "no"}")

            val token = t.qrToken
            if (token.isNullOrBlank()) {
                Text("QR token is missing.")
                if (tokenWriteError != null) {
                    Text("Reason: ${tokenWriteError ?: ""}", color = MaterialTheme.colorScheme.error)
                    Text("If this says permission denied, you must rebook after updating booking code or change Firestore rules.")
                }

                Button(
                    onClick = { generateAndSaveToken(t) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate QR token")
                }

                TextButton(onClick = { fetchTicket() }) { Text("Refresh") }
                return@Column
            }

            val payload = remember(t.ticketId, t.eventId, token) {
                QrCodeUtil.buildPayload(ticketId = t.ticketId, eventId = t.eventId, token = token)
            }

            val sizeDp = 240.dp
            val sizePx = with(LocalDensity.current) { sizeDp.roundToPx() }
            val qrBitmap = remember(payload) { QrCodeUtil.generateQrBitmap(payload, sizePx) }

            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Ticket QR code",
                modifier = Modifier.size(sizeDp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = { fetchTicket() }) { Text("Refresh") }
        }
    }
}
