package org.example.ticketmaster.bookingservice.model

class SeatAlreadyBookedException(eventId: String, seatId: String) :
    Exception("Seat $seatId for event $eventId is already booked")