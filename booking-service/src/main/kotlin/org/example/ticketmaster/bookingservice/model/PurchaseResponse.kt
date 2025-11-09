package org.example.ticketmaster.bookingservice.model

data class PurchaseResponse(
    val eventId: String,
    val seatId: String,
    val userId: String,
    val purchasedAt: String
) {
    companion object {
        fun from(purchase: PurchaseDocument): PurchaseResponse {
            return PurchaseResponse(
                eventId = purchase.eventId,
                seatId = purchase.seatId,
                userId = purchase.userId,
                purchasedAt = purchase.purchasedAt.toString()
            )
        }
    }
}