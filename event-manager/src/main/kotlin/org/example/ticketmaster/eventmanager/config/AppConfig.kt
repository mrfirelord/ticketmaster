package org.example.ticketmaster.eventmanager.config

import com.typesafe.config.ConfigFactory

data class AppConfig(
    val name: String,
    val mongodb: MongoConfig,
    val elasticsearch: ElasticsearchConfig,
    val redis: RedisConfig,
    val sync: SyncConfig
) {
    companion object {
        fun load(resourceName: String = "application.conf"): AppConfig {
            val config = ConfigFactory.load(resourceName).getConfig("app")
            return AppConfig(
                name = config.getString("name"),
                mongodb = MongoConfig.from(config.getConfig("mongodb")),
                elasticsearch = ElasticsearchConfig.from(config.getConfig("elasticsearch")),
                redis = RedisConfig.from(config.getConfig("redis")),
                sync = SyncConfig.from(config.getConfig("sync"))
            )
        }
    }
}