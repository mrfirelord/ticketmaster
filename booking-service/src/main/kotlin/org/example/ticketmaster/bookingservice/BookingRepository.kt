package org.example.ticketmaster.bookingservice

import org.example.ticketmaster.bookingservice.model.Booking
import org.example.ticketmaster.bookingservice.model.SeatAlreadyBookedException
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.ScanParams
import java.time.Instant

class BookingRepository(
    private val jedisPool: JedisPool,
    private val ttlSeconds: Int
) {
    private val logger = LoggerFactory.getLogger(BookingRepository::class.java)

    companion object {
        private const val BOOKING_KEY_PREFIX = "booking"
        private const val EVENT_SEATS_PREFIX = "event"
        private const val BOOKING_EXPIRATIONS_KEY = "booking_expirations"
    }

    // Book a seat
    fun bookSeat(eventId: String, seatId: String, userId: String): Booking {
        jedisPool.resource.use { jedis ->
            val bookingKey = "$BOOKING_KEY_PREFIX:$eventId:$seatId"
            val setKey = "$EVENT_SEATS_PREFIX:$eventId:booked_seats"
            val expirationValue = "$eventId:$seatId"
            val bookedAt = Instant.now()
            val expiresAt = bookedAt.plusSeconds(ttlSeconds.toLong())
            val expirationScore = expiresAt.epochSecond.toDouble()

            val script = """
                local bookingKey = KEYS[1]
                local setKey = KEYS[2]
                local expirationKey = KEYS[3]
                
                -- Check if already booked
                if redis.call('EXISTS', bookingKey) == 1 then
                    return 0
                end
                
                -- Create booking
                redis.call('HSET', bookingKey, 'userId', ARGV[1], 'bookedAt', ARGV[2], 'expiresAt', ARGV[3])
                redis.call('EXPIRE', bookingKey, tonumber(ARGV[4]))
                
                -- Add to event's booked seats set
                redis.call('SADD', setKey, ARGV[5])
                
                -- Track in expiration sorted set
                redis.call('ZADD', expirationKey, ARGV[6], ARGV[7])
                
                return 1
            """.trimIndent()

            val result = jedis.eval(
                script,
                listOf(bookingKey, setKey, BOOKING_EXPIRATIONS_KEY),
                listOf(
                    userId,
                    bookedAt.toString(),
                    expiresAt.toString(),
                    ttlSeconds.toString(),
                    seatId,
                    expirationScore.toString(),
                    expirationValue
                )
            ) as Long

            if (result == 0L) {
                throw SeatAlreadyBookedException(eventId, seatId)
            }

            logger.info("Seat booked: eventId=$eventId, seatId=$seatId, userId=$userId, expiresAt=$expiresAt")

            return Booking(eventId, seatId, userId, bookedAt, expiresAt)
        }
    }

    // Get booking details
    fun getBooking(eventId: String, seatId: String): Booking? {
        jedisPool.resource.use { jedis ->
            val bookingKey = "$BOOKING_KEY_PREFIX:$eventId:$seatId"
            val bookingData = jedis.hgetAll(bookingKey)

            if (bookingData.isEmpty()) {
                return null
            }

            return Booking(
                eventId = eventId,
                seatId = seatId,
                userId = bookingData["userId"]!!,
                bookedAt = Instant.parse(bookingData["bookedAt"]),
                expiresAt = Instant.parse(bookingData["expiresAt"])
            )
        }
    }

    // Get all booked seats for an event
    fun getBookedSeats(eventId: String): Set<String> {
        jedisPool.resource.use { jedis ->
            val setKey = "$EVENT_SEATS_PREFIX:$eventId:booked_seats"
            return jedis.smembers(setKey) ?: emptySet()
        }
    }

    // Check if seat is booked
    fun isBooked(eventId: String, seatId: String): Boolean {
        jedisPool.resource.use { jedis ->
            val bookingKey = "$BOOKING_KEY_PREFIX:$eventId:$seatId"
            return jedis.exists(bookingKey)
        }
    }

    // Release booking (for purchase or cancellation)
    fun releaseBooking(eventId: String, seatId: String) {
        jedisPool.resource.use { jedis ->
            val bookingKey = "$BOOKING_KEY_PREFIX:$eventId:$seatId"
            val setKey = "$EVENT_SEATS_PREFIX:$eventId:booked_seats"
            val expirationValue = "$eventId:$seatId"

            val script = """
                local bookingKey = KEYS[1]
                local setKey = KEYS[2]
                local expirationKey = KEYS[3]
                local seatId = ARGV[1]
                local expirationValue = ARGV[2]
                
                redis.call('DEL', bookingKey)
                redis.call('SREM', setKey, seatId)
                redis.call('ZREM', expirationKey, expirationValue)
                
                return 1
            """.trimIndent()

            jedis.eval(
                script,
                listOf(bookingKey, setKey, BOOKING_EXPIRATIONS_KEY),
                listOf(seatId, expirationValue)
            )

            logger.info("Booking released: eventId=$eventId, seatId=$seatId")
        }
    }

    // Get expired bookings (for reconciliation)
    fun getExpiredBookings(): List<Pair<String, String>> {
        jedisPool.resource.use { jedis ->
            val now = Instant.now().epochSecond.toDouble()

            val expiredBookings = jedis.zrangeByScore(
                BOOKING_EXPIRATIONS_KEY,
                0.0,
                now
            )

            return expiredBookings.map { booking ->
                val parts = booking.split(":")
                Pair(parts[0], parts[1]) // eventId, seatId
            }
        }
    }

    // Remove from expiration tracking
    fun removeFromExpirationTracking(eventId: String, seatId: String) {
        jedisPool.resource.use { jedis ->
            val expirationValue = "$eventId:$seatId"
            jedis.zrem(BOOKING_EXPIRATIONS_KEY, expirationValue)
        }
    }

    // Clear all bookings (for testing)
    fun clearAll() {
        jedisPool.resource.use { jedis ->
            // Clear all booking keys
            var cursor = ScanParams.SCAN_POINTER_START
            do {
                val scanResult = jedis.scan(cursor, ScanParams().match("$BOOKING_KEY_PREFIX:*").count(100))
                cursor = scanResult.cursor

                if (scanResult.result.isNotEmpty()) {
                    jedis.del(*scanResult.result.toTypedArray())
                }
            } while (cursor != ScanParams.SCAN_POINTER_START)

            // Clear all event sets
            cursor = ScanParams.SCAN_POINTER_START
            do {
                val scanResult = jedis.scan(cursor, ScanParams().match("$EVENT_SEATS_PREFIX:*:booked_seats").count(100))
                cursor = scanResult.cursor

                if (scanResult.result.isNotEmpty()) {
                    jedis.del(*scanResult.result.toTypedArray())
                }
            } while (cursor != ScanParams.SCAN_POINTER_START)

            // Clear expiration tracking
            jedis.del(BOOKING_EXPIRATIONS_KEY)

            logger.info("All bookings cleared")
        }
    }
}