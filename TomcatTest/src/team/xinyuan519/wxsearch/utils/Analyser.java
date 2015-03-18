package team.xinyuan519.wxsearch.utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

class Node{
	double score;
	String collName;
	String remark;
	public Node(String collName, double sc){
		this.collName=collName;
		this.score=sc;
		this.remark="";
	}
	public int compareTo(Node b){
		if(this.score>b.score) return 1;
		if(this.score<b.score) return -1;
		return 0;
	}
	static void sort(Node ns[]){
		int n=ns.length;
		for(int i=n-1;i>=0;i--){
			for(int j=1;j<i;j++){
				if(ns[j].compareTo(ns[j+1])<0){
					Node t=ns[j];
					ns[j]=ns[j+1];
					ns[j+1]=t;
				}
			}
		}
	}
}

/**
 * 
 * @author zhaoyi
 *
 */
public class Analyser {
	/**
	 * The main method for test. Delete it if necessary. 
	 */
	public static void main(String args[]) {
		String key = "汽车";
		if (args.length == 2)
			key = args[1];
		Analyser ana = new Analyser();
		try {
			System.out.println(ana.analyse(key));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Analyze accounts in database and calculate score for each of them. 
	 * 
	 * @param key
	 * Key word from user input. 
	 * @return
	 * Double typed score. 
	 * @throws UnknownHostException
	 * Error occurred when connecting to database. 
	 */
	public String[] analyse(String key) throws UnknownHostException {
		return analyse(key, "192.168.1.153", 27017);
	}

	/**
	 * Analyze accounts in database and calculate score for each of them. 
	 * 
	 * @param key
	 * Key word from user input. 
	 * @param host
	 * Host Address of database. 
	 * @param port
	 * Port of database. 
	 * @return
	 * Array of account name sorted by scores in descending order. 
	 * @throws UnknownHostException
	 * Error occurred when connecting to database. 
	 */
	public String[] analyse(String key, String host, int port)
			throws UnknownHostException {
		MongoClient mongoClient = new MongoClient(host, port);
		DB db = mongoClient.getDB("WeiXinFresh");
		List<Node> nodes = new ArrayList<Node>();
		for (String s : db.getCollectionNames()) {
			if (s.compareTo("system.indexes") == 0)
				continue;
			DBCollection coll = db.getCollection(s);
			DBCursor cursor = coll.find();
			double sc = score(key, cursor);
			Node node = new Node(s, sc);
			nodes.add(node);
		}
		Node[] arrnodes=(Node[])nodes.toArray();
		Node.sort(arrnodes);
		String[] ans=new String[arrnodes.length];
		for(int i=0;i<arrnodes.length;i++){
			ans[i]=new String(arrnodes[i].collName);
		}
		return ans;
	}

	/**
	 * Calculate the score of one account. 
	 * @param key
	 * Key word from user input
	 * @param cursor
	 * Database cursor containing articles of target account. 
	 * @return
	 * Score of the account
	 */
	double score(String key, DBCursor cursor) {
		int count = 0;
		double sum = 0.0;
		try {
			while (cursor.hasNext()) {
				count += 1;
				DBObject dbo = cursor.next();
				String sysout = "";
				double name_weight = 10;
				String name = dbo.get("Name").toString();
				double title_weight = 15;
				String title = dbo.get("Title").toString();
				double content_weight = 1;
				String content = dbo.get("Content").toString();
				sysout += name + "\t";
				if (name.indexOf(key) >= 0)
					sum += name_weight;
				sysout += sum + "\t";
				if (title.indexOf(key) >= 0)
					sum += title_weight;
				sysout += sum + "\t";
				sum += content_weight * (content.split(key).length - 1);
				sysout += sum + "\t";
				// System.out.println(sysout);
			}
		} finally {
			cursor.close();
		}
		return sum / count;
	}
}
