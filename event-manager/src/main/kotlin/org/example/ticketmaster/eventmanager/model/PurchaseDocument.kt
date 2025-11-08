package org.example.ticketmaster.eventmanager.model

import java.time.Instant

data class PurchaseDocument(
    val _id: String,              // Composite: "eventId:seatId"
    val eventId: String,
    val seatId: String,
    val userId: String,
    val purchasedAt: Instant = Instant.now()
) {
    companion object {
        fun create(eventId: String, seatId: String, userId: String): PurchaseDocument {
            return PurchaseDocument(
                _id = generateId(eventId = eventId, seatId = seatId),
                eventId = eventId,
                seatId = seatId,
                userId = userId
            )
        }

        // Helper to generate ID
        fun generateId(eventId: String, seatId: String): String = "$eventId:$seatId"
    }
}