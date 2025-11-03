package org.example.ticketmaster.eventmanager

import org.example.ticketmaster.eventmanager.db.ElasticsearchSyncRepository
import org.example.ticketmaster.eventmanager.db.EventRepository
import org.example.ticketmaster.eventmanager.db.MongoConnection
import org.example.ticketmaster.eventmanager.elastic.ElasticsearchRepository
import org.example.ticketmaster.eventmanager.model.CreateEventRequest
import org.example.ticketmaster.eventmanager.model.ElasticsearchSyncDocument
import org.example.ticketmaster.eventmanager.model.Location
import org.example.ticketmaster.eventmanager.model.PriceRange
import org.example.ticketmaster.eventmanager.model.Venue
import org.example.ticketmaster.eventmanager.service.EventService
import java.util.concurrent.Executors
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

object EventServiceStressTest {
    fun runStressTest(
        eventService: EventService,
        numThreads: Int = 10,
        requestsPerThread: Int = 10
    ) {
        println("=== Starting Stress Test ===")
        println("Threads: $numThreads")
        println("Requests per thread: $requestsPerThread")
        println("Total requests: ${numThreads * requestsPerThread}")
        println()

        val executor = Executors.newFixedThreadPool(numThreads)
        val latch = CountDownLatch(numThreads * requestsPerThread)

        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        val startTime = System.currentTimeMillis()

        // Submit tasks
        repeat(numThreads) { threadId ->
            repeat(requestsPerThread) { requestId ->
                executor.submit {
                    try {
                        val request = generateRandomEvent(threadId, requestId)
                        eventService.createEvent(request)
                        successCount.incrementAndGet()
                        println("✓ Thread-$threadId Request-$requestId: SUCCESS")
                    } catch (e: Exception) {
                        failureCount.incrementAndGet()
                        println("✗ Thread-$threadId Request-$requestId: FAILED - ${e.message}")
                    } finally {
                        latch.countDown()
                    }
                }
            }
        }

        // Wait for all tasks to complete
        latch.await()
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        val endTime = System.currentTimeMillis()
        val durationMs = endTime - startTime

        // Print results
        println()
        println("=== Stress Test Results ===")
        println("Duration: ${durationMs}ms (${durationMs / 1000.0}s)")
        println("Successful: ${successCount.get()}")
        println("Failed: ${failureCount.get()}")
        println("Throughput: ${(numThreads * requestsPerThread) / (durationMs / 1000.0)} requests/sec")
    }

    private fun generateRandomEvent(threadId: Int, requestId: Int): CreateEventRequest {
        val artists = listOf("Taylor Swift", "Ed Sheeran", "Coldplay", "The Weeknd", "Adele")
        val categories = listOf("Concert", "Sports", "Theater", "Comedy", "Festival")
        val venues = listOf(
            Triple("Madison Square Garden", 40.7505, -73.9934),
            Triple("Staples Center", 34.0430, -118.2673),
            Triple("Wembley Stadium", 51.5560, -0.2796),
            Triple("Tokyo Dome", 35.7056, 139.7519),
            Triple("Melbourne Cricket Ground", -37.8200, 144.9834)
        )

        val artist = artists.random()
        val category = categories.random()
        val (venueName, lat, lon) = venues.random()
        val randomPrice = Random.nextDouble(50.0, 500.0)

        return CreateEventRequest(
            name = "$artist - World Tour [T$threadId-R$requestId]",
            artist = artist,
            description = "An amazing ${category.lowercase()} event featuring $artist. Don't miss it!",
            venue = Venue(
                name = venueName,
                location = Location(
                    lat = lat + Random.nextDouble(-0.01, 0.01),
                    lon = lon + Random.nextDouble(-0.01, 0.01),
                    city = "Test City",
                    state = "TC",
                    address = "123 Test Street"
                )
            ),
            dateTime = "2025-12-${Random.nextInt(1, 28).toString().padStart(2, '0')}T19:00:00Z",
            category = category,
            priceRange = PriceRange(
                min = randomPrice,
                max = randomPrice * 1.5,
                currency = "USD"
            ),
            totalSeats = Random.nextInt(5000, 50000)
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // Initialize services
        val mongoClient = MongoConnection.createClient()
        val database = MongoConnection.getDatabase(mongoClient)
        val eventRepository = EventRepository(database)
        val syncRepository = ElasticsearchSyncRepository(database)
        val elasticsearchRepository = ElasticsearchRepository(
            host = "161.35.180.47",
            port = 9200,
            scheme = "https",
            disableSslVerification = true,
            username = "elastic",
            password = "-nFBfu8wpuTCyM_tn8Wa"
        )

        val eventService = EventService(eventRepository, elasticsearchRepository, syncRepository)

        // Clear existing data (optional)
        println("Clearing existing data...")
        eventRepository.clearAll()
        syncRepository.clearAll()
        elasticsearchRepository.clearAll()

        // Run stress test

        // Light test: 5 threads, 10 requests each = 50 total
        EventServiceStressTest.runStressTest(eventService, numThreads = 5, requestsPerThread = 1000)

        // Medium test: 10 threads, 20 requests each = 200 total
        // stressTest.runStressTest(numThreads = 10, requestsPerThread = 20)

        // Heavy test: 20 threads, 50 requests each = 1000 total
        // stressTest.runStressTest(numThreads = 20, requestsPerThread = 50)

        mongoClient.close()
        elasticsearchRepository.close()
    }
}