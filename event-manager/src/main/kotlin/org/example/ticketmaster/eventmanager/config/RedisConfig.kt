package org.example.ticketmaster.eventmanager.config

import com.typesafe.config.Config

data class RedisConfig(
    val host: String,
    val port: Int
) {
    companion object {
        fun from(config: Config) = RedisConfig(
            host = config.getString("host"),
            port = config.getInt("port")
        )
    }
}