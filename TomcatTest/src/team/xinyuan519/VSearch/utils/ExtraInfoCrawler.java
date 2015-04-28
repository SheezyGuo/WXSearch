package team.xinyuan519.VSearch.utils;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.*;

public class ExtraInfoCrawler {
	private static int maxPoolSize = 10; // a phantomjs thread cost about 20 MB
										// memory
	private static final ExecutorService phantomjsPool = Executors
			.newFixedThreadPool(maxPoolSize);
	
	public void putTask(String targetUrl, String collName) {
		ExtraInfoCrawlThread crawlThread = new ExtraInfoCrawlThread(targetUrl,
				collName);
		ExtraInfoCrawler.phantomjsPool.execute(crawlThread);
	}

	private class ExtraInfoCrawlThread implements Runnable {
		private String url;
		private String collName;

		public ExtraInfoCrawlThread(String url, String collName) {
			this.url = url;
			this.collName = collName;
		}

		public String getNewUrl(String rawUrl, Parameters p) {
			if (p == null || p.getKey() == null || p.getUin() == null) {
				System.out.println("获取Key或Uin失败");
				return null;
			}
			String newUrl;
			try {
				newUrl = rawUrl.replace(
						"#rd",
						String.format("&key=%s&uin=%s#rd", p.getKey(),
								p.getUin()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return newUrl;
		}

		@Override
		public void run() {
			WebDriver driver = null;
			MongoClient mongoClient = null;
			try {
				String userAgent = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; MI 2S Build/JRO03L) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
				DesiredCapabilities cap = DesiredCapabilities.phantomjs();
				cap.setJavascriptEnabled(true);
				cap.setCapability("phantomjs.page.settings.userAgent",
						userAgent);
				cap.setCapability("phantomjs.page.settings.resourceTimeout",
						20 * 1000);

				PhantomJSDriverService.Builder builder = new PhantomJSDriverService.Builder();
				PhantomJSDriverService service = builder
						.usingPhantomJSExecutable(
								new File(
										EnvironmentInfo.PhantomJSExecutablePath))
						.build();
				driver = new PhantomJSDriver(service, cap);
				driver.manage().timeouts()
						.pageLoadTimeout(10, TimeUnit.MINUTES);
				driver.manage().timeouts()
						.setScriptTimeout(10, TimeUnit.MINUTES);
				Parameters param = new Parameters(
						EnvironmentInfo.ParametersFilePath);

				String newurl = getNewUrl(this.url, param);
				driver.get(newurl);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String readNum = null;
				readNum = driver.findElement(By.id("readNum")).getText();

				String likeNum = null;
				likeNum = driver.findElement(By.id("likeNum")).getText();
				try {
					likeNum = String.valueOf(Integer.valueOf(likeNum));
				} catch (NumberFormatException e) {
					likeNum = String.valueOf(0);
				}
				
				ServerAddress address = new ServerAddress(
						EnvironmentInfo.dbIP, EnvironmentInfo.dbPort);
				MongoCredential credential = MongoCredential
						.createCredential(EnvironmentInfo.dbUser,
								EnvironmentInfo.authDB,
								EnvironmentInfo.dbPwd);
				mongoClient = new MongoClient(address,
						Arrays.asList(credential));
//				MongoDatabase historyDB = mongoClient
//						.getDatabase(EnvironmentInfo.historyDBName
//								+ EnvironmentInfo.dbNameSuffix);
				MongoDatabase freshDB = mongoClient
						.getDatabase(EnvironmentInfo.freshDBName
								+ EnvironmentInfo.dbNameSuffix);
//				MongoCollection<Document> historyColl = historyDB
//						.getCollection(this.collName);
				MongoCollection<Document> freshColl = freshDB
						.getCollection(this.collName);
				if (!likeNum.equals("") && !readNum.equals("")) {
					Document find = freshColl.find(eq("Url", this.url)).first();
					if (find != null) {
						Document extraInfo = new Document();
						extraInfo.put("ReadNum", readNum);
						extraInfo.put("LikeNum", likeNum);
						Document updateSetValue = new Document("$set",
								extraInfo);
						freshColl
								.updateOne(eq("Url", this.url), updateSetValue);
//						historyColl.updateOne(eq("Url", this.url),
//								updateSetValue);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (driver != null) {
					driver.quit();
				}
				if (mongoClient != null) {
					mongoClient.close();
				}
			}
		}
	}

	public static void main(String[] args) {
		// String likeNum = "123", readNum = "234", url =
		// "http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204468998&idx=1&sn=5387eb639ffddf2aa04b55f9cac4d33f&3rd=MzA3MDU4NTYzMw==&scene=6#rd";
		// MongoClient mongoClient = new MongoClient(EnvironmentInfo.dbIP,
		// EnvironmentInfo.dbPort);
		// MongoDatabase historyDB = mongoClient
		// .getDatabase(EnvironmentInfo.historyDBName
		// + EnvironmentInfo.dbNameSuffix);
		// MongoDatabase freshDB = mongoClient
		// .getDatabase(EnvironmentInfo.freshDBName
		// + EnvironmentInfo.dbNameSuffix);
		// MongoCollection<Document> historyColl = historyDB
		// .getCollection("BOSYcomic");
		// MongoCollection<Document> freshColl = freshDB
		// .getCollection("BOSYcomic");
		// if (!likeNum.equals("") && !readNum.equals("")) {
		// Document find = freshColl.find(eq("Url", url)).first();
		// System.out.println(find.toString());
		// if (find != null) {
		// Document extraInfo = new Document();
		// extraInfo.put("ReadNum", readNum);
		// extraInfo.put("LikeNum", likeNum);
		// Document updateSetValue = new Document("$set", extraInfo);
		// freshColl.updateOne(eq("Url", url), updateSetValue);
		// historyColl.updateOne(eq("Url", url), updateSetValue);
		// }
		// }
		// mongoClient.close();

		String[] links = new String[] {
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204468998&idx=1&sn=5387eb639ffddf2aa04b55f9cac4d33f&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204468998&idx=2&sn=17a4ca6597aa6e4222187188fa6ea6a9&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204468998&idx=3&sn=736f8060ea8958120c72012ea2b3790a&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204419995&idx=1&sn=7e7c195943a8a46d2e2d1f4acbefccad&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204419995&idx=2&sn=de53af0cceb1f4d434f98a38f0d1035c&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204419995&idx=3&sn=771d0f1add2112edf954f3f3b4628266&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204395452&idx=1&sn=2b71731677cf12ad454d1bcc3279f88f&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204288630&idx=1&sn=019f38bc1e2973860327c76a5fdf94d2&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204288630&idx=3&sn=5db079e2cc93207389623c20ee89e6d3&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				"http://mp.weixin.qq.com/s?__biz=MzA5OTAxNzEyOQ==&mid=204288630&idx=2&sn=838151dc7df5603a023370fb7e22eb81&3rd=MzA3MDU4NTYzMw==&scene=6#rd" };
		for (String str : links) {
			ExtraInfoCrawler crawler = new ExtraInfoCrawler();
			crawler.putTask(str, "BOSYcomic");
		}
	}
}
