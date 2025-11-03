package org.example.ticketmaster.eventmanager.db

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.example.ticketmaster.eventmanager.model.ElasticsearchSyncDocument
import org.example.ticketmaster.eventmanager.model.SyncStatus
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.lt
import org.litote.kmongo.lte
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import java.time.Instant

class ElasticsearchSyncRepository(database: MongoDatabase) {
    private val collection: MongoCollection<ElasticsearchSyncDocument> =
        database.getCollection<ElasticsearchSyncDocument>("elasticsearch_sync")

    // Create sync record
    fun create(eventId: String): ElasticsearchSyncDocument {
        val syncDoc = ElasticsearchSyncDocument(
            eventId = eventId,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            readyToRetryAfter = Instant.now(),
            attemptCount = 0,
            lastDelay = 0
        )
        collection.insertOne(syncDoc)
        return syncDoc
    }

    // Update status to SEARCHABLE
    fun markAsSearchable(eventId: String): Boolean {
        val result = collection.updateOne(
            ElasticsearchSyncDocument::eventId eq eventId,
            set(ElasticsearchSyncDocument::status setTo SyncStatus.SEARCHABLE)
        )
        return result.modifiedCount > 0
    }

    // Update after failed retry attempt (for Phase 2)
    fun updateAfterFailedAttempt(
        eventId: String,
        attemptCount: Int,
        lastDelay: Long,
        readyToRetry: Instant
    ): Boolean {
        val result = collection.updateOne(
            ElasticsearchSyncDocument::eventId eq eventId,
            combine(
                set(ElasticsearchSyncDocument::attemptCount setTo attemptCount),
                set(ElasticsearchSyncDocument::lastDelay setTo lastDelay),
                set(ElasticsearchSyncDocument::readyToRetryAfter setTo readyToRetry),
                set(ElasticsearchSyncDocument::lastAttemptAt setTo Instant.now())
            )
        )
        return result.modifiedCount > 0
    }

    // Mark as FAILED after max retries (for Phase 2)
    fun markAsFailed(eventId: String): Boolean {
        val result = collection.updateOne(
            ElasticsearchSyncDocument::eventId eq eventId,
            set(ElasticsearchSyncDocument::status setTo SyncStatus.FAILED)
        )
        return result.modifiedCount > 0
    }

    fun findByEventId(eventId: String): ElasticsearchSyncDocument? =
        collection.findOne(ElasticsearchSyncDocument::eventId eq eventId)

    // Find pending records ready for retry (for Phase 2)
    fun findPendingReadyForRetry(maxAttempts: Int): List<ElasticsearchSyncDocument> =
        collection.find(
            and(
                ElasticsearchSyncDocument::status eq SyncStatus.PENDING,
                ElasticsearchSyncDocument::readyToRetryAfter lte Instant.now(),
                ElasticsearchSyncDocument::attemptCount lt maxAttempts
            )
        ).toList()


    // Clear all (for testing)
    fun clearAll(): Long {
        val result = collection.deleteMany("{}")
        return result.deletedCount
    }
}