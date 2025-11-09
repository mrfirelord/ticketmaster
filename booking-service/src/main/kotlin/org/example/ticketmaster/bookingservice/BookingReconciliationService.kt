package org.example.ticketmaster.bookingservice

import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class BookingReconciliationService(
    private val jedisPool: JedisPool,
    private val bookingRepository: BookingRepository,
    private val reconciliationIntervalMs: Long
) {
    private val logger = LoggerFactory.getLogger(BookingReconciliationService::class.java)
    private var listenerThread: Thread? = null
    private var reconciliationTimer: Timer? = null
    
    fun start() {
        startExpirationListener()
        if (reconciliationIntervalMs > 0)
            startPeriodicReconciliation()
        logger.info("Booking reconciliation service started")
    }
    
    fun stop() {
        listenerThread?.interrupt()
        reconciliationTimer?.cancel()
        logger.info("Booking reconciliation service stopped")
    }
    
    private fun startExpirationListener() {
        listenerThread = Thread {
            try {
                jedisPool.resource.use { jedis ->
                    val currentConfig = jedis.configGet("notify-keyspace-events")
                    logger.info("Current notify-keyspace-events: $currentConfig")

                    // Set configuration
                    jedis.configSet("notify-keyspace-events", "Ex")

                    // Verify it was set
                    val newConfig = jedis.configGet("notify-keyspace-events")
                    logger.info("Updated notify-keyspace-events: $newConfig")

                    logger.info("Starting Redis keyspace expiration listener")
                    
                    jedis.subscribe(object : JedisPubSub() {
                        override fun onMessage(channel: String, message: String) {
                            if (channel == "__keyevent@0__:expired") {
                                handleExpiredKey(message)
                            }
                        }
                        
                        override fun onSubscribe(channel: String, subscribedChannels: Int) {
                            logger.info("Subscribed to channel: $channel")
                        }
                    }, "__keyevent@0__:expired")
                }
            } catch (e: Exception) {
                logger.error("Expiration listener error: ${e.message}", e)
                // In production, implement retry logic here
            }
        }.apply {
            name = "Redis-Expiration-Listener"
            isDaemon = true
            start()
        }
    }
    
    private fun handleExpiredKey(key: String) {
        try {
            // key format: "booking:eventId:seatId"
            val parts = key.split(":")
            if (parts[0] == "booking" && parts.size == 3) {
                val eventId = parts[1]
                val seatId = parts[2]
                
                cleanupExpiredBooking(eventId, seatId)
                logger.debug("Cleaned up expired booking via notification: $eventId:$seatId")
            }
        } catch (e: Exception) {
            logger.error("Error handling expired key: $key", e)
        }
    }
    
    private fun cleanupExpiredBooking(eventId: String, seatId: String) {
        jedisPool.resource.use { jedis ->
            val setKey = "event:$eventId:booked_seats"
            val expirationValue = "$eventId:$seatId"
            
            jedis.srem(setKey, seatId)
            jedis.zrem("booking_expirations", expirationValue)
        }
    }
    
    private fun startPeriodicReconciliation() {
        reconciliationTimer = Timer("Booking-Reconciliation", true).apply {
            scheduleAtFixedRate(0, reconciliationIntervalMs) {
                try {
                    reconcileExpiredBookings()
                } catch (e: Exception) {
                    logger.error("Reconciliation error", e)
                }
            }
        }
        
        logger.info("Periodic reconciliation started (interval: ${reconciliationIntervalMs}ms)")
    }
    
    private fun reconcileExpiredBookings() {
        val expiredBookings = bookingRepository.getExpiredBookings()
        
        if (expiredBookings.isEmpty()) {
            logger.debug("Reconciliation: no expired bookings found")
            return
        }
        
        logger.info("Reconciliation: found ${expiredBookings.size} potentially expired bookings")
        
        var cleaned = 0
        expiredBookings.forEach { (eventId, seatId) ->
            // Verify booking is actually gone
            if (!bookingRepository.isBooked(eventId, seatId)) {
                cleanupExpiredBooking(eventId, seatId)
                cleaned++
            } else {
                // Still exists - shouldn't happen, remove from expiration tracking
                bookingRepository.removeFromExpirationTracking(eventId, seatId)
                logger.warn("Booking still exists but was in expiration queue: $eventId:$seatId")
            }
        }
        
        logger.info("Reconciliation: cleaned $cleaned orphaned bookings")
    }
}