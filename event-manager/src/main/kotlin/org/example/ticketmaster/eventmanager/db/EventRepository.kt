package org.example.ticketmaster.eventmanager.db

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.example.ticketmaster.eventmanager.model.EventDocument
import org.litote.kmongo.*
import java.time.Instant

class EventRepository(database: MongoDatabase) {
    private val collection: MongoCollection<EventDocument> = database.getCollection<EventDocument>("events")

    fun save(event: EventDocument): EventDocument {
        collection.insertOne(event)
        return event
    }

    fun findById(id: String): EventDocument? = collection.findOne(EventDocument::_id eq id)
    
    // Update event
    fun update(id: String, event: EventDocument): EventDocument? {
        val updated = event.copy(_id = id, updatedAt = Instant.now())
        collection.replaceOne(EventDocument::_id eq id, updated)
        return findById(id)
    }
    
    // Delete event
    fun deleteById(id: String): Boolean {
        val result = collection.deleteOne(EventDocument::_id eq id)
        return result.deletedCount > 0
    }
    
    // Check if event exists
    fun existsById(id: String): Boolean {
        return collection.countDocuments(EventDocument::_id eq id) > 0
    }

    fun clearAll(): Long {
        val result = collection.deleteMany("{}")
        return result.deletedCount
    }
}