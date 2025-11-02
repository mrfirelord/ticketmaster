package org.example.ticketmaster.eventmanager.model

data class EventElasticDocument(
    val eventId: String,
    val name: String,
    val artist: String,
    val description: String,
    val venue: VenueElastic,
    val dateTime: String,
    val category: String,
    val priceRange: PriceRange,
    val totalSeats: Int
) {
    companion object {
        fun from(document: EventDocument): EventElasticDocument {
            return EventElasticDocument(
                eventId = document._id!!,
                name = document.name,
                artist = document.artist,
                description = document.description,
                venue = VenueElastic(
                    name = document.venue.name,
                    location = LocationElastic(
                        geoPoint = GeoPoint(
                            lat = document.venue.location.lat,
                            lon = document.venue.location.lon
                        ),
                        city = document.venue.location.city,
                        state = document.venue.location.state,
                        address = document.venue.location.address
                    )
                ),
                dateTime = document.dateTime,
                category = document.category,
                priceRange = document.priceRange,
                totalSeats = document.totalSeats
            )
        }
    }
}