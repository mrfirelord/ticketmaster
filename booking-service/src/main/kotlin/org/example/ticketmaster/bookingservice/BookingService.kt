package org.example.ticketmaster.bookingservice

import org.example.ticketmaster.bookingservice.model.BookingRequest
import org.example.ticketmaster.bookingservice.model.BookingResponse
import org.example.ticketmaster.bookingservice.model.PurchaseResponse
import org.example.ticketmaster.bookingservice.model.SeatAlreadyPurchasedException
import org.slf4j.LoggerFactory

class BookingService(
    private val bookingRepository: BookingRepository,
    private val purchaseRepository: PurchaseRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun bookSeat(request: BookingRequest): BookingResponse {
        // Check if already purchased
        if (purchaseRepository.isPurchased(request.eventId, request.seatId)) {
            throw SeatAlreadyPurchasedException(request.eventId, request.seatId)
        }

        val booking = bookingRepository.bookSeat(
            request.eventId,
            request.seatId,
            request.userId
        )

        logger.info("Seat ${request.eventId}:${request.seatId} was booked")
        return BookingResponse.from(booking)
    }

    fun getBooking(eventId: String, seatId: String): BookingResponse? {
        val booking = bookingRepository.getBooking(eventId, seatId)
        return booking?.let { BookingResponse.from(it) }
    }

    fun getBookedSeats(eventId: String): Set<String> {
        return bookingRepository.getBookedSeats(eventId)
    }

    fun releaseBooking(eventId: String, seatId: String) {
        logger.info("Releasing booking: $eventId:$seatId")
        bookingRepository.releaseBooking(eventId, seatId)
    }

    fun isBooked(eventId: String, seatId: String): Boolean {
        return bookingRepository.isBooked(eventId, seatId)
    }

    fun purchaseSeat(eventId: String, seatId: String, userId: String): PurchaseResponse {
        logger.info("Purchase request: $eventId:$seatId for user $userId")

        val booking = bookingRepository.getBooking(eventId, seatId)
            ?: throw BookingNotFoundException(eventId, seatId)

        if (booking.userId != userId)
            throw UnauthorizedPurchaseException(eventId, seatId, userId)

        if (purchaseRepository.isPurchased(eventId, seatId))
            throw SeatAlreadyPurchasedException(eventId, seatId)

        val purchase = purchaseRepository.purchase(eventId, seatId, userId)

        bookingRepository.releaseBooking(eventId, seatId)

        logger.info("Purchase completed: $eventId:$seatId")

        return PurchaseResponse.from(purchase)
    }

    fun getPurchasedSeats(eventId: String): List<String> = purchaseRepository.findPurchasedSeatsByEvent(eventId)

    fun getUserPurchases(userId: String): List<PurchaseResponse> =
        purchaseRepository.findPurchasesByUser(userId).map { PurchaseResponse.from(it) }
}

// New exceptions
class BookingNotFoundException(eventId: String, seatId: String) :
    Exception("Booking not found: $eventId:$seatId")

class UnauthorizedPurchaseException(eventId: String, seatId: String, userId: String) :
    Exception("User $userId is not authorized to purchase $eventId:$seatId")