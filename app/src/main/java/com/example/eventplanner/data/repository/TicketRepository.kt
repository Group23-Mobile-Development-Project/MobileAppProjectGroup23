package com.example.eventplanner.data.repository

import com.example.eventplanner.data.model.Ticket
import kotlinx.coroutines.flow.Flow

data class CreateOrGetTicketResult(
    val ticket: Ticket,
    val rawTokenForQr: String?
)

interface TicketRepository {
    fun observeMyTicketForEvent(eventId: String, userId: String): Flow<Ticket?>

    suspend fun getMyTicketForEvent(eventId: String, userId: String): Ticket?

    suspend fun createOrGetTicket(
        eventId: String,
        userId: String,
        userName: String?,
        paymentRequired: Boolean
    ): CreateOrGetTicketResult

    suspend fun rotateQrToken(ticketId: String): String
}
