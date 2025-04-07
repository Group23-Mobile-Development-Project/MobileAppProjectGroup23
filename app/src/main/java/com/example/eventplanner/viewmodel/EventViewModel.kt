    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.eventplanner.data.model.Event
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await

    class EventViewModel : ViewModel() {
        private val firestoreHelper = FirestoreHelper()
        private val auth = FirebaseAuth.getInstance()
        private val db = FirebaseFirestore.getInstance()

        // StateFlow to store events list
        private val _events = MutableStateFlow<List<Event>>(emptyList())
        val events: StateFlow<List<Event>> = _events

        init {
            fetchEvents() // Fetch events when ViewModel is initialized
        }

        fun createEvent(title: String, description: String, date: String, location: String) {
            val user = auth.currentUser

            if (user != null) {
                val organizerId = user.uid

                viewModelScope.launch {
                    try {
                        // ✅ Fetch organizer's name from Firestore
                        val userDoc = db.collection("users").document(organizerId).get().await()
                        val organizerName = userDoc.getString("name") ?: "Unknown"

                        // ✅ Create event with name
                        val event = Event("", title, description, date, location, organizerId, organizerName)

                        val success = firestoreHelper.addEvent(event)
                        if (success) {
                            println("Event added successfully!")
                            fetchEvents() // Refresh events list after adding
                        } else {
                            println("Failed to add event")
                        }
                    } catch (e: Exception) {
                        println("Error fetching organizer name: ${e.message}")
                    }
                }
            } else {
                println("User is not authenticated.")
            }
        }

        fun fetchEvents() {
            viewModelScope.launch {
                val eventList = firestoreHelper.getEvents()
                _events.value = eventList
            }
        }
    }
