package org.example.ticketmaster.eventmanager.db

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo

object MongoConnection {
    fun createClient(connectionString: String = "mongodb://localhost:27017"): MongoClient {
        return KMongo.createClient(connectionString)
    }
    
    fun getDatabase(client: MongoClient, dbName: String = "ticketmaster"): MongoDatabase {
        return client.getDatabase(dbName)
    }
}