package org.example.ticketmaster.eventmanager

import org.example.ticketmaster.eventmanager.config.AppConfig
import org.example.ticketmaster.eventmanager.db.ElasticsearchSyncRepository
import org.example.ticketmaster.eventmanager.db.EventRepository
import org.example.ticketmaster.eventmanager.db.MongoConnection
import org.example.ticketmaster.eventmanager.elastic.ElasticsearchRepository
import org.example.ticketmaster.eventmanager.model.CreateEventRequest
import org.example.ticketmaster.eventmanager.model.Location
import org.example.ticketmaster.eventmanager.model.PriceRange
import org.example.ticketmaster.eventmanager.model.Venue
import org.example.ticketmaster.eventmanager.service.EventService

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = AppConfig.load()

        // Initialize
        val mongoClient = MongoConnection.createClient(config.mongodb.connectionString)
        val database = MongoConnection.getDatabase(mongoClient, config.mongodb.database)

        val eventRepository = EventRepository(database)
        val syncRepository = ElasticsearchSyncRepository(database)

        val elasticsearchRepository = ElasticsearchRepository(
            host = config.elasticsearch.host,
            port = config.elasticsearch.port,
            scheme = config.elasticsearch.scheme,
            username = config.elasticsearch.username,
            password = config.elasticsearch.password,
            disableSslVerification = config.elasticsearch.disableSslVerification
        )

        clearAll(eventRepository, elasticsearchRepository)

        val eventService = EventService(eventRepository, elasticsearchRepository, syncRepository)
        createSampleEvent(eventService)

        elasticsearchRepository.close()
    }

    private fun clearAll(eventRepository: EventRepository, elasticsearchRepository: ElasticsearchRepository) {
        eventRepository.clearAll()
        elasticsearchRepository.clearAll()
    }

    private fun createSampleEvent(eventService: EventService) {
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

        val response = eventService.createEvent(sampleEvent)
        println(response)
    }
}