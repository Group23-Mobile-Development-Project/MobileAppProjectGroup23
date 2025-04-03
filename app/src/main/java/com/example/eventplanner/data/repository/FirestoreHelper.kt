import com.example.eventplanner.data.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addEvent(event: Event): Boolean {
        return try {
            val newEventRef = db.collection("events").document()
            val eventWithId = event.copy(id = newEventRef.id) // Assign Firestore ID
            newEventRef.set(eventWithId).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Function to fetch events, including organizer names
    suspend fun getEvents(): List<Event> {
        return try {
            val snapshot = db.collection("events").get().await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
