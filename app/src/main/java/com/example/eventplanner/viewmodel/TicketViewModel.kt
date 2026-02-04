package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.model.Ticket
import com.example.eventplanner.data.repository.FirestoreTicketRepository
import com.example.eventplanner.data.repository.TicketRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TicketViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val repo: TicketRepository = FirestoreTicketRepository(db)

    private val _ticket = MutableStateFlow<Ticket?>(null)
    val ticket: StateFlow<Ticket?> = _ticket

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val tokenCache = mutableMapOf<String, String>()

    private var observeJob: Job? = null
    private var activeEventId: String? = null

    fun start(eventId: String) {
        activeEventId = eventId
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _error.value = "Not signed in"
            _ticket.value = null
            return
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repo.observeMyTicketForEvent(eventId, uid).collect { t ->
                _ticket.value = t
            }
        }
    }

    fun createOrGetTicket(eventId: String, paymentRequired: Boolean) {
        val uid = auth.currentUser?.uid ?: run {
            _error.value = "Not signed in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userName = fetchUserName(uid)
                val result = repo.createOrGetTicket(
                    eventId = eventId,
                    userId = uid,
                    userName = userName,
                    paymentRequired = paymentRequired
                )
                _ticket.value = result.ticket
                result.rawTokenForQr?.let { raw ->
                    tokenCache[result.ticket.id] = raw
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to create ticket: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        val eventId = activeEventId ?: return
        val uid = auth.currentUser?.uid ?: run {
            _error.value = "Not signed in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _ticket.value = repo.getMyTicketForEvent(eventId, uid)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh ticket: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rotateQrToken(ticketId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val raw = repo.rotateQrToken(ticketId)
                tokenCache[ticketId] = raw
                // ticket doc will also update via snapshot listener
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to rotate QR token: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getQrTokenFor(ticket: Ticket): String? {
        return tokenCache[ticket.id] ?: ticket.qrToken
    }

    private suspend fun fetchUserName(uid: String): String? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("name")
        } catch (_: Exception) {
            null
        }
    }
}
