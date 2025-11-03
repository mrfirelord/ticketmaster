package org.example.ticketmaster.eventmanager.service

import org.example.ticketmaster.eventmanager.db.EventRepository
import org.example.ticketmaster.eventmanager.elastic.ElasticsearchRepository
import org.example.ticketmaster.eventmanager.db.ElasticsearchSyncRepository
import org.example.ticketmaster.eventmanager.db.VenueRepository
import org.example.ticketmaster.eventmanager.model.CreateEventRequest
import org.example.ticketmaster.eventmanager.model.CreateEventResponse
import org.example.ticketmaster.eventmanager.model.EventDocument
import org.example.ticketmaster.eventmanager.model.EventElasticDocument
import org.example.ticketmaster.eventmanager.model.VenueDocument
import org.slf4j.LoggerFactory

class EventService(
    private val eventRepository: EventRepository,
    private val elasticsearchRepository: ElasticsearchRepository,
    private val syncRepository: ElasticsearchSyncRepository,
    private val venueRepository: VenueRepository
) {
    private val logger = LoggerFactory.getLogger(EventService::class.java)

    fun createEvent(request: CreateEventRequest): CreateEventResponse {
        logger.info("Creating event: ${request.name}")

        // 1. Fetch venue from venues collection
        val venue: VenueDocument = venueRepository.findById(request.venueId)
            ?: throw IllegalArgumentException("Venue not found: ${request.venueId}")

        logger.debug("Venue fetched: ${venue.name}")

        val eventDocument = EventDocument(
            name = request.name,
            artist = request.artist,
            description = request.description,
            venue = venue.toVenue(),
            dateTime = request.dateTime,
            category = request.category,
            priceRange = request.priceRange,
            totalSeats = request.totalSeats
        )

        val savedDocument = eventRepository.save(eventDocument)

        syncRepository.create(savedDocument._id!!)

        try {
            val elasticDocument = EventElasticDocument.from(savedDocument)
            elasticsearchRepository.indexEvent(elasticDocument)
            syncRepository.markAsSearchable(savedDocument._id)
            logger.info("Event ${savedDocument._id} indexed in Elasticsearch successfully")
        } catch (e: Exception) {
            // Failure - leave as PENDING for external process
            logger.error("Warning: Failed to index in Elasticsearch", e)
        }

        return CreateEventResponse.fromEventDocument(savedDocument)
    }
}