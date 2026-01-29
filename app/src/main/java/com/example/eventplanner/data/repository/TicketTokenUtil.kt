package com.example.eventplanner.data.repository

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object TicketTokenUtil {
    private val rng = SecureRandom()

    fun generateTokenUrlSafe(bytes: Int = 24): String {
        val raw = ByteArray(bytes)
        rng.nextBytes(raw)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw)
    }

    fun sha256Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
