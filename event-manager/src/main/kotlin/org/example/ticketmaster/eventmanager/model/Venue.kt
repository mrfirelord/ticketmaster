package org.example.ticketmaster.eventmanager.model

data class Venue(
    val venueId: String,
    val name: String,
    val location: Location,
    val seatingType: SeatingType,
    val capacity: Int,
    val seatMap: List<String>? = null  // <-- Simple list of seat IDs
)