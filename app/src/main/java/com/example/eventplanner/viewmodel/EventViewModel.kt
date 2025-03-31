import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.model.Event

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {
    private val firestoreHelper = FirestoreHelper()
    private val auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth

    // StateFlow to store events list
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    init {
        fetchEvents() // Fetch events when ViewModel is initialized
    }

    fun createEvent(title: String, description: String, date: String, location: String, organizerId: String) {
        val userId = auth.currentUser?.uid  // Get the logged-in user's ID

        if (userId != null) {
            val event = Event("", title, description, date, location, organizerId)

            viewModelScope.launch {
                val success = firestoreHelper.addEvent(event)
                if (success) {
                    println("Event added successfully!")
                    fetchEvents() // Refresh events list after adding
                } else {
                    println("Failed to add event")
                }
            }
        } else {
            println("User is not authenticated.")
        }
    }

    fun fetchEvents() {
        viewModelScope.launch {
            val eventList = firestoreHelper.getEvents() // Get events from Firestore
            _events.value = eventList
        }
    }
}
