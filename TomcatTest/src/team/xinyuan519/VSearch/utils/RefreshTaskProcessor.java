package team.xinyuan519.VSearch.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class RefreshTaskProcessor implements Runnable {
	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> coll;
	private final String user = "JavaOperator";
	private final char[] pwd = "gsh632260737".toCharArray();
	private final String authDB = "admin";
	private final int DBFullCount = 10000;
	private String keyWord;
	private boolean isInitialized = false;
	
	/*
	 * run() method is designed to speed up addTask() method,so use it after
	 * this.keyWord is specified!!
	 */
	
	public RefreshTaskProcessor() {
		this.initialize();
	}
	
	public RefreshTaskProcessor(String keyWord){
		this.keyWord = keyWord;
	}
	
	@Override
	public void run() {
		if(this.keyWord == null){
			System.err.println("This method is used for speeding up addTask(),specify the keyWord parameter first");
			return;
		}
		this.initialize();
		this.addTask(this.keyWord);
	}

	public void initialize() {
		if (this.isInitialized != true) {
			MongoCredential credential = MongoCredential.createCredential(user,
					authDB, pwd);
			ServerAddress address = new ServerAddress(EnvironmentInfo.dbIP,
					EnvironmentInfo.dbPort);
			this.client = new MongoClient(address, Arrays.asList(credential));
			this.db = client.getDatabase("RefreshTask");
			this.coll = db.getCollection("refreshTask");
			this.isInitialized = true;
		}
	}

	public long getColletionCount() {
		return this.coll.count();
	}

	public void addTask(String keyWord) {
		Document doc = new Document();
		doc.append("KeyWrod", keyWord).append("Finished", "False");
		this.coll.insertOne(doc);
	}
	
	public void markFinished(String keyWord){
		Document query = new Document("KeyWord",keyWord);
		Document doc = new Document();
		doc.append("KeyWord",keyWord).append("Finished","True");
		this.coll.findOneAndReplace(query, doc);
	}
	
	public Queue<String> getUnfinishedTaskList() {
		Queue<String> list = new LinkedList<String>();
		Document query = new Document("Finished", "False");
		FindIterable<Document> iterator = coll.find(query);
		MongoCursor<Document> cursor = iterator.iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			String keyWord = doc.getString("KeyWord");
			list.offer(keyWord);
		}
		return list;
	}

	public void removeAllCompletedTask() {
		Document query = new Document("Finished", "True");
		FindIterable<Document> iterator = coll.find(query);
		MongoCursor<Document> cursor = iterator.iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			coll.deleteOne(doc);
		}
	}

	public void releaseDBWhenFull() {
		if (this.getColletionCount() >= this.DBFullCount) {
			this.removeAllCompletedTask();
		}
	}
	
	public void process(){
		while(true){
			Queue<String> taskList = this.getUnfinishedTaskList();
			int count = taskList.size();
			if(count == 0){
				try {
					System.out.println("Nothing to do,sleep for 2 min...");
					Thread.sleep(2*60*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else{
				this.releaseDBWhenFull();
				while(!taskList.isEmpty()){
					String keyWord = taskList.poll();
					KeyWordsHandler handler = new KeyWordsHandler(keyWord);
					handler.handleKeyWords();
					this.markFinished(keyWord);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		RefreshTaskProcessor processor = new RefreshTaskProcessor();
//		Queue<String> list = processor.getUnfinishedTaskList();
//		if (list.size() == 0) {
//			System.out.println("Empty list");
//			return;
//		}
//		while (!list.isEmpty()) {
//			String str = list.poll();
//			System.out.println(str);
//		}
//		processor.removeAllCompletedTask();
//		processor.markFinished("linyu");
		processor.process();
	}

}
