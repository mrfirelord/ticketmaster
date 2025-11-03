package org.example.ticketmaster.eventmanager.model

data class CreateEventRequest(
    val name: String,
    val artist: String,
    val description: String,
    val venueId: String,
    val dateTime: String,  // ISO 8601 format
    val category: String,
    val priceRange: PriceRange,
    val totalSeats: Int
)