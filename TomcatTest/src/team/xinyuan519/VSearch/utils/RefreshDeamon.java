package team.xinyuan519.VSearch.utils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class RefreshDeamon {
	private final int poolSize = 5;
	
	public void refreshAccount(ExecutorService pool,ProfileInfo profileInfo) {
		RefreshThread refreshThread = new RefreshThread(profileInfo);
		pool.execute(refreshThread);
	}

	public void refreshAll() {
		ExecutorService pool = Executors.newFixedThreadPool(this.poolSize);
		ServerAddress address = new ServerAddress(EnvironmentInfo.dbIP,
				EnvironmentInfo.dbPort);
		MongoCredential credential = MongoCredential.createCredential(
				EnvironmentInfo.dbUser, EnvironmentInfo.authDB,
				EnvironmentInfo.dbPwd);
		MongoClient mongoClient = new MongoClient(address,
				Arrays.asList(credential));
		MongoDatabase database = mongoClient
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
			refreshAccount(pool,profileInfo);
		}
		cursor.close();
		mongoClient.close();
		pool.shutdown();
		while (!pool.isTerminated()) {
			try {
				pool.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void StartDeamon() {
		while (true) {
			try {
				this.refreshAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		RefreshDeamon deamon = new RefreshDeamon();
		deamon.StartDeamon();
	}
}
