package net.zargor.afterlife.server.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Collections;
import lombok.Getter;
import net.zargor.afterlife.server.WebServer;
import org.bson.Document;

/**
 * Created by Zargor on 08.07.2017.
 */
public class MongoDB {

	private MongoClient client;
	private MongoDatabase db;
	@Getter
	private MongoCollection<Document> playerColl;
	@Getter
	private MongoCollection<Document> groupColl;
	@Getter
	private MongoCollection<Document> moduleColl;

	public MongoDB() {
		MongoCredential credential = MongoCredential
				.createCredential(
						WebServer.getInstance().getConfig().getValue("mongodb_username"),
						WebServer.getInstance().getConfig().getValue("mongodb_database"),
						((String) WebServer.getInstance().getConfig().getValue("mongodb_password"))
								.toCharArray());
		client = new MongoClient(new ServerAddress(WebServer.getInstance().getConfig().getValue("mongodb_host").toString(), Integer.valueOf(WebServer.getInstance().getConfig().getValue("mongodb_port").toString())), Collections.singletonList(credential));
		db = client.getDatabase(WebServer.getInstance().getConfig().getValue("mongodb_database").toString());
		playerColl = db.getCollection("players");
		if (playerColl == null) {
			db.createCollection("players");
			playerColl = db.getCollection("players");
		}
		groupColl = db.getCollection("groups");
		if (groupColl == null) {
			db.createCollection("groups");
			groupColl = db.getCollection("groups");
		}
		moduleColl = db.getCollection("modules");
		if (moduleColl == null) {
			db.createCollection("modules");
			moduleColl = db.getCollection("modules");
		}
	}
}