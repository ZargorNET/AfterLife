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
class MongoDB(config : Config) {
   private val client : MongoClient
    val db : MongoDatabase
    var playerColl : MongoCollection<Document>?
    var groupColl : MongoCollection<Document>?
    init {
        val credential = MongoCredential.createCredential(config.config["mongodb_username"] as String, config.config["mongodb_database"] as String, config.config["mongodb_password"].toString().toCharArray())
        client = MongoClient(ServerAddress(config.config["mongodb_host"] as String, config.config["mongodb_port"].toString().toInt()), Arrays.asList(credential))
        db = client.getDatabase(config.config["mongodb_database"] as String?)
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