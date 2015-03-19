package team.xinyuan519.wxsearch.utils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

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
			Node[] results = ana.analyse(key);
			for (Node result : results) {
				System.out.println(result.getCollName());
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Analyze accounts in database and calculate score for each of them.
	 * 
	 * @param key
	 *            Key word from user input.
	 * @return Double typed score.
	 * @throws UnknownHostException
	 *             Error occurred when connecting to database.
	 */
	public Node[] analyse(String key) throws UnknownHostException {
		return analyse(key, "192.168.1.118", 27017);
	}

	/**
	 * Analyze accounts in database and calculate score for each of them.
	 * 
	 * @param key
	 *            Key word from user input.
	 * @param host
	 *            Host Address of database.
	 * @param port
	 *            Port of database.
	 * @return Array of account name sorted by scores in descending order.
	 * @throws UnknownHostException
	 *             Error occurred when connecting to database.
	 */
	public Node[] analyse(String key, String host, int port)
			throws UnknownHostException {
		MongoClient mongoClient = new MongoClient(host, port);
		DB db = mongoClient.getDB("WeiXinFresh");
		List<Node> nodes = new ArrayList<Node>();
		int length = 0;
		for (String s : db.getCollectionNames()) {
			// System.out.println(s);
			if (s.compareTo("system.indexes") == 0)
				continue;
			DBCollection coll = db.getCollection(s);
			DBCursor cursor = coll.find();
			double sc = score(key, cursor);
			Node node = new Node(s, sc);
			nodes.add(node);
			length++;
		}
		// Node[] arrnodes = (Node[]) nodes.toArray();
		Node[] arrnodes = new Node[length];
		for (int i = 0; i < nodes.size(); i++) {
			arrnodes[i] = nodes.get(i);
		}

		new Node().sort(arrnodes);
		// String[] ans = new String[arrnodes.length];
		// for (int i = 0; i < arrnodes.length; i++) {
		// // System.out.println(arrnodes[i].collName);
		// ans[i] = new String(arrnodes[i].collName);
		// }
		// return ans;
		mongoClient.close();
		return arrnodes;
	}

	/**
	 * Calculate the score of one account.
	 * 
	 * @param key
	 *            Key word from user input
	 * @param cursor
	 *            Database cursor containing articles of target account.
	 * @return Score of the account
	 */
	double score(String key, DBCursor cursor) {
		int count = 0;
		double sum = 0.0;
		try {
			while (cursor.hasNext()) {
				double score = 0.0;
				count += 1;
				DBObject dbo = cursor.next();
				String sysout = "";
				double name_weight = 10;
				String name = dbo.get("Name").toString();
				double title_weight = 15;
				String title = dbo.get("Title").toString();
				double content_weight = 1;
				String content = dbo.get("Content").toString();
				double read_weight = 1;
				int iread = Integer.parseInt(dbo.get("ReadNum").toString());
				double like_weight = 0.3;
				int ilike = Integer.parseInt(dbo.get("LikeNum").toString());
				sysout += name + "\t";
				if (name.indexOf(key) >= 0)
					score += name_weight;
				sysout += score + "\t";
				if (title.indexOf(key) >= 0)
					score += title_weight;
				sysout += score + "\t";
				score += content_weight * (content.split(key).length - 1);
				sysout += score + "\t";
				// System.out.println(sysout);
				double wr = Math.pow(iread, read_weight);
				double wl = Math.pow(ilike, like_weight);
				score *= wr * wl;
				sum += score;
			}
		} finally {
			cursor.close();
		}
		return sum / count;
	}
}
