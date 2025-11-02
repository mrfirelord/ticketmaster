package org.example.ticketmaster.eventmanager.model

data class PriceRange(
    val min: Double,
    val max: Double,
    val currency: String = "USD"
)