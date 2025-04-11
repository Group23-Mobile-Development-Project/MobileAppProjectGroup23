package com.example.eventplanner.data

import com.example.eventplanner.data.model.Event
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
        return try {
            val snapshot = db.collection("events").get().await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
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

    suspend fun getEventById(eventId: String): Event? {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("events")
            .document(eventId)
            .get()
            .await()

        return if (snapshot.exists()) {
            snapshot.toObject(Event::class.java)?.copy(id = snapshot.id)
        } else {
            null
        }
    }

}
