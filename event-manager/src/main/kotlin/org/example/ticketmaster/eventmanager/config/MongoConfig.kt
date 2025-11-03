package org.example.ticketmaster.eventmanager.config

import com.typesafe.config.Config

data class MongoConfig(
    val host: String,
    val port: Int,
    val database: String,
    val connectionString: String
) {
    companion object {
        fun from(config: Config) = MongoConfig(
            host = config.getString("host"),
            port = config.getInt("port"),
            database = config.getString("database"),
            connectionString = config.getString("connectionString")
        )
    }
}