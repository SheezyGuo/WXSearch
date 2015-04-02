package team.xinyuan519.wxsearch.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class WeiXinThread implements Runnable {

	private ProfileInfo pl;
	private String identity;
	private String openid;
	private String dbNameSuffix;
	private WebDriver driver = null;

	// on Windows
	// private final String PhantomJSExecutablePath =
	// "D:\\PhantomJS\\phantomjs-2.0.0-windows\\bin\\phantomjs.exe";
	// private final String ParametersFilePath = "D:\\rawURL.txt";

	// on Linux
	private final String PhantomJSExecutablePath = "/home/dtlvhyy/APPS/Phanjomjs/phantomjs/bin/phantomjs";
	private final String ParametersFilePath = "/home/dtlvhyy/rawURL";

	public WeiXinThread(String identity, String openid) {
		this.identity = identity;
		this.openid = openid;
		this.pl = new ProfileInfo(identity, openid);
		this.dbNameSuffix = "";
	}

	public void setDbNameSuffix(String suffix) {
		this.dbNameSuffix = suffix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		Queue<String> qs = pl.getLinkListByPhantomJS();
		if (qs == null) {
			System.out.println(String.format("openid为%s的公众号首页:%s不能打开!", openid,
					"http://weixin.sogou.com/gzh?openid=" + openid));
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
					new File(PhantomJSExecutablePath)).build();
			driver = new PhantomJSDriver(service, cap);
			driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
			driver.manage().timeouts().setScriptTimeout(10, TimeUnit.MINUTES);
			Parameters p = new Parameters(ParametersFilePath);
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

						Mongo mongo = new Mongo("localhost", 27017);
						DB historyDB = mongo.getDB("WeiXinHistory"
								+ this.dbNameSuffix);// 历史数据库
						DB freshDB = mongo.getDB("WeiXinFresh"
								+ this.dbNameSuffix); // 最新数据库
						DBCollection historyColl = historyDB
								.getCollection(this.identity);
						DBCollection freshColl = freshDB
								.getCollection(this.identity);

						Calendar now = Calendar.getInstance();

						if (!likeNum.equals("") && !readNum.equals("")) {
							BasicDBObject article = new BasicDBObject();
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
							historyColl.insert(article);

							BasicDBObject query = new BasicDBObject("MD5", MD5);
							DBCursor cursor = freshColl.find(query);
							if (cursor.hasNext()) {
								BasicDBObject updateValue = new BasicDBObject();
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
								BasicDBObject updateSetValue = new BasicDBObject(
										"$set", updateValue);
								freshColl.update(query, updateSetValue);
							} else {
								freshColl.insert(article);
							}
							DB db = mongo.getDB("AccountInfo"
									+ this.dbNameSuffix);
							DBCollection infoColl = db
									.getCollection("accountInfo");
							BasicDBObject query2 = new BasicDBObject("OpenID",
									pl.getOpenid());
							DBCursor cursor2 = infoColl.find(query2);
							if (cursor2.hasNext()) {
								;
							} else {
								BasicDBObject item = new BasicDBObject();
								item.append("Name", pl.getName())
										.append("Identity", pl.getIdentity())
										.append("Info", pl.getInfo())
										.append("OpenID", pl.getOpenid())
										.append("WebUrl", pl.getWebURL());
								infoColl.insert(item);
							}
							mongo.close();

							System.out.println(String.format(
									"%s Read:%s Date:%s Url:%s", this.identity,
									readNum, date, newUrl));
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
					} catch (UnknownHostException e) {
						e.printStackTrace();
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

	public static void main(String[] args) {
		WeiXinThread wxt = new WeiXinThread("wechengdu",
				"oIWsFtxK3-RIHp4VizC1BUioKtdE");
		Thread t = new Thread(wxt);
		t.start();
		// String likeNum = "123Like";
		// try {
		// likeNum = String.valueOf(Integer.valueOf(likeNum));
		// } catch (NumberFormatException e) {
		// likeNum = String.valueOf(0);
		// }
		// System.out.println(likeNum);
	}
}