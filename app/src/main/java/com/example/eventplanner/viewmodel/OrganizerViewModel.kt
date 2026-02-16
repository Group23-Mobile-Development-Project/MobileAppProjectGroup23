package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import com.example.eventplanner.ui.screens.TicketCounts
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TicketUi(
    val id: String = "",
    val userName: String? = null,
    val checkedInAt: Timestamp? = null
)

class OrganizerViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _tickets = MutableStateFlow<List<TicketUi>>(emptyList())
    val tickets: StateFlow<List<TicketUi>> = _tickets.asStateFlow()

    private val _ticketCounts = MutableStateFlow(TicketCounts(issued = 0, checkedIn = 0, notArrived = 0))
    val ticketCounts: StateFlow<TicketCounts> = _ticketCounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentEventId: String? = null
    private var ticketListener: ListenerRegistration? = null

    fun loadTicketsForEvent(eventId: String) {
        if (currentEventId == eventId && ticketListener != null) return

        currentEventId = eventId
        _isLoading.value = true
        _error.value = null

        ticketListener?.remove()

        // Assumption: events/{eventId}/tickets
        ticketListener = db
            .collection("events")
            .document(eventId)
            .collection("tickets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message ?: "Failed to load tickets"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.map { doc ->
                    val userName = doc.getString("userName")
                        ?: doc.getString("attendeeName")
                        ?: doc.getString("name")

                    TicketUi(
                        id = doc.id,
                        userName = userName,
                        checkedInAt = doc.getTimestamp("checkedInAt")
                    )
                }.orEmpty()

                _tickets.value = list
                updateCounts(list)
                _isLoading.value = false
            }
    }

    fun markTicketCheckedIn(ticketId: String) {
        val eventId = currentEventId ?: return
        _error.value = null

        db.collection("events")
            .document(eventId)
            .collection("tickets")
            .document(ticketId)
            .update(mapOf("checkedInAt" to FieldValue.serverTimestamp()))
            .addOnFailureListener { ex ->
                _error.value = ex.message ?: "Failed to check in ticket"
            }
    }

    private fun updateCounts(list: List<TicketUi>) {
        val issued = list.size
        val checkedIn = list.count { it.checkedInAt != null }
        _ticketCounts.value = TicketCounts(
            issued = issued,
            checkedIn = checkedIn,
            notArrived = issued - checkedIn
        )
    }

    override fun onCleared() {
        super.onCleared()
        ticketListener?.remove()
        ticketListener = null
    }
}
