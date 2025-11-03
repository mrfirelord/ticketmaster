package org.example.ticketmaster.eventmanager.model

import java.time.Instant

data class VenueDocument(
    val _id: String? = null,
    val name: String,
    val location: Location,
    val seatingType: SeatingType,
    val capacity: Int,
    val seatMap: List<String>? = null,  // <-- Just seat IDs like ["A-1-1", "A-1-2", ...]
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun toVenue(): Venue = Venue(
        venueId = this._id ?: throw IllegalArgumentException("No id"),
        name = this.name,
        location = this.location,
        seatingType = this.seatingType,
        capacity = this.capacity,
        seatMap = this.seatMap
    )
}