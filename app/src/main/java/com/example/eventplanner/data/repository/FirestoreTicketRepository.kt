package com.example.eventplanner.data.repository

import com.example.eventplanner.data.model.PaymentStatus
import com.example.eventplanner.data.model.Ticket
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTicketRepository(
    private val db: FirebaseFirestore
) : TicketRepository {

    private val ticketsCol = db.collection("tickets")

    private fun ticketId(eventId: String, userId: String): String = "${eventId}_${userId}"

    override fun observeMyTicketForEvent(eventId: String, userId: String): Flow<Ticket?> = callbackFlow {
        val docRef = ticketsCol.document(ticketId(eventId, userId))

        val reg: ListenerRegistration = docRef.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snap.toObject(Ticket::class.java))
        }

        awaitClose { reg.remove() }
    }

    override suspend fun getMyTicketForEvent(eventId: String, userId: String): Ticket? {
        val snap = ticketsCol.document(ticketId(eventId, userId)).get().await()
        if (!snap.exists()) return null
        return snap.toObject(Ticket::class.java)
    }

    override suspend fun createOrGetTicket(
        eventId: String,
        userId: String,
        userName: String?,
        paymentRequired: Boolean
    ): CreateOrGetTicketResult {
        val id = ticketId(eventId, userId)
        val docRef = ticketsCol.document(id)

        val existingSnap = docRef.get().await()
        if (existingSnap.exists()) {
            val existing = existingSnap.toObject(Ticket::class.java)
                ?: throw IllegalStateException("Ticket exists but could not be parsed")
            return CreateOrGetTicketResult(existing, null)
        }

        val rawToken = TicketTokenUtil.generateTokenUrlSafe()
        val tokenHash = TicketTokenUtil.sha256Hex(rawToken)
        val now = Timestamp.now()

        val data: Map<String, Any?> = mapOf(
            "id" to id,
            "eventId" to eventId,
            "userId" to userId,
            "userName" to userName,
            "issuedAt" to now,
            "paymentRequired" to paymentRequired,
            "paymentStatus" to if (paymentRequired) PaymentStatus.PENDING.value else PaymentStatus.CONFIRMED.value,
            "paymentIntentId" to null,
            "qrTokenHash" to tokenHash,
            "qrToken" to rawToken,
            "checkedInAt" to null,
            "checkedInBy" to null,
            "updatedAt" to now
        )

        docRef.set(data, SetOptions.merge()).await()

        val createdSnap = docRef.get().await()
        val created = createdSnap.toObject(Ticket::class.java)
            ?: throw IllegalStateException("Ticket was created but could not be parsed")

        return CreateOrGetTicketResult(created, rawToken)
    }

    override suspend fun rotateQrToken(ticketId: String): String {
        val docRef = ticketsCol.document(ticketId)
        val snap = docRef.get().await()
        if (!snap.exists()) throw IllegalStateException("Ticket not found")

        val rawToken = TicketTokenUtil.generateTokenUrlSafe()
        val tokenHash = TicketTokenUtil.sha256Hex(rawToken)
        val now = Timestamp.now()

        docRef.set(
            mapOf(
                "qrTokenHash" to tokenHash,
                "qrToken" to rawToken,
                "updatedAt" to now
            ),
            SetOptions.merge()
        ).await()

        return rawToken
    }
}
