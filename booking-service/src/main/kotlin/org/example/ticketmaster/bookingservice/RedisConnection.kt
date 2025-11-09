package org.example.ticketmaster.bookingservice

import org.example.ticketmaster.bookingservice.model.RedisConfig
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

object RedisConnection {
    private val logger = LoggerFactory.getLogger(RedisConnection::class.java)

    fun createPool(config: RedisConfig): JedisPool {
        val poolConfig = JedisPoolConfig().apply {
            maxTotal = 20
            maxIdle = 10
            minIdle = 5
            testOnBorrow = true
            testOnReturn = true
            testWhileIdle = true
        }

        val pool = if (config.password != null) {
            JedisPool(
                poolConfig,
                config.host,
                config.port,
                config.timeout,
                config.password,
                config.database
            )
        } else {
            JedisPool(
                poolConfig,
                config.host,
                config.port,
                config.timeout
            )
        }

        logger.info("Redis connection pool created: ${config.host}:${config.port}")
        return pool
    }
}