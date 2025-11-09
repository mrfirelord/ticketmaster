package org.example.ticketmaster.searchservice.model

data class VenueInfo(
    val name: String,
    val location: LocationInfo,
    val distance: Double? = null  // Only if location filter used
)