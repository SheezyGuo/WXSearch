package team.xinyuan519.VSearch.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.*;

public class RefreshThread implements Runnable {
	private ProfileInfo profileInfo;

	public RefreshThread(ProfileInfo profileInfo) {
		this.profileInfo = profileInfo;
	}

	public void run() {
		WebDriver driver = null;
		Queue<String> qs = profileInfo.getLinkList();
		if (qs == null) {
			System.out.println(String.format(
					"openid为%s的公众号首页:%s不能打开!",
					profileInfo.getOpenid(),
					"http://weixin.sogou.com/gzh?openid="
							+ profileInfo.getOpenid()));
			return;
		}
		try {
			String userAgent = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; MI 2S Build/JRO03L) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
			DesiredCapabilities cap = DesiredCapabilities.phantomjs();
			cap.setJavascriptEnabled(true);
			cap.setCapability("phantomjs.page.settings.userAgent", userAgent);
			cap.setCapability("phantomjs.page.settings.resourceTimeout",
					20 * 1000);

			PhantomJSDriverService.Builder builder = new PhantomJSDriverService.Builder();
			PhantomJSDriverService service = builder.usingPhantomJSExecutable(
					new File(EnvironmentInfo.PhantomJSExecutablePath)).build();
			driver = new PhantomJSDriver(service, cap);
			driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
			driver.manage().timeouts().setScriptTimeout(10, TimeUnit.MINUTES);
			Parameters p = new Parameters(EnvironmentInfo.ParametersFilePath);
			while (!qs.isEmpty()) {
				String targetUrl = qs.poll();
				// int Flag = 5;
				int Flag = 1;
				// 完成标识位 若无异常 且likeNum和readNum为非空字符则置为0 否则重新爬取
				// 防止因key和uin过期或出现异常造成遗漏
				while (Flag > 0) {
					try {
						String newUrl = null;
						// p.setPatameters(ParametersFilePath); //
						// 刷新p里key和uin的内容
						newUrl = getNewUrl(targetUrl, p);

						driver.get(newUrl);
						Thread.sleep(1000);

						String title = null;
						title = driver.getTitle();

						// 获取一篇文章的标题
						// String WebSource = driver.getPageSource();
						// Pattern titlePattern = Pattern
						// .compile("(?<=var msg_title = \")(.*?)(?=\";)");
						// Matcher titleMatcher =
						// titlePattern.matcher(WebSource);
						// if (titleMatcher.find()) {
						// title = titleMatcher.group().replaceAll("&nbsp;",
						// "");
						// }
						// System.out.println("Title:" + title);

						String content = null;
						try {
							content = driver.findElement(By.id("js_content"))
									.getText().replaceAll("\\n\\s*", "\n");
						} catch (NoSuchElementException e) {
							String htmlCode = driver.getPageSource();
							Parser parser = Parser.createParser(htmlCode,
									"utf-8");
							NodeFilter filter = new HasAttributeFilter("id",
									"js_content");
							try {
								NodeList nodeLists = parser
										.extractAllNodesThatMatch(filter);
								Node n = nodeLists.elementAt(0);
								content = n.toPlainTextString();
							} catch (ParserException e1) {
								e1.printStackTrace();
							} catch (NullPointerException e2) {
								content = "Not found";
								System.err.println("Content not found in:"
										+ newUrl);
							}
						}

						// System.out.println("Content:" + content);

						String readNum = null;
						readNum = driver.findElement(By.id("readNum"))
								.getText();
						// System.out.println("readNum:" + readNum);

						String likeNum = null;
						likeNum = driver.findElement(By.id("likeNum"))
								.getText();
						try {
							likeNum = String.valueOf(Integer.valueOf(likeNum));
						} catch (NumberFormatException e) {
							likeNum = String.valueOf(0);
						}
						// System.out.println("likeNum:" + likeNum);

						String date = null;
						date = driver.findElement(By.id("post-date")).getText();
						// System.out.println("Date:" + Date);

						// String[] date = null;
						// date = Date.split("-");
						// System.out.println(String.format(
						// "Year:%s Month:%s Day:%s\n", date[0], date[1],
						// date[2]));

						String MD5 = this.getMD5(content);
						// System.out.println("MD5:" + MD5);

						ServerAddress address = new ServerAddress(
								EnvironmentInfo.dbIP, EnvironmentInfo.dbPort);
						MongoCredential credential = MongoCredential
								.createCredential(EnvironmentInfo.dbUser,
										EnvironmentInfo.authDB,
										EnvironmentInfo.dbPwd);
						MongoClient mongoClient = new MongoClient(address,
								Arrays.asList(credential));
						// MongoDatabase historyDB = mongoClient
						// .getDatabase(EnvironmentInfo.historyDBName
						// + EnvironmentInfo.dbNameSuffix);// 历史数据库
						MongoDatabase freshDB = mongoClient
								.getDatabase(EnvironmentInfo.freshDBName
										+ EnvironmentInfo.dbNameSuffix); // 最新数据库
						// MongoCollection<Document> historyColl = historyDB
						// .getCollection(profileInfo.getIdentity());
						MongoCollection<Document> freshColl = freshDB
								.getCollection(profileInfo.getIdentity());

						Calendar now = Calendar.getInstance();

						if (!likeNum.equals("") && !readNum.equals("")) {
							Document article = new Document();
							article.append("Title", title);
							article.append("Content", content);
							article.append("Date", date);
							article.append("ReadNum", readNum);
							article.append("LikeNum", likeNum);
							article.append("Url", targetUrl);
							article.append("MD5", MD5);
							article.append(
									"Time",
									String.format("%d/%d/%d-%d:%d:%d",
											now.get(Calendar.YEAR),
											now.get(Calendar.MONTH) + 1,
											now.get(Calendar.DAY_OF_MONTH),
											now.get(Calendar.HOUR_OF_DAY),
											now.get(Calendar.MINUTE),
											now.get(Calendar.SECOND)));
							article.append("Milliseconds",
									now.getTimeInMillis());
							// historyColl.insertOne(article);

							Document query = freshColl.find(
									eq("Url", targetUrl)).first();
							if (query != null) {
								Document updateValue = new Document();
								updateValue.append("Title", title);
								updateValue.append("Content", content);
								updateValue.append("Date", date);
								updateValue.append("ReadNum", readNum);
								updateValue.append("LikeNum", likeNum);
								updateValue.append("Url", targetUrl);
								updateValue.append("MD5", MD5);
								updateValue.append("Time", String.format(
										"%d/%d/%d-%d:%d:%d",
										now.get(Calendar.YEAR),
										now.get(Calendar.MONTH) + 1,
										now.get(Calendar.DAY_OF_MONTH),
										now.get(Calendar.HOUR_OF_DAY),
										now.get(Calendar.MINUTE),
										now.get(Calendar.SECOND)));
								updateValue.append("Milliseconds",
										now.getTimeInMillis());
								Document updateSetValue = new Document("$set",
										updateValue);
								freshColl.updateOne(eq("Url", targetUrl),
										updateSetValue);
							} else {
								freshColl.insertOne(article);
							}
							MongoDatabase db = mongoClient.getDatabase(EnvironmentInfo.accountInfoDBName
									+ EnvironmentInfo.dbNameSuffix);
							MongoCollection<Document> infoColl = db
									.getCollection("accountInfo");
							Document find2 = infoColl.find(
									eq("OpenID", this.profileInfo.getOpenid())).first();
							if (find2 != null) {
								;
							} else {
								Document account = new Document();
								account.append("Name", profileInfo.getName())
										.append("Identity", profileInfo.getIdentity())
										.append("Info", profileInfo.getInfo())
										.append("OpenID", profileInfo.getOpenid())
										.append("WebUrl", profileInfo.getWebURL());
								infoColl.insertOne(account);
							}
							mongoClient.close();

							System.out.println(String.format(
									"%s Read:%s Date:%s Url:%s",
									profileInfo.getIdentity(), readNum, date,
									newUrl));
							Flag = 0;
							// System.out.println("Flag:" + Flag);
						} else {
							driver.quit();
							if (Flag > 0) {
								System.out
										.println("Getting new PhantomJSDriver......");
								driver = new PhantomJSDriver(service, cap);
							}
						}
					} catch (MongoException e) {
						e.printStackTrace();
					} catch (NoSuchElementException e) {
						System.err.println(targetUrl + "未能找出元素"
								+ e.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						Flag--;
					}
				}
			}
			if (driver != null) {
				driver.quit();
			}
		} catch (Exception e) {
			if (driver != null) {
				driver.quit();
			}
			System.out.println("Nani-.-!!!!!!!!!!!What happend?===>"
					+ e.getMessage());
			return;
		}
	}

	public String getMD5(String input) {
		try {
			byte[] byteInput = input.getBytes("utf-8");
			MessageDigest mesdig = MessageDigest.getInstance("MD5");
			mesdig.update(byteInput);
			byte[] byteMD5 = mesdig.digest();
			String MD5 = new BigInteger(1, byteMD5).toString(16);
			return MD5;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getNewUrl(String rawUrl, Parameters p) {
		if (p == null || p.getKey() == null || p.getUin() == null) {
			System.out.println("获取Key或Uin失败");
			return null;
		}
		String newUrl;
		try {
			newUrl = rawUrl.replace("#rd",
					String.format("&key=%s&uin=%s#rd", p.getKey(), p.getUin()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return newUrl;
	}
}
