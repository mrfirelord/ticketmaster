package org.example.ticketmaster.eventmanager.model

data class CreateEventRequest(
    val name: String,
    val artist: String,
    val description: String,
    val venue: Venue,
    val dateTime: String,  // ISO 8601 format
    val category: String,
    val priceRange: PriceRange,
    val totalSeats: Int
) {
    fun toDocument(): EventDocument {
        return EventDocument(
            name = this.name,
            artist = this.artist,
            description = this.description,
            venue = this.venue,
            dateTime = this.dateTime,
            category = this.category,
            priceRange = this.priceRange,
            totalSeats = this.totalSeats
        )
    }
}