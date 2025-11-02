package org.example.ticketmaster.eventmanager

import org.example.ticketmaster.eventmanager.db.EventRepository
import org.example.ticketmaster.eventmanager.db.MongoConnection
import org.example.ticketmaster.eventmanager.model.CreateEventRequest
import org.example.ticketmaster.eventmanager.model.Location
import org.example.ticketmaster.eventmanager.model.PriceRange
import org.example.ticketmaster.eventmanager.model.Venue

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        // Initialize
        val mongoClient = MongoConnection.createClient()
        val database = MongoConnection.getDatabase(mongoClient)
        val eventRepository = EventRepository(database)

        val sampleEvent = CreateEventRequest(
            name = "Taylor Swift - Eras Tour",
            artist = "Taylor Swift",
            description = "Experience the magic of Taylor Swift's record-breaking Eras Tour. A journey through her entire musical career with stunning visuals and performances.",
            venue = Venue(
                name = "Madison Square Garden",
                location = Location(
                    lat = 40.7505,
                    lon = -73.9934,
                    city = "New York",
                    state = "NY",
                    address = "4 Pennsylvania Plaza, New York, NY 10001"
                )
            ),
            dateTime = "2025-12-15T19:00:00Z",
            category = "Concert",
            priceRange = PriceRange(
                min = 89.00,
                max = 499.00,
                currency = "USD"
            ),
            totalSeats = 20000
        )

        val newEvent = sampleEvent.toDocument()
//        val savedEvent = eventRepository.save(newEvent)

        val event = eventRepository.findById("69079e832f75336d6b6fdbaf")
        println(event)
    }
}