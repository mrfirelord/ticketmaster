package org.example.ticketmaster.bookingservice.model

data class PurchaseRequest(
    val eventId: String,
    val seatId: String,
    val userId: String
)