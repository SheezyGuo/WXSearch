package team.xinyuan519.wxsearch.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JustInTimeCrawler {

	private AccountInfo[] getDataByAccount(String keyWords) {
		final int MAX_PAGE_NUM = 1;
		AccountInfo[] infos = new AccountInfo[10 * MAX_PAGE_NUM];
		int infosCount = 0;
		String UTF8keyWords = null;
		try {
			UTF8keyWords = URLEncoder.encode(keyWords, "utf-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
		}
		for (int pageNum = 0; pageNum < MAX_PAGE_NUM; pageNum++) {
			try {
				String WXAccountUrlHeader = String
						.format("http://weixin.sogou.com/weixin?type=1&query=%s&ie=utf8page=%d",
								UTF8keyWords, pageNum);
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

	public String getJsonInfo(String keyWords) {
		String result = null;
		result = infos2json(getDataByAccount(keyWords));
		return result;
	}

	public void crawl(String jsonList) {
		try {
			JSONArray array = new JSONArray(jsonList);
			WeiXinThread[] WXThreads = new WeiXinThread[array.length()];
			for (int i = 0; i < array.length(); i++) {
				JSONObject o = array.getJSONObject(i);
				// System.out.println("Identity:"+o.getString("Identity")+"\tOpenID:"+o.getString("OpenID"));
				WXThreads[i] = new WeiXinThread(o.getString("Identity"),
						o.getString("OpenID"));
			}
			ExecutorService pool = Executors.newFixedThreadPool(10);
			for (int i = 0; i < WXThreads.length; i++) {
				WXThreads[i].setDbNameSuffix("_temp");
				pool.execute(WXThreads[i]);
			}
			pool.shutdown();
		} catch (JSONException e) {
			System.out.println("Failed to get JSONArray");
		}

	}
	
	public void crawlTempData(AccountInfo[] infos){
		
	}

	public static void main(String[] args) {
		JustInTimeCrawler j = new JustInTimeCrawler();
		AccountInfo[] ai = j.getDataByAccount("成都火锅");
		System.out.println(j.infos2json(ai));
	}
}
