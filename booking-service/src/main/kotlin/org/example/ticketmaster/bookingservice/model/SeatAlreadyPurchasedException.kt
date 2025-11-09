package org.example.ticketmaster.bookingservice.model

class SeatAlreadyPurchasedException(eventId: String, seatId: String) : 
    Exception("Seat $seatId for event $eventId is already purchased")