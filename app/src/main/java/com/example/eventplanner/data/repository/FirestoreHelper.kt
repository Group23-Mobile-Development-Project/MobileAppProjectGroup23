import com.example.eventplanner.data.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    fun addEvent(event: Event): Boolean {
        return try {
            db.collection("events").add(event)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Function to fetch events from Firestore
    suspend fun getEvents(): List<Event> {
        return try {
            val snapshot = db.collection("events").get().await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
