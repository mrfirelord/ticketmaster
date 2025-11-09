package org.example.ticketmaster.searchservice.model

data class SearchEventsResponse(
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val results: List<EventSearchResult>
)