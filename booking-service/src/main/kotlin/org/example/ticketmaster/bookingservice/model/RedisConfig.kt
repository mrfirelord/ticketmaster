package org.example.ticketmaster.bookingservice.model

import com.typesafe.config.Config

data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String?,
    val database: Int,
    val timeout: Int
) {
    companion object {
        fun from(config: Config) = RedisConfig(
            host = config.getString("host"),
            port = config.getInt("port"),
            password = if (config.hasPath("password") && !config.getIsNull("password")) {
                config.getString("password")
            } else null,
            database = config.getInt("database"),
            timeout = config.getInt("timeout")
        )
    }
}