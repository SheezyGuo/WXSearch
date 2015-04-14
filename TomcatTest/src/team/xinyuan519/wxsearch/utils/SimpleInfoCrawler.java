package team.xinyuan519.wxsearch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.bson.Document;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

public class SimpleInfoCrawler implements Runnable {
	private String targetUrl;
	private ProfileInfo profileInfo;
	private final String charset = "utf-8";
	final String urlHeader = "http://weixin.sogou.com/gzh?openid=";
	private static final Object lock = new Object();

	public SimpleInfoCrawler(String targetUrl, ProfileInfo profileInfo) {
		super();
		this.targetUrl = targetUrl;
		this.profileInfo = profileInfo;
	}

	private String getHtmlCode(String targetUrl) {
		String HTMLCode = "";
		int count = 5;
		while (count-- > 0) {
			try {
				URL url = new URL(targetUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(10 * 1000);
				conn.setReadTimeout(10 * 1000);
				conn.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E; rv:11.0) like Gecko");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), charset));
				String line = "";
				while ((line = reader.readLine()) != null) {
					HTMLCode += line + "\n";
				}
				count = 0;
			} catch (MalformedURLException e) {
				;
			} catch (IOException e) {
				;
			}
		}
		if (HTMLCode.equals("")) {
			return null;
		} else {
			return HTMLCode;
		}
	}

	private String getTitle(Parser parser) {
		String title = null;
		parser.reset();
		NodeFilter filter = new TagNameFilter("title");
		try {
			NodeList list = parser.extractAllNodesThatMatch(filter);
			title = list.elementAt(0).toPlainTextString()
					.replaceAll("&nbsp;", " ").replaceAll("&quot;", "\"");
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return title;
	}

	private String getContent(Parser parser) {
		String content = null;
		parser.reset();
		NodeFilter filter = new HasAttributeFilter("id", "js_content");
		try {
			NodeList list = parser.extractAllNodesThatMatch(filter);
			content = list.elementAt(0).toPlainTextString();
			content = content.replaceAll("&nbsp;", " ").replaceAll("&quot;",
					"\"");
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return content;
	}

	private String getDate(String htmlCode) {
		String date = null;
		date = htmlCode
				.substring(
						htmlCode.indexOf("<em id=\"post-date\" class=\"rich_media_meta rich_media_meta_text\">")
								+ "<em id=\"post-date\" class=\"rich_media_meta rich_media_meta_text\">"
										.length(),
						htmlCode.indexOf(
								"</em>",
								htmlCode.indexOf("<em id=\"post-date\" class=\"rich_media_meta rich_media_meta_text\">")));
		return date;
	}

	private String getUrl() {
		return this.targetUrl;
	}

	private String getMD5(String input) {
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

	private String getTime() {
		String time = null;
		Calendar now = Calendar.getInstance();
		time = String.format("%d/%d/%d-%d:%d:%d", now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
		return time;
	}

	private String getMilliseconds() {
		String milliseconds = null;
		milliseconds = String.valueOf(Calendar.getInstance().getTimeInMillis());
		return milliseconds;
	}

	public void crawlSimpleInfo() {
		String htmlCode = getHtmlCode(this.targetUrl);
		Parser parser = Parser.createParser(htmlCode, charset);
		String content = getContent(parser);
		DbItem item = new DbItem(getTitle(parser), content, getDate(htmlCode),
				null, null, getUrl(), getMD5(content), getTime(),
				getMilliseconds());

		MongoClient mongoClient = new MongoClient(EnvironmentInfo.dbIP,
				EnvironmentInfo.dbPort);
//		MongoDatabase historyDB = mongoClient
//				.getDatabase(EnvironmentInfo.historyDBName
//						+ EnvironmentInfo.dbNameSuffix);// 历史数据库
		MongoDatabase freshDB = mongoClient
				.getDatabase(EnvironmentInfo.freshDBName
						+ EnvironmentInfo.dbNameSuffix); // 最新数据库
//		MongoCollection<Document> historyColl = historyDB
//				.getCollection(profileInfo.getIdentity());
		MongoCollection<Document> freshColl = freshDB.getCollection(profileInfo
				.getIdentity());

		Document article = new Document();
		article.append("Title", item.getTitle());
		article.append("Content", item.getContent());
		article.append("Date", item.getDate());
		article.append("ReadNum", item.getReadNum());
		article.append("LikeNum", item.getLikeNum());
		article.append("Url", item.getUrl());
		article.append("MD5", item.getMD5());
		article.append("Time", item.getTime());
		article.append("Milliseconds", item.getMilliseconds());
//		historyColl.insertOne(article);

		synchronized (lock) {
			Document find = freshColl.find(eq("Url", item.getUrl())).first();
			if (find != null) {
				Document updateValue = new Document();
				updateValue.append("Title", item.getTitle());
				updateValue.append("Content", item.getContent());
				updateValue.append("Date", item.getDate());
				updateValue.append("ReadNum", item.getReadNum());
				updateValue.append("LikeNum", item.getLikeNum());
				updateValue.append("Url", item.getUrl());
				updateValue.append("MD5", item.getMD5());
				updateValue.append("Time", item.getTime());
				updateValue.append("Milliseconds", item.getMilliseconds());
				Document updateSetValue = new Document("$set", updateValue);
				freshColl.updateOne(eq("Url", item.getUrl()), updateSetValue);
			} else {
				freshColl.insertOne(article);
			}

			MongoDatabase db = mongoClient.getDatabase("AccountInfo"
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
		}
		mongoClient.close();
	}

	@Override
	public void run() {
		crawlSimpleInfo();
	}

	public static void main(String[] args) {
		ProfileInfo profileInfo = new ProfileInfo("gh_c5983b1900ea",
				"oIWsFt__ri8LG3IniUySdMV13M-s");
		profileInfo.init();
		SimpleInfoCrawler crawler = new SimpleInfoCrawler(
				"http://mp.weixin.qq.com/s?__biz=MjM5Njc5MjQwMQ==&mid=204701500&idx=1&sn=bd1d48cadb5361193102c5dc406785bb&3rd=MzA3MDU4NTYzMw==&scene=6#rd",
				profileInfo);
		crawler.crawlSimpleInfo();
	}
}
