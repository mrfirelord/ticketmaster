package org.example.ticketmaster.bookingservice

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.example.ticketmaster.bookingservice.model.PurchaseDocument
import org.litote.kmongo.*
import org.slf4j.LoggerFactory

class PurchaseRepository(database: MongoDatabase) {
    private val logger = LoggerFactory.getLogger(PurchaseRepository::class.java)

    private val collection: MongoCollection<PurchaseDocument> =
        database.getCollection<PurchaseDocument>("purchases")

    fun purchase(eventId: String, seatId: String, userId: String): PurchaseDocument {
        val purchase = PurchaseDocument.create(eventId, seatId, userId)
        collection.insertOne(purchase)
        logger.info("Seat purchased: eventId=$eventId, seatId=$seatId, userId=$userId")
        return purchase
    }

    fun isPurchased(eventId: String, seatId: String): Boolean {
        val id = "$eventId:$seatId"
        return collection.countDocuments(PurchaseDocument::_id eq id) > 0
    }

    fun findPurchasedSeatsByEvent(eventId: String): List<String> {
        return collection.find(PurchaseDocument::eventId eq eventId)
            .toList()
            .map { it.seatId }
    }

    fun findPurchasesByUser(userId: String): List<PurchaseDocument> {
        return collection.find(PurchaseDocument::userId eq userId).toList()
    }

    fun clearAll(): Long {
        val result = collection.deleteMany("{}")
        return result.deletedCount
    }
}