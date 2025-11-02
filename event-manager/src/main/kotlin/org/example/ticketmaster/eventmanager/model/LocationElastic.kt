package org.example.ticketmaster.eventmanager.model

data class LocationElastic(
    val geoPoint: GeoPoint,  // For Elasticsearch geo queries
    val city: String,
    val state: String,
    val address: String
)