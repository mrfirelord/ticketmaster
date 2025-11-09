package org.example.ticketmaster.searchservice.model

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.DistanceUnit
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.*
import co.elastic.clients.elasticsearch.core.search.Hit
import co.elastic.clients.json.JsonData
import org.slf4j.LoggerFactory

class EventSearchRepository(
    private val client: ElasticsearchClient,
    private val indexName: String = "events"
) {
    private val logger = LoggerFactory.getLogger(EventSearchRepository::class.java)

    fun search(request: SearchEventsRequest): SearchResult {
        logger.info("Searching events with filters: $request")

        // Build the query
        val queries = buildQueries(request)

        // Execute search
        val searchResponse = client.search({ s ->
            s
                .index(indexName)
                .query { q ->
                    q
                        .bool { b ->
                            queries.forEach { query -> b.must(query) }
                            b
                        }
                }
                .from((request.page - 1) * request.pageSize)
                .size(request.pageSize)
                .sort { sort ->
                    sort
                        .score { sc -> sc.order(SortOrder.Desc) }
                }
        }, EventElasticDocument::class.java)

        val hits = searchResponse.hits().hits()
        val total = searchResponse.hits().total()?.value() ?: 0

        logger.info("Found $total events")

        return SearchResult(
            total = total,
            events = hits.mapNotNull { it.source() },
            distances = extractDistances(hits, request.location)
        )
    }

    private fun buildQueries(request: SearchEventsRequest): List<Query> {
        val queries = mutableListOf<Query>()

        // 1. Keyword search (artist, name, description)
        request.query?.let { keyword ->
            queries.add(Query.of { q ->
                q
                    .multiMatch { m ->
                        m
                            .query(keyword)
                            .fields("name^3", "artist^2", "description")  // Boost name highest
                            .fuzziness("AUTO")
                    }
            })
        }

        // 2. Category filter (exact match)
        request.category?.let { category ->
            queries.add(Query.of { q ->
                q
                    .term { t ->
                        t
                            .field("category")
                            .value(category)
                    }
            })
        }

        // 3. City filter (exact match)
        request.city?.let { city ->
            queries.add(Query.of { q ->
                q
                    .term { t ->
                        t
                            .field("venue.location.city")
                            .value(city)
                    }
            })
        }

        // 4. Geo distance filter
        request.location?.let { loc ->
            queries.add(Query.of { q ->
                q
                    .geoDistance { g ->
                        g
                            .field("venue.location.geoPoint")
                            .distance("${loc.radius}${loc.unit}")
                            .location { l ->
                                l
                                    .latlon { ll ->
                                        ll
                                            .lat(loc.lat)
                                            .lon(loc.lon)
                                    }
                            }
                    }
            })
        }

        request.dateRange?.let { dateRange ->
            if (dateRange.from != null || dateRange.to != null) {
                queries.add(Query.of { q ->
                    q
                        .range { r ->
                            r
                                .date { d ->
                                    val builder = d.field("dateTime")
                                    dateRange.from?.let { from -> builder.gte(from) }
                                    dateRange.to?.let { to -> builder.lte(to) }
                                }
                        }
                })
            }
        }

        // 6. Price range filter (overlapping ranges)
        request.priceRange?.let { priceRange ->
            // Find events where price range overlaps with search range
            // Event's min <= search max AND event's max >= search min
            val priceQueries = mutableListOf<Query>()

            priceRange.max?.let { maxPrice ->
                priceQueries.add(Query.of { q ->
                    q
                        .range { r ->
                            r
                                .field("priceRange.min")
                                .lte(JsonData.of(maxPrice))
                        }
                })
            }

            priceRange.min?.let { minPrice ->
                priceQueries.add(Query.of { q ->
                    q
                        .range { r ->
                            r
                                .field("priceRange.max")
                                .gte(JsonData.of(minPrice))
                        }
                })
            }

            if (priceQueries.isNotEmpty()) {
                queries.add(Query.of { q ->
                    q
                        .bool { b ->
                            b
                                .must(priceQueries)
                        }
                })
            }
        }

        // If no queries, match all
        if (queries.isEmpty()) {
            queries.add(Query.of { q -> q.matchAll { it } })
        }

        return queries
    }

    private fun extractDistances(
        hits: List<Hit<EventElasticDocument>>,
        locationFilter: LocationFilter?
    ): Map<String, Double> {
        if (locationFilter == null) return emptyMap()

        // Extract distance from sort values if available
        return hits.associate { hit ->
            val distance = hit.sort()?.getOrNull(1)?.toString()?.toDoubleOrNull()
            hit.source()!!.eventId to (distance ?: 0.0)
        }
    }
}

data class SearchResult(
    val total: Long,
    val events: List<EventElasticDocument>,
    val distances: Map<String, Double>
)

// Elasticsearch document (you already have this)
data class EventElasticDocument(
    val eventId: String,
    val name: String,
    val artist: String,
    val description: String,
    val venue: VenueElastic,
    val dateTime: String,
    val category: String,
    val priceRange: PriceRangeElastic,
    val totalSeats: Int
)

data class VenueElastic(
    val name: String,
    val location: LocationElastic
)

data class LocationElastic(
    val geoPoint: GeoPoint,
    val city: String,
    val state: String,
    val address: String
)

data class GeoPoint(
    val lat: Double,
    val lon: Double
)

data class PriceRangeElastic(
    val min: Double,
    val max: Double,
    val currency: String
)