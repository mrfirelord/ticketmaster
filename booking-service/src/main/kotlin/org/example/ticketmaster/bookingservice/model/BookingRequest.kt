package org.example.ticketmaster.bookingservice.model

data class BookingRequest(
    val eventId: String,
    val seatId: String,
    val userId: String
)