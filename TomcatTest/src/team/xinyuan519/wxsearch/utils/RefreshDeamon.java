package team.xinyuan519.wxsearch.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class RefreshDeamon {
	private final static int poolSize = 5;
	private ExecutorService pool;

	public RefreshDeamon() {
		this.pool = Executors.newFixedThreadPool(poolSize);
	}

	public void refreshAccount(ProfileInfo profileInfo) {
		RefreshThread refreshThread = new RefreshThread(profileInfo);
		this.pool.execute(refreshThread);
	}

	public void StartDeamon() {
		MongoClient client = new MongoClient(EnvironmentInfo.dbIP,
				EnvironmentInfo.dbPort);
		MongoDatabase database = client
				.getDatabase(EnvironmentInfo.accountInfoDBName
						+ EnvironmentInfo.dbNameSuffix);
		MongoCollection<Document> coll = database.getCollection("accountInfo");
		MongoCursor<Document> cursor = coll.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			String identity = doc.getString("Identity");
			String openID = doc.getString("OpenID");
			ProfileInfo profileInfo = new ProfileInfo(identity, openID);
			profileInfo.init();
			refreshAccount(profileInfo);
		}
		cursor.close();
		client.close();
		this.pool.shutdown();
	}

}
