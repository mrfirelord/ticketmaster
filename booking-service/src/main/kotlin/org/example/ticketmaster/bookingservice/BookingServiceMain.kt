package org.example.ticketmaster.bookingservice

import org.example.ticketmaster.bookingservice.model.BookingConfig
import org.example.ticketmaster.bookingservice.model.BookingRequest
import org.example.ticketmaster.bookingservice.model.SeatAlreadyBookedException
import org.litote.kmongo.KMongo
import org.slf4j.LoggerFactory

object BookingServiceMain {
    private val logger = LoggerFactory.getLogger("BookingServiceApp")

    @JvmStatic
    fun main(args: Array<String>) {
//        val config = BookingConfig.load()
//
//        // Redis connection
//        val jedisPool = RedisConnection.createPool(config.redis)
//        jedisPool.resource.use { it.configSet("notify-keyspace-events", "Ex") }
//
//        // MongoDB connection
//        val mongoClient = KMongo.createClient(config.mongodb.connectionString)
//        val database = mongoClient.getDatabase(config.mongodb.database)
//        logger.info("MongoDB connected: ${config.mongodb.host}:${config.mongodb.port}")
//
//        // Repositories
//        val bookingRepository = BookingRepository(jedisPool, config.reservation.ttlSeconds)
//        val purchaseRepository = PurchaseRepository(database)
//
//        // Service
//        val bookingService = BookingService(bookingRepository, purchaseRepository)
//
//        // Reconciliation
//        val reconciliationService = BookingReconciliationService(
//            jedisPool,
//            bookingRepository,
//            config.reconciliation.intervalMs
//        )
//        reconciliationService.start()
//
//        logger.info("Booking service started successfully")
//
//        // Shutdown hook
//        Runtime.getRuntime().addShutdownHook(Thread {
//            reconciliationService.stop()
//            jedisPool.close()
//            mongoClient.close()
//            logger.info("Shutdown complete")
//        })
//
//        Thread.currentThread().join()
        testBookingService()
    }

    fun testBookingService() {
        val config = BookingConfig.load()

        // Redis connection
        val jedisPool = RedisConnection.createPool(config.redis)
        jedisPool.resource.use { it.configSet("notify-keyspace-events", "Ex") }

        // MongoDB connection
        val mongoClient = KMongo.createClient(config.mongodb.connectionString)
        val database = mongoClient.getDatabase(config.mongodb.database)
        logger.info("MongoDB connected: ${config.mongodb.host}:${config.mongodb.port}")

        // Repositories
        val bookingRepository = BookingRepository(jedisPool, config.reservation.ttlSeconds)
        val purchaseRepository = PurchaseRepository(database)
        val bookingService = BookingService(bookingRepository, purchaseRepository)

        // 1. User books seat
        val bookingResponse = bookingService.bookSeat(
            BookingRequest("evt123", "A-5-13", "user456")
        )
        println("Booked: $bookingResponse")

        // 2. User completes purchase (within 10 minutes)
        val purchaseResponse = bookingService.purchaseSeat(
            eventId = "evt123",
            seatId = "A-5-13",
            userId = "user456"
        )
        println("Purchased: $purchaseResponse")

        // 3. Check purchased seats
        val purchasedSeats = bookingService.getPurchasedSeats("evt123")
        println("Purchased seats for event: $purchasedSeats")
    }
}