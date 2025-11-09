package org.example.ticketmaster.searchservice.model

data class LocationInfo(
    val lat: Double,
    val lon: Double,
    val city: String,
    val state: String,
    val address: String
)