package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.model.Ticket
import com.example.eventplanner.data.repository.FirestoreTicketRepository
import com.example.eventplanner.data.repository.TicketRepository
import com.example.eventplanner.ui.screens.TicketCounts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class OrganizerViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val repository: TicketRepository = FirestoreTicketRepository(db)

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _ticketCounts = MutableStateFlow(TicketCounts(0, 0, 0))
    val ticketCounts: StateFlow<TicketCounts> = _ticketCounts

    private var currentEventId: String? = null

    fun loadTicketsForEvent(eventId: String) {
        currentEventId = eventId

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Observe tickets in real-time
                repository.observeTicketsForEvent(eventId)
                    .catch { exception ->
                        _error.value = "Failed to load tickets: ${exception.message}"
                        _isLoading.value = false
                    }
                    .collect { ticketList ->
                        _tickets.value = ticketList
                        updateTicketCounts(ticketList)
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun markTicketCheckedIn(ticketId: String) {
        val organizerUid = auth.currentUser?.uid ?: run {
            _error.value = "Not authenticated"
            return
        }

        viewModelScope.launch {
            try {
                repository.markCheckedIn(ticketId, organizerUid)
                // The flow will automatically update via observeTicketsForEvent
            } catch (e: Exception) {
                _error.value = "Failed to check in: ${e.message}"
            }
        }
    }

    fun searchTickets(query: String) {
        val eventId = currentEventId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.searchTicketsByAttendee(eventId, query)
                result.onSuccess { searchedTickets ->
                    _tickets.value = searchedTickets
                    updateTicketCounts(searchedTickets)
                }.onFailure { exception ->
                    _error.value = "Search failed: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Search error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateTicketCounts(tickets: List<Ticket>) {
        val issued = tickets.size
        val checkedIn = tickets.count { it.checkedInAt != null }
        val notArrived = tickets.count { it.checkedInAt == null }

        _ticketCounts.value = TicketCounts(issued, checkedIn, notArrived)
    }

    fun clearError() {
        _error.value = null
    }
}