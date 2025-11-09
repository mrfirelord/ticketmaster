package org.example.ticketmaster.bookingservice.model

import com.typesafe.config.Config

data class ReservationConfig(val ttlSeconds: Int) {
    companion object {
        fun from(config: Config) = ReservationConfig(
            ttlSeconds = config.getInt("ttlSeconds")
        )
    }
}