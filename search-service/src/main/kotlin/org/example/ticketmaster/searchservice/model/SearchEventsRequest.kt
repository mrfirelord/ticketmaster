package org.example.ticketmaster.searchservice.model

data class SearchEventsRequest(
    val query: String? = null,
    val location: LocationFilter? = null,
    val dateRange: DateRangeFilter? = null,
    val category: String? = null,
    val city: String? = null,
    val priceRange: PriceRangeFilter? = null,
    val page: Int = 1,
    val pageSize: Int = 20
)