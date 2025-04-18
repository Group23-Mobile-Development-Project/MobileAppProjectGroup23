package com.example.eventplanner.data

import com.example.eventplanner.data.model.Event
import com.example.eventplanner.data.model.Attendee
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

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

    suspend fun getAllEvents(): List<Event> {
        return db.collection("events")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Event::class.java)?.copy(id = it.id) }
    }

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

    suspend fun getUserDetails(userId: String) = try {
        val doc = db.collection("users").document(userId).get().await()
        doc
    } catch (e: Exception) {
        null
    }

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
            val eventSnapshot = eventRef.get().await()
            val event = eventSnapshot.toObject(Event::class.java)

            if (event != null) {
                val attendees = event.attendees.toMutableList()

                // Remove the current user from attendees
                attendees.removeAll { it.userId == userId }

                if (status == "attending") {
                    // Add user back only if they are attending
                    val userDoc = db.collection("users").document(userId).get().await()
                    val userName = userDoc.getString("name") ?: "Unknown"
                    attendees.add(Attendee(userId = userId, userName = userName, status = status))
                }

                // Update the event with the modified attendees list
                eventRef.update("attendees", attendees).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    suspend fun getAttendeesCount(eventId: String): Int {
        val eventDoc = db.collection("events").document(eventId).get().await()
        val attendees = eventDoc.get("attendees") as? List<Map<String, Any>> ?: emptyList()
        return attendees.size
    }

    suspend fun getAttendees(eventId: String): List<Map<String, Any>> {
        val eventDoc = db.collection("events").document(eventId).get().await()
        return eventDoc.get("attendees") as? List<Map<String, Any>> ?: emptyList()
    }

    suspend fun saveRsvp(eventId: String, userId: String, userName: String, status: String): Boolean {
        return try {
            val eventRef = db.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()
            val attendees = eventDoc.get("attendees") as? MutableList<Map<String, Any>> ?: mutableListOf()

            val existingRsvp = attendees.find { it["userId"] == userId }
            if (existingRsvp != null) {
                return false
            }

            attendees.add(mapOf("userId" to userId, "userName" to userName, "status" to status))
            eventRef.update("attendees", attendees).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteEvent(eventId: String): Boolean {
        return try {
            FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

}
