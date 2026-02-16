package com.example.eventplanner.data.repository

import java.security.MessageDigest
import java.util.UUID

object TicketTokenUtil {

    fun generateTokenUrlSafe(): String {
        // URL safe enough for payload use
        return UUID.randomUUID().toString().replace("-", "")
    }

    fun sha256Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
