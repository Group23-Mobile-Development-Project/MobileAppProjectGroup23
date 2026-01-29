package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.data.model.Ticket
import com.example.eventplanner.data.repository.CreateOrGetTicketResult
import com.example.eventplanner.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TicketViewModel(
    private val repo: TicketRepository
) : ViewModel() {

    private val _createResult = MutableStateFlow<CreateOrGetTicketResult?>(null)
    val createResult: StateFlow<CreateOrGetTicketResult?> = _createResult

    fun createOrGetTicket(eventId: String, userId: String, userName: String?, paymentRequired: Boolean) {
        viewModelScope.launch {
            _createResult.value = repo.createOrGetTicket(eventId, userId, userName, paymentRequired)
        }
    }

    fun observeMyTicket(eventId: String, userId: String): StateFlow<Ticket?> {
        return repo.observeMyTicketForEvent(eventId, userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
}
