package com.example.eventplanner.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val attendees: List<Attendee> = listOf() // List of attendees
)
