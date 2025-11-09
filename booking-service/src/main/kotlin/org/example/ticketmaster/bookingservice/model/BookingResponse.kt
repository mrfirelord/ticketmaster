package org.example.ticketmaster.bookingservice.model

data class BookingResponse(
    val eventId: String,
    val seatId: String,
    val userId: String,
    val bookedAt: String,
    val expiresAt: String
) {
    companion object {
        fun from(booking: Booking): BookingResponse {
            return BookingResponse(
                eventId = booking.eventId,
                seatId = booking.seatId,
                userId = booking.userId,
                bookedAt = booking.bookedAt.toString(),
                expiresAt = booking.expiresAt.toString()
            )
        }
    }
}