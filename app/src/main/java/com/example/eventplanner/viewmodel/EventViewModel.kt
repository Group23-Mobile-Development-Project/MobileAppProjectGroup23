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

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    fun fetchAllEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val eventList = firestoreHelper.getAllEvents()
                _events.value = eventList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid
                _events.value = if (userId != null) {
                    firestoreHelper.getEventsByUser(userId)
                } else {
                    emptyList()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch user events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchEventById(eventId: String) {
        viewModelScope.launch {
            try {
                val event = firestoreHelper.getEventById(eventId)
                _selectedEvent.value = event
            } catch (e: Exception) {
                _error.value = "Failed to fetch event: ${e.message}"
            }
        }
    }

    fun createEvent(title: String, description: String, date: String, location: String) {
        val user = auth.currentUser ?: return
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
                    fetchUserEvents()
                }
            } catch (e: Exception) {
                _error.value = "Error creating event: ${e.message}"
            }
        }
    }
}
