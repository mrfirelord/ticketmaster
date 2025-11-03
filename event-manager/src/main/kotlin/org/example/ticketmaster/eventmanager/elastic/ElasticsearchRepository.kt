package org.example.ticketmaster.eventmanager.elastic

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.ssl.SSLContextBuilder
import org.elasticsearch.client.RestClient
import org.example.ticketmaster.eventmanager.model.EventElasticDocument
import javax.net.ssl.SSLContext

class ElasticsearchRepository(
    host: String,
    port: Int,
    scheme: String,
    username: String,
    password: String,
    disableSslVerification: Boolean
) {
    private val restClient = RestClient.builder(HttpHost(host, port, scheme))
        .setHttpClientConfigCallback { httpClientBuilder ->
            // Add authentication
            val credentialsProvider = BasicCredentialsProvider()
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                UsernamePasswordCredentials(username, password)
            )
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)

            // Configure SSL if needed
            if (disableSslVerification && scheme == "https") {
                configureHttpsClient(httpClientBuilder)
            }

            httpClientBuilder
        }
        .build()

    private val transport = RestClientTransport(
        restClient,
        JacksonJsonpMapper()
    )

    private val client = ElasticsearchClient(transport)

    private val indexName = "events"

    private fun configureHttpsClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder {
        val sslContext: SSLContext = SSLContextBuilder()
            .loadTrustMaterial(null) { _, _ -> true }
            .build()

        return httpClientBuilder
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
    }

    fun indexEvent(document: EventElasticDocument): String {
        val response = client.index { i ->
            i
                .index(indexName)
                .id(document.eventId)
                .document(document)
        }
        return response.id()
    }

    fun clearAll(): Long {
        val response = client.deleteByQuery { d -> d.index(indexName).query { q -> q.matchAll { it } } }
        return response.deleted()!!
    }

    fun deleteEvent(eventId: String): Boolean {
        val response = client.delete { d -> d.index(indexName).id(eventId) }
        return response.result().name == "DELETED"
    }

    fun close() {
        transport.close()
        restClient.close()
    }
}