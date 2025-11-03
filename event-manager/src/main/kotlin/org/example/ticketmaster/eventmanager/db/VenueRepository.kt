package org.example.ticketmaster.eventmanager.db

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.example.ticketmaster.eventmanager.model.VenueDocument
import org.litote.kmongo.*
import java.time.Instant

class VenueRepository(database: MongoDatabase) {

    private val collection: MongoCollection<VenueDocument> =
        database.getCollection<VenueDocument>("venues")

    fun save(venue: VenueDocument): VenueDocument {
        collection.insertOne(venue)
        return venue
    }

    fun findById(id: String): VenueDocument? {
        return collection.findOne(VenueDocument::_id eq id)
    }

    fun findAll(): List<VenueDocument> {
        return collection.find().toList()
    }

    fun update(id: String, venue: VenueDocument): VenueDocument? {
        val updated = venue.copy(_id = id, updatedAt = Instant.now())
        collection.replaceOne(VenueDocument::_id eq id, updated)
        return findById(id)
    }

    fun deleteById(id: String): Boolean {
        val result = collection.deleteOne(VenueDocument::_id eq id)
        return result.deletedCount > 0
    }

    fun clearAll(): Long {
        val result = collection.deleteMany("{}")
        return result.deletedCount
    }
}