package org.example.ticketmaster.eventmanager.config

import com.typesafe.config.Config

data class ElasticsearchConfig(
    val host: String,
    val port: Int,
    val scheme: String,
    val username: String,
    val password: String,
    val disableSslVerification: Boolean,
    val indexName: String
) {
    companion object {
        fun from(config: Config) = ElasticsearchConfig(
            host = config.getString("host"),
            port = config.getInt("port"),
            scheme = config.getString("scheme"),
            username = config.getString("username"),
            password = config.getString("password"),
            disableSslVerification = config.getBoolean("disableSslVerification"),
            indexName = config.getString("indexName")
        )
    }
}