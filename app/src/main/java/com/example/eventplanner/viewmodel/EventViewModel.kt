package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.FirestoreHelper
import com.example.eventplanner.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val firestoreHelper = FirestoreHelper()
    private val auth = FirebaseAuth.getInstance()

    // StateFlow to store events list
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    // Fetch all events (for HomeScreen)
    fun fetchAllEvents() {
        viewModelScope.launch {
            val eventList = firestoreHelper.getAllEvents()
            _events.value = eventList
        }
    }

    // Fetch events for the logged-in user (for EventScreen)
    fun fetchUserEvents() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val eventList = firestoreHelper.getEventsByUser(userId)
                _events.value = eventList
            } else {
                _events.value = emptyList()
            }
        }
    }

    // Create a new event
    fun createEvent(title: String, description: String, date: String, location: String) {
        val user = auth.currentUser

        if (user != null) {
            val organizerId = user.uid

            viewModelScope.launch {
                try {
                    val userDoc = firestoreHelper.getUserDetails(organizerId)
                    val organizerName = userDoc?.getString("name") ?: "Unknown"

                    val event = Event(
                        id = "",
                        title = title,
                        description = description,
                        date = date,
                        location = location,
                        organizerId = organizerId,
                        organizerName = organizerName
                    )

                    val success = firestoreHelper.addEvent(event)
                    if (success) {
                        // Fetch the latest events after creating a new one
                        fetchUserEvents() // Refresh for EventScreen or fetchAllEvents() for HomeScreen
                    }
                } catch (e: Exception) {
                    println("Error creating event: ${e.message}")
                }
            }
        }
    }
}
