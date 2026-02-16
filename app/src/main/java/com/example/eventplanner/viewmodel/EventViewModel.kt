package com.example.eventplanner.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.FirestoreHelper
import com.example.eventplanner.data.model.Event
import com.example.eventplanner.data.model.PaymentStatus
import com.example.eventplanner.data.model.Ticket
import com.example.eventplanner.data.repository.TicketTokenUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    private val firestoreHelper = FirestoreHelper()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    fun fetchUserEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = currentUser?.uid ?: return@launch
                val eventList = firestoreHelper.getEventsByUser(userId)
                _events.value = eventList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAllEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val eventList = firestoreHelper.getAllEvents()
                _events.value = eventList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch all events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = firestoreHelper.addEvent(event)
                if (success) {
                    _events.value = _events.value + event
                    _error.value = null
                } else {
                    _error.value = "Failed to create event"
                }
            } catch (e: Exception) {
                _error.value = "Error creating event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchEventById(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val event = firestoreHelper.getEventById(eventId)
                _selectedEvent.value = event
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRSVPStatus(eventId: String, status: String) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = firestoreHelper.updateRSVPStatus(eventId, userId, status)
                if (updated) {
                    fetchEventById(eventId)

                    val topic = "event_$eventId"
                    if (status == "attending") {
                        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("FCM", "Subscribed to topic: $topic")
                                } else {
                                    Log.e("FCM", "Subscription failed", task.exception)
                                }
                            }
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("FCM", "Unsubscribed from topic: $topic")
                                } else {
                                    Log.e("FCM", "Unsubscription failed", task.exception)
                                }
                            }
                    }

                    _error.value = null
                } else {
                    _error.value = "Failed to update RSVP status"
                }
            } catch (e: Exception) {
                _error.value = "Error updating RSVP status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = firestoreHelper.deleteEvent(eventId)
                if (success) {
                    _events.value = _events.value.filterNot { it.id == eventId }
                    _error.value = null
                } else {
                    _error.value = "Failed to delete event"
                }
            } catch (e: Exception) {
                _error.value = "Error deleting event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEventDetails(eventId: String, updatedFields: Map<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = firestoreHelper.updateEventDetails(eventId, updatedFields)
                if (success) {
                    _error.value = null
                    fetchEventById(eventId)
                } else {
                    _error.value = "Failed to update event"
                }
            } catch (e: Exception) {
                _error.value = "Error updating event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Option A (fixed): one atomic transaction:
    // - reads event + ticket first
    // - then updates attendees + writes ticket
    fun getFreeTicketAndAttend(eventId: String, onDone: (() -> Unit)? = null) {
        val user = currentUser ?: run {
            _error.value = "Not signed in"
            return
        }

        val uid = user.uid
        val userName = user.displayName ?: user.email ?: "Unknown"

        val eventRef = db.collection("events").document(eventId)
        val ticketId = "${eventId}_${uid}"
        val ticketRef = db.collection("tickets").document(ticketId)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.runTransaction { txn ->
                    // READS FIRST (important!)
                    val eventSnap = txn.get(eventRef)
                    val existingTicketSnap = txn.get(ticketRef)

                    // build/update attendees list
                    val current = eventSnap.get("attendees") as? List<*>
                    val currentMaps = current?.mapNotNull { it as? Map<String, Any> } ?: emptyList()

                    var found = false
                    val updated = currentMaps.map { m ->
                        val mUid = m["userId"] as? String
                        if (mUid == uid) {
                            found = true
                            mapOf(
                                "userId" to uid,
                                "userName" to userName,
                                "status" to "attending"
                            )
                        } else {
                            m
                        }
                    }.toMutableList()

                    if (!found) {
                        updated.add(
                            mapOf(
                                "userId" to uid,
                                "userName" to userName,
                                "status" to "attending"
                            )
                        )
                    }

                    // ticket: keep existing token if present, otherwise generate
                    val rawToken = existingTicketSnap.getString("qrToken")
                        ?: TicketTokenUtil.generateTokenUrlSafe()

                    val tokenHash = TicketTokenUtil.sha256Hex(rawToken)
                    val now = Timestamp.now()

                    val ticket = Ticket(
                        id = ticketId,
                        eventId = eventId,
                        userId = uid,
                        userName = userName,
                        issuedAt = existingTicketSnap.getTimestamp("issuedAt") ?: now,
                        paymentRequired = false,
                        paymentStatus = PaymentStatus.CONFIRMED.value,
                        paymentIntentId = null,
                        qrTokenHash = tokenHash,
                        qrToken = rawToken,
                        checkedInAt = null,
                        checkedInBy = null,
                        updatedAt = now
                    )

                    // WRITES AFTER READS
                    txn.update(eventRef, "attendees", updated)
                    txn.set(ticketRef, ticket, SetOptions.merge())

                    null
                }.addOnSuccessListener {
                    fetchEventById(eventId)

                    val topic = "event_$eventId"
                    FirebaseMessaging.getInstance().subscribeToTopic(topic)

                    _error.value = null
                    onDone?.invoke()
                }.addOnFailureListener { e ->
                    _error.value = "Failed to get free ticket: ${e.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to get free ticket: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
