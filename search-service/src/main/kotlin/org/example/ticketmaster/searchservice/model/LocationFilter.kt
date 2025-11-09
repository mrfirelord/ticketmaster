package org.example.ticketmaster.searchservice.model

data class LocationFilter(
    val lat: Double,
    val lon: Double,
    val radius: Double,
    val unit: String = "mi"  // "mi" or "km"
)