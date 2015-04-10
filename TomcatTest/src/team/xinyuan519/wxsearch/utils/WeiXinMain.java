package team.xinyuan519.wxsearch.utils;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class WeiXinMain {

	public static void main(String[] args) {
		// HashMap<String, String> hashmap = new HashMap<String, String>();
		// hashmap.put("mschengdu", "oIWsFt2Xdvlz_w4LRhTdpck5rOYQ");// 成都美食
		// hashmap.put("eatsichuang", "oIWsFt7-7TVWoQ-jytY9HHRNVpeU");// 舌尖上的四川
		// hashmap.put("scms12580", "oIWsFt_EjYQNHEemcDqbCJwSWuNg");// 四川美食
		// hashmap.put("chengduchihuo", "oIWsFtxbWtyg2yemm3wQH1XsyFZ8");// 成都好吃嘴
		// // 吃货报告
		// hashmap.put("cdzhoubian", "oIWsFt8KTlgivRJJhK_FxSbop2-g");// 成都周边游
		// hashmap.put("scly100", "oIWsFt8f6ygzBERTB6HEUxMUEgw0");// 四川旅游
		// hashmap.put("scfun100", "oIWsFtyYL3NowliqdUn18-taf49U");// 玩转四川
		// hashmap.put("chengdushuajia", "oIWsFtxLWXCWV4iW0_T8Eugb7yMk");// 成都耍家
		// hashmap.put("chengduqjc", "oIWsFt9bymyOUs0SbRLDOSD2zloQ");// 成都全接触
		// hashmap.put("upchengdu", "oIWsFt3DJzRD40CMWVmoW1zzfoNA");// 成都第四城
		// hashmap.put("lifecd", "oIWsFt4JrqaLGAt3YsRX1m0AM9kI");// 成都生活
		// hashmap.put("chengdufan028", "oIWsFtyocEBzNTzizWmNXnFjZyU8");// 成都范儿
		// // 爱成都
		// // 趣微生活
		// hashmap.put("cdmlife", "oIWsFt0H0SBo6yVLF6_-y0gUA2kE");// 成都微生活
		// hashmap.put("mycitycd", "oIWsFty0GX48v1IZbTjjFZTQJ8Qo");// 玩转成都
		// hashmap.put("quweichengdu", "oIWsFtxmCDJNA-EeC-l1pJe4rNks");// 趣味成都
		// hashmap.put("sosocd", "oIWsFt1UOX3PH3yQvWGYnzpWPqfU");// 最成都
		// hashmap.put("cdcash", "oIWsFty_hKjvM4asKGb7zOdRRQmk");// 成都打折优惠
		// // 成都趣微时尚
		// hashmap.put("imeilione", "oIWsFt8fa2753cqI7XXc9N-JdzTY");// 思羽美丽女人
		// // 趣微时尚女人
		// hashmap.put("chengdumeier", "oIWsFt_GS4zgM_WDPb4uPKs7SjR8");// 成都妹儿
		// hashmap.put("wechengdu", "oIWsFtxK3-RIHp4VizC1BUioKtdE");// 微成都
		// hashmap.put("dachengnews", "oIWsFt2r23upGoUFg2NBDXa8y4cU");// 大成新闻网
		// hashmap.put("quansousuo", "oIWsFt8gUm9P5ryiS9ZJ8GG8NSkg");// 成都全搜索
		// hashmap.put("chengshang028", "oIWsFt8vl-dlDnT-aF410pusEyoo");// 成商新闻
		// hashmap.put("huaxinews", "oIWsFt2sdv0wYsTe1aEqzF85hxQU");// 华西新闻
		// hashmap.put("tianfuzixun", "oIWsFtzDqjTOeQXmTu2IhYXqR4Rs");// 天府资讯
		// hashmap.put("ssss028", "oIWsFt6mDApbFxF1IqrU7XTf1Y4M");// 成都那些事儿
		// hashmap.put("byorwx", "oIWsFt2Xq2X9PYo2jh1G3bAdupsg");// 汽车保养与维修
		// hashmap.put("cd_cheyouhui", "oIWsFt2SIJkzWiWBJwZ3MwvVIpxk");// 成都车友会
		// // 成都汽车俱乐部
		// // 趣微汽车知识
		// hashmap.put("chengduphoto", "oIWsFt3pYyBo8EcCOkmtBNUj9eTI");// 成都摄友会
		// hashmap.put("chinaday1", "oIWsFt_cbRuwM5pgldUh_mAj0_eQ");// 这才是中国
		// hashmap.put("zsdq88", "oIWsFtxoD_jYXK0uhzPZXMWHhyTI");// 姿势大全
		// int mapSize = hashmap.size();
		// WeiXinThread[] WXThreads = new WeiXinThread[mapSize];
		//
		// int delta = 1 * 3600 * 1000; // 间隔设置为1小时
		// Calendar calendar = Calendar.getInstance();
		// long start = calendar.getTimeInMillis() - delta;
		// long now = 0;
		// while (true) {
		// now = Calendar.getInstance().getTimeInMillis();
		// if (now - start >= delta) {
		// // try {
		// // Runtime.getRuntime().exec(
		// // "cmd /c TASKKILL /F /IM phantomjs.exe");
		// // } catch (IOException e) {
		// // e.printStackTrace();
		// // }
		// start += delta;
		// Iterator<String> iter = hashmap.keySet().iterator();
		// int i = 0;
		// while (iter.hasNext()) {
		// String identity = iter.next();
		// String openid = hashmap.get(identity);
		// // System.out.println(String.format("Key:%S Valule:%s",
		// // identity,openid));
		// WXThreads[i] = new WeiXinThread(identity, openid);
		// i++;
		// }
		// // while (!WXThreads[0].errorList.isEmpty()) {
		// // WXThreads[0].errorList.poll();
		// // }
		// // 最多同时5个线程在运行
		// ExecutorService pool = Executors.newFixedThreadPool(5);
		// for (int i1 = 0; i1 < mapSize; i1++) {
		// pool.execute(WXThreads[i1]);
		// }
		// pool.shutdown();
		// }
		// try {
		// Thread.sleep(1 * 60 * 1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
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

		}
		cursor.close();
		client.close();
	}

}
