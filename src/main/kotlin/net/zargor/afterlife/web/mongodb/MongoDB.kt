package net.zargor.afterlife.web.mongodb

import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import java.util.*

/**
 * Created by Zargor on 08.07.2017.
 */
class MongoDB(config : Properties) {
   private val client : MongoClient
    val db : MongoDatabase
    var playerColl : MongoCollection<Document>?
    var groupColl : MongoCollection<Document>?
    init {
        val credential = MongoCredential.createCredential(config.getProperty("mongodb_username"), config.getProperty("mongodb_db"), config.getProperty("mongodb_password").toCharArray())
        client = MongoClient(ServerAddress(config.getProperty("mongodb_host"),config.getProperty("mongodb_port").toInt()),Arrays.asList(credential))
        db = client.getDatabase(config.getProperty("mongodb_db"))
        playerColl = db.getCollection("players")
        if(!db.listCollectionNames().contains("players")){
            db.createCollection("players")
            playerColl = db.getCollection("players")
        }
        groupColl = db.getCollection("groups")
        if(!db.listCollectionNames().contains("groups")){
            db.createCollection("groups")
            groupColl = db.getCollection("groups")
        }
    }
}