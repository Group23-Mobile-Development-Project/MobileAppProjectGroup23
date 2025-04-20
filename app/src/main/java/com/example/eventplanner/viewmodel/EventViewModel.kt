package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.FirestoreHelper
import com.example.eventplanner.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
// At the top of the file

import android.util.Log




class EventViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser

    private val firestoreHelper = FirestoreHelper()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    // Fetch all events for the logged-in user
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

    // ✅ Fetch all events for everyone
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

    // Create a new event
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

    // Fetch the event by ID
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

    // RSVP to the event (Attending or Not Attending)
    fun updateRSVPStatus(eventId: String, status: String) {
        val userId = currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = firestoreHelper.updateRSVPStatus(eventId, userId, status)
                if (updated) {
                    fetchEventById(eventId) // Refresh event data

                    // ✅ Subscribe or Unsubscribe to topic
                    val topic = "event_$eventId"
                    if (status == "Attending") {
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


    fun saveUserFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .update("fcmToken", token)
            }
        }
    }


    // Delete an event by ID
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
                    fetchEventById(eventId) // Refresh the selected event
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



}

