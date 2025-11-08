package org.example.ticketmaster.eventmanager.db

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.example.ticketmaster.eventmanager.model.PurchaseDocument
import org.litote.kmongo.*
import org.slf4j.LoggerFactory

class PurchaseRepository(database: MongoDatabase) {
    
    private val logger = LoggerFactory.getLogger(PurchaseRepository::class.java)
    
    private val collection: MongoCollection<PurchaseDocument> = 
        database.getCollection<PurchaseDocument>("purchases")
    
    // Create purchase (buy a seat)
    fun purchase(eventId: String, seatId: String, userId: String): PurchaseDocument {
        val purchase = PurchaseDocument.create(eventId, seatId, userId)
        collection.insertOne(purchase)
        logger.info("Seat purchased: eventId=$eventId, seatId=$seatId, userId=$userId")
        return purchase
    }
    
    // Check if specific seat is purchased
    fun isPurchased(eventId: String, seatId: String): Boolean {
        val id = PurchaseDocument.generateId(eventId, seatId)
        return collection.countDocuments(PurchaseDocument::_id eq id) > 0
    }
    
    // Get all purchased seats for an event
    fun findPurchasedSeatsByEvent(eventId: String): List<String> {
        return collection.find(PurchaseDocument::eventId eq eventId)
            .toList()
            .map { it.seatId }
    }
    
    // Get all purchases for an event (full details)
    fun findPurchasesByEvent(eventId: String): List<PurchaseDocument> {
        return collection.find(PurchaseDocument::eventId eq eventId).toList()
    }
    
    // Get all purchases by a user
    fun findPurchasesByUser(userId: String): List<PurchaseDocument> {
        return collection.find(PurchaseDocument::userId eq userId).toList()
    }
    
    // Get specific purchase
    fun findById(eventId: String, seatId: String): PurchaseDocument? {
        val id = PurchaseDocument.generateId(eventId, seatId)
        return collection.findOne(PurchaseDocument::_id eq id)
    }
    
    // Delete purchase (for testing/refunds)
    fun delete(eventId: String, seatId: String): Boolean {
        val id = PurchaseDocument.generateId(eventId, seatId)
        val result = collection.deleteOne(PurchaseDocument::_id eq id)
        return result.deletedCount > 0
    }
    
    // Count purchased seats for event
    fun countPurchasedSeats(eventId: String): Long {
        return collection.countDocuments(PurchaseDocument::eventId eq eventId)
    }
    
    // Clear all purchases (for testing)
    fun clearAll(): Long {
        val result = collection.deleteMany("{}")
        return result.deletedCount
    }
    
    // Clear purchases for specific event (for testing)
    fun clearByEvent(eventId: String): Long {
        val result = collection.deleteMany(PurchaseDocument::eventId eq eventId)
        return result.deletedCount
    }
}