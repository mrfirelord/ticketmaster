package org.example.ticketmaster.eventmanager.model

import java.time.Instant

data class EventDocument(
    val _id: String? = null,  // MongoDB's _id field, null before insert
    val name: String,
    val artist: String,
    val description: String,
    val venue: Venue,
    val dateTime: String,  // ISO 8601 format
    val category: String,
    val priceRange: PriceRange,
    val totalSeats: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)