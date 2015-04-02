package team.xinyuan519.wxsearch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.sun.javafx.binding.MapExpressionHelper.SimpleChange;

public class SimpleInfoCrawler {
	private String keyWords;
	private String dbIP; // remote host IP
	private int dbPort; // remove host port
	private final String charset = "utf-8";
	private final String dbNameSuffix = "_temp";
	final String urlHeader = "http://weixin.sogou.com/gzh?openid=";

	public SimpleInfoCrawler(String keyWords, String dbIP, int dbPort) {
		super();
		this.keyWords = keyWords;
		this.dbIP = dbIP;
		this.dbPort = dbPort;
	}

	private AccountInfo[] getInfo(String keyWords) {
		final int MAX_PAGE_NUM = 1;
		AccountInfo[] infos = new AccountInfo[10 * MAX_PAGE_NUM];
		int infosCount = 0;
		String UTF8keyWords = null;
		try {
			UTF8keyWords = URLEncoder.encode(keyWords, "utf-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
		}
		for (int pageNum = 1; pageNum <= MAX_PAGE_NUM; pageNum++) {
			try {
				String WXAccountUrlHeader = String
						.format("http://weixin.sogou.com/weixin?query=%s&type=1&page=%d&ie=utf8",
								UTF8keyWords, pageNum);
				System.out.println(WXAccountUrlHeader);
				URL url = new URL(WXAccountUrlHeader);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(30000);
				conn.setRequestProperty(
						"User-agent",
						"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.0b9pre) Gecko/20101228 Firefox/4.0b9pre");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				conn.connect();
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new Exception("HTTP Response is not HTTP_OK");
				}
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), "utf-8"));
				String html = "";
				String line = "";
				while ((line = reader.readLine()) != null) {
					html += line + "\n";
				}
				System.out.println(html);
				conn.disconnect();
				Parser parser = Parser.createParser(html, "utf-8");
				NodeFilter filter = new HasAttributeFilter("class",
						"wx-rb bg-blue wx-rb_v1 _item");
				NodeList nodeList = parser.extractAllNodesThatMatch(filter);
				Pattern pOpenid = Pattern
						.compile("(?<=href=\"/gzh\\?openid=)(.*?)(?=\")");
				Pattern pIdentity = Pattern
						.compile("(?<=<span>微信号：)(.*?)(?=</span>)");
				for (int i = 0; i < nodeList.size(); i++) {
					Node n = nodeList.elementAt(i);
					String tmpStr = n.toHtml();
					String openid = null;
					String identity = null;
					Matcher mOpenid = pOpenid.matcher(tmpStr);
					Matcher mIdentity = pIdentity.matcher(tmpStr);
					if (mOpenid.find()) {
						openid = mOpenid.group();
					}
					if (mIdentity.find()) {
						identity = mIdentity.group();
					}
					if (openid == null || identity == null) {
						System.err.println("missed an openid or identity");
						continue;
					} else {
						infos[infosCount++] = new AccountInfo(openid, identity);
					}
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return null;
			}
		}
		return infos;
	}

	private String infos2json(AccountInfo[] ai) {
		String json = null;
		JSONArray array = new JSONArray();
		for (int i = 0; i < ai.length; i++) {
			if (ai[i] != null) {
				JSONObject object = new JSONObject();
				try {
					object.put("OpenID", ai[i].getOpenid());
					object.put("Identity", ai[i].getIdentity());
					array.put(object);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		json = array.toString();
		return json;
	}

	// public String getJsonInfo(String keyWords) {
	// String result = null;
	// result = infos2json(getInfo(keyWords));
	// return result;
	// }

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
			title = list.elementAt(0).toPlainTextString();
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
			content = content.replaceAll("&quot;", "\"");
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

	private String getUrl(String url) {
		return url;
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

	public String crawlSimpleInfo() {

		AccountInfo[] accountInfos = getInfo(this.keyWords);
		int steps = accountInfos.length;
		for (int step = 0; step < steps; step++) {
			ProfileInfo profileInfo = new ProfileInfo(
					accountInfos[step].getIdentity(),
					accountInfos[step].getOpenid());
			Queue<String> links = profileInfo.getLinkListByPhantomJS();
			int length = links.size();
			DbItem[] items = new DbItem[length];
			ExecutorService pool = Executors.newFixedThreadPool(10);
			for (int i = 0; i < length; i++) {
				String url = links.poll();
				CrawlThread ct = new CrawlThread(url, items, i);
				pool.execute(ct);
			}
			pool.shutdown();
			try {
				pool.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				;
			}

			MongoClient client = new MongoClient(dbIP, dbPort);
			for (int i = 0; i < length; i++) {
				DB historyDB = client
						.getDB("WeiXinHistory" + this.dbNameSuffix);// 历史数据库
				DB freshDB = client.getDB("WeiXinFresh" + this.dbNameSuffix); // 最新数据库
				DBCollection historyColl = historyDB.getCollection(profileInfo
						.getIdentity());
				DBCollection freshColl = freshDB.getCollection(profileInfo
						.getIdentity());

				BasicDBObject article = new BasicDBObject();
				article.append("Title", items[i].getTitle());
				article.append("Content", items[i].getContent());
				article.append("Date", items[i].getDate());
				article.append("ReadNum", items[i].getReadNum());
				article.append("LikeNum", items[i].getLikeNum());
				article.append("Url", items[i].getUrl());
				article.append("MD5", items[i].getMD5());
				article.append("Time", items[i].getTime());
				article.append("Milliseconds", items[i].getMilliseconds());
				historyColl.insert(article);

				BasicDBObject query = new BasicDBObject("MD5",
						items[i].getMD5());
				DBCursor cursor = freshColl.find(query);
				if (cursor.hasNext()) {
					BasicDBObject updateValue = new BasicDBObject();
					updateValue.append("Title", items[i].getTitle());
					updateValue.append("Content", items[i].getContent());
					updateValue.append("Date", items[i].getDate());
					updateValue.append("ReadNum", items[i].getReadNum());
					updateValue.append("LikeNum", items[i].getLikeNum());
					updateValue.append("Url", items[i].getUrl());
					updateValue.append("MD5", items[i].getMD5());
					updateValue.append("Time", items[i].getTime());
					updateValue.append("Milliseconds",
							items[i].getMilliseconds());
					BasicDBObject updateSetValue = new BasicDBObject("$set",
							updateValue);
					freshColl.update(query, updateSetValue);
				} else {
					freshColl.insert(article);
				}
				DB db = client.getDB("AccountInfo" + this.dbNameSuffix);
				DBCollection infoColl = db.getCollection("accountInfo");
				BasicDBObject query2 = new BasicDBObject("OpenID",
						profileInfo.getOpenid());
				DBCursor cursor2 = infoColl.find(query2);
				if (cursor2.hasNext()) {
					;
				} else {
					BasicDBObject item = new BasicDBObject();
					item.append("Name", profileInfo.getName())
							.append("Identity", profileInfo.getIdentity())
							.append("Info", profileInfo.getInfo())
							.append("OpenID", profileInfo.getOpenid())
							.append("WebUrl", profileInfo.getWebURL());
					infoColl.insert(item);
				}
			}
			client.close();
		}
		return infos2json(accountInfos);
	}

	private class CrawlThread implements Runnable {
		private String url;
		private DbItem[] items;
		private int index;

		public CrawlThread(String url, DbItem[] items, int index) {
			this.url = url;
			this.items = items;
			this.index = index;
		}

		@Override
		public void run() {
			String htmlCode = getHtmlCode(url);
			Parser parser = Parser.createParser(htmlCode, charset);
			String content = getContent(parser);
			DbItem item = new DbItem(getTitle(parser), content,
					getDate(htmlCode), null, null, url, getMD5(content),
					getTime(), getMilliseconds());
			items[index] = item;
		}

	}

	public static void main(String[] args) {
		SimpleInfoCrawler sic = new SimpleInfoCrawler("苏菲", "localhost", 4399);
		AccountInfo[] infos = sic.getInfo("苏菲");
		infos[0].getIdentity();
		// sic.crawlSimpleInfo();
	}
}
