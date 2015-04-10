package team.xinyuan519.wxsearch.utils;

import java.io.File;
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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

class ExtraInfoCrawler {
	private static int maxPoolSize = 5; // a phantomjs thread cost about 20 MB
										// memory
	private static final ExecutorService phantomjsPool = Executors
			.newCachedThreadPool();

	@SuppressWarnings("static-access")
	public void putTask(String targetUrl, String collName) {
		ExtraInfoCrawlThread crawlThread = new ExtraInfoCrawlThread(targetUrl,
				collName);
		this.phantomjsPool.execute(crawlThread);
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

				url = getNewUrl(this.url, param);
				driver.get(url);
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
				MongoClient mongoClient = new MongoClient(EnvironmentInfo.dbIP,
						EnvironmentInfo.dbPort);
				MongoDatabase historyDB = mongoClient
						.getDatabase(EnvironmentInfo.historyDBName
								+ EnvironmentInfo.dbNameSuffix);
				MongoDatabase freshDB = mongoClient
						.getDatabase(EnvironmentInfo.freshDBName
								+ EnvironmentInfo.dbNameSuffix);
				MongoCollection<Document> historyColl = historyDB
						.getCollection(this.collName);
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
						historyColl.updateOne(eq("Url", this.url),
								updateSetValue);
					}
				}
				mongoClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (driver != null) {
					driver.close();
				}
			}
		}
	}

	public static void main(String[] args) {
		String likeNum = "123", readNum = "234", url = "http://mp.weixin.qq.com/s?__biz=MjM5MDMxMzkwMA==&mid=204766778&idx=6&sn=4fe68e4065821f973b540a6f128d6426&3rd=MzA3MDU4NTYzMw==&scene=6#rd";
		MongoClient mongoClient = new MongoClient(EnvironmentInfo.dbIP,
				EnvironmentInfo.dbPort);
		MongoDatabase historyDB = mongoClient
				.getDatabase(EnvironmentInfo.historyDBName
						+ EnvironmentInfo.dbNameSuffix);
		MongoDatabase freshDB = mongoClient
				.getDatabase(EnvironmentInfo.freshDBName
						+ EnvironmentInfo.dbNameSuffix);
		MongoCollection<Document> historyColl = historyDB
				.getCollection("aiaiaiai_9");
		MongoCollection<Document> freshColl = freshDB
				.getCollection("aiaiaiai_9");
		if (!likeNum.equals("") && !readNum.equals("")) {
			Document find = freshColl.find(eq("Url", url)).first();
			System.out.println(find.toString());
			if (find != null) {
				Document extraInfo = new Document();
				extraInfo.put("ReadNum", readNum);
				extraInfo.put("LikeNum", likeNum);
				Document updateSetValue = new Document("$set", extraInfo);
				freshColl.updateOne(eq("Url", url), updateSetValue);
				historyColl.updateOne(eq("Url", url), updateSetValue);
			}
		}
		mongoClient.close();
	}
}
