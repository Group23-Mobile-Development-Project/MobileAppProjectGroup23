package com.example.eventplanner.data

import com.example.eventplanner.data.model.Event
import com.example.eventplanner.data.model.Attendee
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    // Function to add an event
    suspend fun addEvent(event: Event): Boolean {
        return try {
            val newEventRef = db.collection("events").document()
            val eventWithId = event.copy(id = newEventRef.id)
            newEventRef.set(eventWithId).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Function to get all events
    suspend fun getAllEvents(): List<Event> {
        return db.collection("events")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Event::class.java)?.copy(id = it.id) }
    }


    // Function to get events by a specific user
    suspend fun getEventsByUser(userId: String): List<Event> {
        return try {
            val snapshot = db.collection("events")
                .whereEqualTo("organizerId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Function to get user details (e.g., name)
    suspend fun getUserDetails(userId: String) = try {
        val doc = db.collection("users").document(userId).get().await()
        doc
    } catch (e: Exception) {
        null
    }

    // Function to get event by ID
    suspend fun getEventById(eventId: String): Event? {
        val snapshot = db.collection("events")
            .document(eventId)
            .get()
            .await()

        return if (snapshot.exists()) {
            snapshot.toObject(Event::class.java)?.copy(id = snapshot.id)
        } else {
            null
        }
    }

    // Function to update RSVP status for a user
    suspend fun updateRSVPStatus(eventId: String, userId: String, status: String): Boolean {
        return try {
            val eventRef = db.collection("events").document(eventId)
            val event = eventRef.get().await().toObject(Event::class.java)

            if (event != null) {
                // Get current attendees list and modify it
                val attendees = event.attendees.toMutableList()

                // Remove the user if already exists and add the updated status
                attendees.removeAll { it.userId == userId }
                attendees.add(Attendee(userId = userId, status = status))

                // Update the event document with the new attendees list
                eventRef.update("attendees", attendees).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Function to fetch attendees count
    suspend fun getAttendeesCount(eventId: String): Int {
        val eventDoc = db.collection("events").document(eventId).get().await()
        val attendees = eventDoc.get("attendees") as? List<Map<String, Any>> ?: emptyList()
        return attendees.size
    }

    // Function to fetch all attendees for the event
    suspend fun getAttendees(eventId: String): List<Map<String, Any>> {
        val eventDoc = db.collection("events").document(eventId).get().await()
        return eventDoc.get("attendees") as? List<Map<String, Any>> ?: emptyList()
    }

    // Function to save RSVP to the event
    suspend fun saveRsvp(eventId: String, userId: String, userName: String, status: String): Boolean {
        return try {
            val eventRef = db.collection("events").document(eventId)

            // Fetch the current event's attendees list
            val eventDoc = eventRef.get().await()
            val attendees = eventDoc.get("attendees") as? MutableList<Map<String, Any>> ?: mutableListOf()

            // Check if user has already RSVP'd
            val existingRsvp = attendees.find { it["userId"] == userId }
            if (existingRsvp != null) {
                return false // User has already RSVP'd
            }

            // Add current user's RSVP status
            attendees.add(mapOf("userId" to userId, "userName" to userName, "status" to status))

            // Update the event document with the new attendees list
            eventRef.update("attendees", attendees).await()

            true
        } catch (e: Exception) {
            false
        }
    }
}
