package org.example.ticketmaster.eventmanager.model

import java.time.Instant

data class ElasticsearchSyncDocument(
    val _id: String? = null,
    val eventId: String,
    val status: SyncStatus,
    val createdAt: Instant = Instant.now(),
    val lastAttemptAt: Instant? = null,
    val attemptCount: Int = 0,
    val readyToRetryAfter: Instant = Instant.now(),
    val lastDelay: Long = 0
)

enum class SyncStatus {
    PENDING,
    SEARCHABLE,
    FAILED
}