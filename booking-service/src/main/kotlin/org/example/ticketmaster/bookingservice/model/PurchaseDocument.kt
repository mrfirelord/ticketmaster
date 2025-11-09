package org.example.ticketmaster.bookingservice.model

import java.time.Instant

data class PurchaseDocument(
    val _id: String,              // "eventId:seatId"
    val eventId: String,
    val seatId: String,
    val userId: String,
    val purchasedAt: Instant = Instant.now()
) {
    companion object {
        fun create(eventId: String, seatId: String, userId: String): PurchaseDocument {
            return PurchaseDocument(
                _id = "$eventId:$seatId",
                eventId = eventId,
                seatId = seatId,
                userId = userId
            )
        }
    }
}