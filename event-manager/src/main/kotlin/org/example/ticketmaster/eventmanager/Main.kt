package org.example.ticketmaster.eventmanager

import org.example.ticketmaster.eventmanager.config.AppConfig
import org.example.ticketmaster.eventmanager.db.ElasticsearchSyncRepository
import org.example.ticketmaster.eventmanager.db.EventRepository
import org.example.ticketmaster.eventmanager.db.MongoConnection
import org.example.ticketmaster.eventmanager.db.VenueRepository
import org.example.ticketmaster.eventmanager.elastic.ElasticsearchRepository
import org.example.ticketmaster.eventmanager.model.*
import org.example.ticketmaster.eventmanager.service.EventService
import org.slf4j.LoggerFactory

object Main {
    private val logger = LoggerFactory.getLogger(EventService::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val config = AppConfig.load()

        // Initialize
        val mongoClient = MongoConnection.createClient(config.mongodb.connectionString)
        val database = MongoConnection.getDatabase(mongoClient, config.mongodb.database)

        val eventRepository = EventRepository(database)
        val syncRepository = ElasticsearchSyncRepository(database)
        val venueRepository = VenueRepository(database)

        val elasticsearchRepository = ElasticsearchRepository(
            host = config.elasticsearch.host,
            port = config.elasticsearch.port,
            scheme = config.elasticsearch.scheme,
            username = config.elasticsearch.username,
            password = config.elasticsearch.password,
            disableSslVerification = config.elasticsearch.disableSslVerification
        )

        eventRepository.clearAll()
        elasticsearchRepository.clearAll()
        venueRepository.clearAll()
        syncRepository.clearAll()

        // Create a sample venue first
        val venueDocument = createSampleVenue(venueRepository)

        val eventService = EventService(eventRepository, elasticsearchRepository, syncRepository, venueRepository)
        createSampleEvent(eventService, venueDocument)

        elasticsearchRepository.close()
        mongoClient.close()
    }

    fun createSampleVenue(venueRepository: VenueRepository): VenueDocument {
        val venue = VenueDocument(
            name = "Madison Square Garden",
            location = Location(
                lat = 40.7505,
                lon = -73.9934,
                city = "New York",
                state = "NY",
                address = "4 Pennsylvania Plaza, New York, NY 10001"
            ),
            seatingType = SeatingType.GENERAL_ADMISSION,
            capacity = 20000
        )

        val saved: VenueDocument = venueRepository.save(venue)
        println("Venue created: ${saved._id} - ${saved.name}")
        return saved
    }

    private fun createSampleEvent(eventService: EventService, venue: VenueDocument) {
        val sampleEvent = CreateEventRequest(
            name = "Taylor Swift - Eras Tour",
            artist = "Taylor Swift",
            description = "Experience the magic of Taylor Swift's record-breaking Eras Tour. A journey through her entire musical career with stunning visuals and performances.",
            venueId = venue._id!!,
            dateTime = "2025-12-15T19:00:00Z",
            category = "Concert",
            priceRange = PriceRange(
                min = 89.00,
                max = 499.00,
                currency = "USD"
            ),
            totalSeats = venue.capacity
        )

        val response = eventService.createEvent(sampleEvent)
        logger.info(response.toString())
    }
}