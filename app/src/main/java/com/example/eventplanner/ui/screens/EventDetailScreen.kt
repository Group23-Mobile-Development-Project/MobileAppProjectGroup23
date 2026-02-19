package com.example.eventplanner.ui.screens

import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    navController: NavHostController
) {
    val ctx = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf<String?>(null) }
    var time by remember { mutableStateOf<String?>(null) }
    var location by remember { mutableStateOf<String?>(null) }
    var price by remember { mutableStateOf<String?>(null) }
    var capacity by remember { mutableStateOf<String?>(null) }
    var organizerId by remember { mutableStateOf<String?>(null) }

    var actionInProgress by remember { mutableStateOf(false) }
    var isBooked by remember { mutableStateOf(false) }
    var rsvpStatus by remember { mutableStateOf<String?>(null) } // "attending" / "not attending" / null

    fun generateQrTokenUrlSafe(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun getUid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    fun refreshTicketAndRsvp() {
        val uid = getUid()
        if (uid == null) {
            isBooked = false
            rsvpStatus = null
            return
        }

        val eventRef = db.collection("events").document(eventId)
        val ticketRef = eventRef.collection("tickets").document(uid)

        ticketRef.get()
            .addOnSuccessListener { t ->
                isBooked = t.exists()
            }
            .addOnFailureListener {
                isBooked = false
            }

        eventRef.get()
            .addOnSuccessListener { doc ->
                val attendees = doc.get("attendees") as? List<*>
                val found = attendees
                    ?.mapNotNull { it as? Map<*, *> }
                    ?.firstOrNull { (it["userId"] as? String) == uid }

                rsvpStatus = (found?.get("status") as? String)
            }
            .addOnFailureListener {
                rsvpStatus = null
            }
    }

    fun readEvent() {
        isLoading = true
        error = null

        db.collection("events")
            .document(eventId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    error = "Event not found"
                    isLoading = false
                    return@addOnSuccessListener
                }

                fun getAnyString(vararg keys: String): String? {
                    for (k in keys) {
                        val v = doc.get(k)
                        if (v is String && v.isNotBlank()) return v
                    }
                    return null
                }

                fun getAnyNumberAsString(vararg keys: String): String? {
                    for (k in keys) {
                        val v = doc.get(k)
                        when (v) {
                            is Long -> return v.toString()
                            is Int -> return v.toString()
                            is Double -> return if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
                            is Float -> return v.toString()
                            is String -> if (v.isNotBlank()) return v
                        }
                    }
                    return null
                }

                title = getAnyString("title", "eventName", "name")
                date = getAnyString("date", "eventDate")
                time = getAnyString("time", "eventTime")
                location = getAnyString("location", "venue", "place")
                price = getAnyNumberAsString("price", "ticketPrice", "amount")
                capacity = getAnyNumberAsString("capacity", "availableSeats", "seats")
                organizerId = getAnyString("organizerId", "organizerUid", "createdBy")

                isLoading = false
                refreshTicketAndRsvp()
            }
            .addOnFailureListener { ex ->
                error = ex.message ?: "Failed to load event"
                isLoading = false
            }
    }

    fun updateRsvpOnly(status: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(ctx, "Please login first", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
            return
        }

        val uid = user.uid
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "User"

        actionInProgress = true
        val eventRef = db.collection("events").document(eventId)

        db.runTransaction { txn ->
            val snap = txn.get(eventRef)

            val current = (snap.get("attendees") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
            val cleaned = current.filterNot { (it["userId"] as? String) == uid }.toMutableList()

            cleaned.add(
                hashMapOf(
                    "userId" to uid,
                    "userName" to name,
                    "status" to status
                )
            )

            txn.update(eventRef, "attendees", cleaned)
            null
        }.addOnSuccessListener {
            actionInProgress = false
            rsvpStatus = status
            Toast.makeText(ctx, "RSVP updated: $status", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { ex ->
            actionInProgress = false
            Toast.makeText(ctx, ex.message ?: "Failed to update RSVP", Toast.LENGTH_SHORT).show()
        }
    }

    fun bookTicketAndAttend() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(ctx, "Please login first", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
            return
        }

        val uid = user.uid
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "User"

        val qrToken = generateQrTokenUrlSafe()

        actionInProgress = true

        val eventRef = db.collection("events").document(eventId)
        val ticketRef = eventRef.collection("tickets").document(uid)

        db.runTransaction { txn ->
            val existing = txn.get(ticketRef)
            if (existing.exists()) throw IllegalStateException("You already booked this event")

            val eventSnap = txn.get(eventRef)

            val seatsField = when {
                eventSnap.contains("availableSeats") -> "availableSeats"
                eventSnap.contains("capacity") -> "capacity"
                eventSnap.contains("seats") -> "seats"
                else -> null
            }

            if (seatsField != null) {
                val currentSeats = (eventSnap.get(seatsField) as? Long) ?: 0L
                if (currentSeats <= 0L) throw IllegalStateException("No seats available")
                txn.update(eventRef, seatsField, currentSeats - 1L)
            }

            val ticketData = hashMapOf(
                "userId" to uid,
                "userName" to name,
                "bookedAt" to FieldValue.serverTimestamp(),
                "checkedInAt" to null,
                "eventId" to eventId,
                "qrToken" to qrToken
            )
            txn.set(ticketRef, ticketData, SetOptions.merge())

            val currentAtt = (eventSnap.get("attendees") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
            val cleaned = currentAtt.filterNot { (it["userId"] as? String) == uid }.toMutableList()
            cleaned.add(hashMapOf("userId" to uid, "userName" to name, "status" to "attending"))
            txn.update(eventRef, "attendees", cleaned)

            null
        }.addOnSuccessListener {
            actionInProgress = false
            isBooked = true
            rsvpStatus = "attending"
            Toast.makeText(ctx, "Ticket booked and marked attending", Toast.LENGTH_SHORT).show()
            navController.navigate("myTicket/$eventId")
        }.addOnFailureListener { ex ->
            actionInProgress = false
            Toast.makeText(ctx, ex.message ?: "Booking failed", Toast.LENGTH_SHORT).show()
            refreshTicketAndRsvp()
        }
    }

    fun cancelBookingAndNotAttend() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(ctx, "Please login first", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
            return
        }

        val uid = user.uid
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "User"

        actionInProgress = true

        val eventRef = db.collection("events").document(eventId)
        val ticketRef = eventRef.collection("tickets").document(uid)

        db.runTransaction { txn ->
            val eventSnap = txn.get(eventRef)
            val ticketSnap = txn.get(ticketRef)

            if (ticketSnap.exists()) {
                txn.delete(ticketRef)

                val seatsField = when {
                    eventSnap.contains("availableSeats") -> "availableSeats"
                    eventSnap.contains("capacity") -> "capacity"
                    eventSnap.contains("seats") -> "seats"
                    else -> null
                }

                if (seatsField != null) {
                    val currentSeats = (eventSnap.get(seatsField) as? Long) ?: 0L
                    txn.update(eventRef, seatsField, currentSeats + 1L)
                }
            }

            val currentAtt = (eventSnap.get("attendees") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
            val cleaned = currentAtt.filterNot { (it["userId"] as? String) == uid }.toMutableList()
            cleaned.add(hashMapOf("userId" to uid, "userName" to name, "status" to "not attending"))
            txn.update(eventRef, "attendees", cleaned)

            null
        }.addOnSuccessListener {
            actionInProgress = false
            isBooked = false
            rsvpStatus = "not attending"
            Toast.makeText(ctx, "Booking cancelled and marked not attending", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { ex ->
            actionInProgress = false
            Toast.makeText(ctx, ex.message ?: "Cancel failed", Toast.LENGTH_SHORT).show()
            refreshTicketAndRsvp()
        }
    }

    LaunchedEffect(eventId) { readEvent() }

    val uid = getUid()
    val isOrganizer = uid != null && organizerId != null && uid == organizerId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading...")
                    }
                }

                error != null -> {
                    Text("Error: ${error ?: "Unknown"}")
                    Button(onClick = { navController.navigateUp() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Back")
                    }
                }

                else -> {
                    Text(text = title ?: "(No title)")
                    Text(text = "Date: ${date ?: "-"}")
                    Text(text = "Time: ${time ?: "-"}")
                    Text(text = "Location: ${location ?: "-"}")
                    Text(text = "Price: ${price ?: "-"}")
                    Text(text = "Capacity: ${capacity ?: "-"}")

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "RSVP: ${rsvpStatus ?: "-"}")

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isOrganizer) {
                        Button(
                            onClick = { navController.navigate("organizerDashboard/$eventId") },
                            enabled = !actionInProgress,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open organizer dashboard")
                        }
                    } else {
                        if (isBooked) {
                            Button(
                                onClick = { navController.navigate("myTicket/$eventId") },
                                enabled = !actionInProgress,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View ticket QR")
                            }

                            Button(
                                onClick = { cancelBookingAndNotAttend() },
                                enabled = !actionInProgress,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (actionInProgress) "Please wait..." else "Cancel booking")
                            }
                        } else {
                            Button(
                                onClick = { bookTicketAndAttend() },
                                enabled = !actionInProgress,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (actionInProgress) "Please wait..." else "Book ticket")
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = { updateRsvpOnly("not attending") },
                                enabled = !actionInProgress,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Not attending")
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { navController.navigate("participation") },
                            enabled = !actionInProgress,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go to My Participation")
                        }
                    }

                    Button(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
