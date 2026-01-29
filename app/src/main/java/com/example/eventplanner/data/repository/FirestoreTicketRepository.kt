package com.example.eventplanner.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.example.eventplanner.data.model.PaymentStatus
import com.example.eventplanner.data.model.Ticket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTicketRepository(
    private val db: FirebaseFirestore
) : TicketRepository {

    private val ticketsCol = db.collection("tickets")

    override suspend fun createOrGetTicket(
        eventId: String,
        userId: String,
        userName: String?,
        paymentRequired: Boolean
    ): CreateOrGetTicketResult {
        // One ticket per user per event
        val existing = ticketsCol
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .await()

        if (!existing.isEmpty) {
            val doc = existing.documents.first()
            val t = doc.toObject<Ticket>()?.copy(id = doc.id)
            requireNotNull(t) { "Failed to parse existing ticket" }
            return CreateOrGetTicketResult(ticket = t, rawTokenForQr = null)
        }

        val rawToken = TicketTokenUtil.generateTokenUrlSafe()
        val tokenHash = TicketTokenUtil.sha256Hex(rawToken)
        val now = Timestamp.now()

        val docRef = ticketsCol.document()

        val initialStatus = if (paymentRequired) {
            PaymentStatus.PENDING.value
        } else {
            // free ticket can be confirmed immediately
            PaymentStatus.CONFIRMED.value
        }

        val ticket = Ticket(
            id = docRef.id,
            eventId = eventId,
            userId = userId,
            userName = userName,
            issuedAt = now,
            paymentRequired = paymentRequired,
            paymentStatus = initialStatus,
            paymentIntentId = null,
            qrTokenHash = tokenHash,
            checkedInAt = null,
            checkedInBy = null,
            updatedAt = now
        )

        docRef.set(ticket).await()
        return CreateOrGetTicketResult(ticket = ticket, rawTokenForQr = rawToken)
    }

    override suspend fun setPaymentPending(ticketId: String, paymentIntentId: String?) {
        val updates = hashMapOf<String, Any>(
            "paymentStatus" to PaymentStatus.PENDING.value,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (paymentIntentId != null) updates["paymentIntentId"] = paymentIntentId

        ticketsCol.document(ticketId).update(updates).await()
    }

    override suspend fun markCheckedIn(ticketId: String, organizerUid: String) {
        ticketsCol.document(ticketId).update(
            mapOf(
                "checkedInAt" to FieldValue.serverTimestamp(),
                "checkedInBy" to organizerUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    override fun observeMyTicketForEvent(eventId: String, userId: String): Flow<Ticket?> = callbackFlow {
        val query = ticketsCol
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("userId", userId)
            .limit(1)

        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snap == null || snap.isEmpty) {
                trySend(null)
                return@addSnapshotListener
            }
            val doc = snap.documents.first()
            val t = doc.toObject<Ticket>()?.copy(id = doc.id)
            trySend(t)
        }

        awaitClose { reg.remove() }
    }

    override fun observeTicketsForEvent(eventId: String): Flow<List<Ticket>> = callbackFlow {
        val query = ticketsCol
            .whereEqualTo("eventId", eventId)
            .orderBy("issuedAt", Query.Direction.DESCENDING)

        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject<Ticket>()?.copy(id = d.id)
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { reg.remove() }
    }
}
