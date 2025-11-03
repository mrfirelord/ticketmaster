package org.example.ticketmaster.eventmanager.config

import com.typesafe.config.Config

data class SyncConfig(val maxRetryAttempts: Int, val initialDelayMs: Long, val maxDelayMs: Long) {
    companion object {
        fun from(config: Config) = SyncConfig(
            maxRetryAttempts = config.getInt("maxRetryAttempts"),
            initialDelayMs = config.getLong("initialDelayMs"),
            maxDelayMs = config.getLong("maxDelayMs")
        )
    }
}