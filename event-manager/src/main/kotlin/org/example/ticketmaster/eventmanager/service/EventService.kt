package org.example.ticketmaster.eventmanager.service

import org.example.ticketmaster.eventmanager.db.EventRepository
import org.example.ticketmaster.eventmanager.elastic.ElasticsearchRepository
import org.example.ticketmaster.eventmanager.db.ElasticsearchSyncRepository
import org.example.ticketmaster.eventmanager.model.CreateEventRequest
import org.example.ticketmaster.eventmanager.model.CreateEventResponse
import org.example.ticketmaster.eventmanager.model.EventElasticDocument

class EventService(
    private val eventRepository: EventRepository,
    private val elasticsearchRepository: ElasticsearchRepository,
    private val syncRepository: ElasticsearchSyncRepository
) {
    fun createEvent(request: CreateEventRequest): CreateEventResponse {
        val eventDocument = request.toDocument()
        val savedDocument = eventRepository.save(eventDocument)

        syncRepository.create(savedDocument._id!!)

        try {
            val elasticDocument = EventElasticDocument.from(savedDocument)
            elasticsearchRepository.indexEvent(elasticDocument)

            syncRepository.markAsSearchable(savedDocument._id)
        } catch (e: Exception) {
            // Failure - leave as PENDING for external process
            println("Warning: Failed to index in Elasticsearch: ${e.message}")
        }

        return CreateEventResponse.fromEventDocument(savedDocument)
    }
}