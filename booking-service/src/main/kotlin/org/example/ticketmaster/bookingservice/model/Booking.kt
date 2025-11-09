package org.example.ticketmaster.bookingservice.model

import java.time.Instant

data class Booking(
    val eventId: String,
    val seatId: String,
    val userId: String,
    val bookedAt: Instant,
    val expiresAt: Instant
)