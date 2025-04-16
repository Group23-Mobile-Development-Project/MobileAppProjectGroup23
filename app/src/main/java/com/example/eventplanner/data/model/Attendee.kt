package com.example.eventplanner.data.model

data class Attendee(
    val userId: String = "",
    val userName: String = "", // Add userName if needed
    val status: String = "not attending" // Default status
)
