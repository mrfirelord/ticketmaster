package org.example.ticketmaster.searchservice.model

data class DateRangeFilter(
    val from: String?,  // ISO 8601 format
    val to: String?
)