package com.example.eventplanner.data.model

import com.google.firebase.Timestamp

data class Ticket(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val userName: String? = null,
    val issuedAt: Timestamp? = null,
    val paymentRequired: Boolean = false,
    val paymentStatus: String = PaymentStatus.NONE.value,
    val paymentIntentId: String? = null,
    val qrTokenHash: String = "",
    // raw token used only for generating QR payload; it is not personal data
    val qrToken: String? = null,
    val checkedInAt: Timestamp? = null,
    val checkedInBy: String? = null,
    val updatedAt: Timestamp? = null
)

enum class TicketType(val value: String) { FREE("FREE"), PAID("PAID") }

enum class PaymentStatus(val value: String) {
    NONE("none"),
    PENDING("pending"),
    CONFIRMED("confirmed"),
    FAILED("failed"),
    CANCELLED("cancelled")
}
