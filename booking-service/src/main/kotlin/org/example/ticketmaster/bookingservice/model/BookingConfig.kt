package org.example.ticketmaster.bookingservice.model

import com.typesafe.config.ConfigFactory

data class BookingConfig(
    val redis: RedisConfig,
    val mongodb: MongoConfig,
    val reservation: ReservationConfig,
    val reconciliation: ReconciliationConfig
) {
    companion object {
        fun load(resourceName: String = "application.conf"): BookingConfig {
            val config = ConfigFactory.load(resourceName).getConfig("booking")
            return BookingConfig(
                redis = RedisConfig.from(config.getConfig("redis")),
                reservation = ReservationConfig.from(config.getConfig("reservation")),
                mongodb = MongoConfig.from(config.getConfig("mongodb")),
                reconciliation = ReconciliationConfig.from(config.getConfig("reconciliation"))
            )
        }
    }
}