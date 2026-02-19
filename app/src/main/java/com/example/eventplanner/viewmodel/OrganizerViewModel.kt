package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import com.example.eventplanner.ui.screens.TicketCounts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

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

    // UPDATED: matches QrCodeUtil.buildPayload()
    // Payload format: v1|ticketId=...|eventId=...|token=...
    suspend fun checkInWithQr(eventId: String, raw: String) {
        _error.value = null

        val parsed = parseQrPayload(raw)

        // must match the event we are scanning for
        if (parsed.eventId != eventId) throw IllegalArgumentException("QR is for another event")

        val organizerId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Not signed in")

        val ticketRef = db.collection("events")
            .document(eventId)
            .collection("tickets")
            .document(parsed.ticketId)

        val snap = ticketRef.get().await()
        if (!snap.exists()) throw IllegalArgumentException("Ticket not found")

        // token validation: compare with a stored field if you have it
        // try common field names; adjust if your firestore uses a different one.
        val storedToken = snap.getString("token")
            ?: snap.getString("qrToken")
            ?: snap.getString("qr_token")

        if (storedToken.isNullOrBlank() || storedToken != parsed.token) {
            throw IllegalArgumentException("Invalid ticket token")
        }

        val checkedInAt = snap.getTimestamp("checkedInAt")
        if (checkedInAt != null) throw IllegalArgumentException("Already checked in")

        ticketRef.update(
            mapOf(
                "checkedInAt" to FieldValue.serverTimestamp(),
                "checkedInBy" to organizerId
            )
        ).await()

        currentEventId = eventId
    }

    private data class ParsedQr(
        val ticketId: String,
        val eventId: String,
        val token: String
    )

    private fun parseQrPayload(raw: String): ParsedQr {
        // expected: v1|ticketId=...|eventId=...|token=...
        val parts = raw.split("|")
        if (parts.isEmpty() || parts[0] != "v1") throw IllegalArgumentException("Invalid QR payload")

        fun getValue(key: String): String {
            val entry = parts.firstOrNull { it.startsWith("$key=") }
                ?: throw IllegalArgumentException("Missing $key in QR")
            return entry.substringAfter("=", "").trim().also {
                if (it.isBlank()) throw IllegalArgumentException("Empty $key in QR")
            }
        }

        return ParsedQr(
            ticketId = getValue("ticketId"),
            eventId = getValue("eventId"),
            token = getValue("token")
        )
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
