package org.example.ticketmaster.searchservice.model

data class EventSearchResult(
    val eventId: String,
    val name: String,
    val artist: String,
    val description: String,
    val venue: VenueInfo,
    val dateTime: String,
    val category: String,
    val priceRange: PriceRangeInfo,
    val totalSeats: Int,
    val availableSeats: Int  // From Redis/MongoDB
)