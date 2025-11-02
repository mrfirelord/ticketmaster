package org.example.ticketmaster.eventmanager.model

data class CreateEventResponse(
    val eventId: String,
    val name: String,
    val artist: String,
    val description: String,
    val venue: Venue,
    val dateTime: String,  // ISO 8601 format
    val category: String,
    val priceRange: PriceRange,
    val totalSeats: Int,
    val createdAt: Long
) {
    companion object {
        fun fromEventDocument(document: EventDocument): CreateEventResponse {
            return CreateEventResponse(
                eventId = document._id!!,
                name = document.name,
                artist = document.artist,
                description = document.description,
                venue = document.venue,
                dateTime = document.dateTime,
                category = document.category,
                priceRange = document.priceRange,
                totalSeats = document.totalSeats,
                createdAt = document.createdAt.toEpochMilli()
            )
        }
    }
}