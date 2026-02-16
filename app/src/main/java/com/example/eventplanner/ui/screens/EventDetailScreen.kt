package com.example.eventplanner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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

    var bookingInProgress by remember { mutableStateOf(false) }
    var isBooked by remember { mutableStateOf(false) }

    fun refreshBookedState() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            isBooked = false
            return
        }

        db.collection("events")
            .document(eventId)
            .collection("tickets")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                isBooked = doc.exists()
            }
            .addOnFailureListener {
                // if we can't read ticket for some reason, don't block booking UI
                isBooked = false
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

                // also refresh booking status after event is loaded
                refreshBookedState()
            }
            .addOnFailureListener { ex ->
                error = ex.message ?: "Failed to load event"
                isLoading = false
            }
    }

    fun bookTicket() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(ctx, "Please login first", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
            return
        }

        val uid = user.uid
        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "User"

        bookingInProgress = true

        val eventRef = db.collection("events").document(eventId)
        val ticketRef = eventRef.collection("tickets").document(uid)

        db.runTransaction { txn ->
            // prevent double booking: if ticket exists, stop
            val existingTicket = txn.get(ticketRef)
            if (existingTicket.exists()) {
                throw IllegalStateException("You already booked this event")
            }

            val eventSnap = txn.get(eventRef)

            val seatsField = when {
                eventSnap.contains("availableSeats") -> "availableSeats"
                eventSnap.contains("capacity") -> "capacity"
                eventSnap.contains("seats") -> "seats"
                else -> null
            }

            if (seatsField != null) {
                val currentSeats = (eventSnap.get(seatsField) as? Long) ?: 0L
                if (currentSeats <= 0L) {
                    throw IllegalStateException("No seats available")
                }
                txn.update(eventRef, seatsField, currentSeats - 1L)
            }

            val ticketData = hashMapOf(
                "userId" to uid,
                "userName" to displayName,
                "bookedAt" to FieldValue.serverTimestamp(),
                "checkedInAt" to null,
                "eventId" to eventId
            )

            txn.set(ticketRef, ticketData, SetOptions.merge())
            null
        }.addOnSuccessListener {
            bookingInProgress = false
            isBooked = true
            Toast.makeText(ctx, "Ticket booked successfully", Toast.LENGTH_SHORT).show()

            // refresh event (capacity) and move user to bookings screen
            readEvent()
            navController.navigate("participation")
        }.addOnFailureListener { ex ->
            bookingInProgress = false
            Toast.makeText(ctx, ex.message ?: "Booking failed", Toast.LENGTH_SHORT).show()

            // if it failed because already booked, update UI state
            refreshBookedState()
        }
    }

    LaunchedEffect(eventId) {
        readEvent()
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val isOrganizer = userId != null && organizerId != null && userId == organizerId

    Scaffold(
        topBar = { TopAppBar(title = { Text("Event Details") }) }
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.navigateUp() }) { Text("Back") }
                }

                else -> {
                    Text(text = title ?: "(No title)")
                    Text(text = "Date: ${date ?: "-"}")
                    Text(text = "Time: ${time ?: "-"}")
                    Text(text = "Location: ${location ?: "-"}")
                    Text(text = "Price: ${price ?: "-"}")
                    Text(text = "Capacity: ${capacity ?: "-"}")

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isOrganizer) {
                        Button(
                            onClick = { navController.navigate("organizerDashboard/$eventId") },
                            enabled = !bookingInProgress
                        ) {
                            Text("Open organizer dashboard")
                        }
                    } else {
                        if (isBooked) {
                            Button(onClick = { navController.navigate("participation") }) {
                                Text("View my booking")
                            }
                        } else {
                            Button(
                                onClick = { bookTicket() },
                                enabled = !bookingInProgress
                            ) {
                                Text(if (bookingInProgress) "Booking..." else "Book ticket")
                            }
                        }
                    }

                    Button(onClick = { navController.navigateUp() }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
