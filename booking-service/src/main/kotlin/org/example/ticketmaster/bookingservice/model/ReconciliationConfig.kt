package org.example.ticketmaster.bookingservice.model

import com.typesafe.config.Config

data class ReconciliationConfig(
    val intervalMs: Long
) {
    companion object {
        fun from(config: Config) = ReconciliationConfig(
            intervalMs = config.getLong("intervalMs")
        )
    }
}