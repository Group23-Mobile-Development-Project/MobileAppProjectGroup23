package com.example.eventplanner.data.repository

import com.example.eventplanner.data.model.PaymentStatus
import com.example.eventplanner.data.model.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketRepository {

    suspend fun createOrGetTicket(
        eventId: String,
        userId: String,
        userName: String?,
        paymentRequired: Boolean
    ): CreateOrGetTicketResult

    suspend fun setPaymentPending(
        ticketId: String,
        paymentIntentId: String?
    )

    suspend fun markCheckedIn(
        ticketId: String,
        organizerUid: String
    )

    fun observeMyTicketForEvent(
        eventId: String,
        userId: String
    ): Flow<Ticket?>

    fun observeTicketsForEvent(
        eventId: String
    ): Flow<List<Ticket>>
}

data class CreateOrGetTicketResult(
    val ticket: Ticket,
    val rawTokenForQr: String?
)
