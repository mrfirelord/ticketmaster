package org.example.ticketmaster.eventmanager.model

data class Location(
    val lat: Double,
    val lon: Double,
    val city: String,
    val state: String,
    val address: String
)