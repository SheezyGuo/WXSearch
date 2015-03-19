package team.xinyuan519.wxsearch.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JustInTimeCrawler {

	public AccountInfo[] getDataByAccount(String keyWords) {
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

	public String infos2json(AccountInfo[] ai) throws JSONException {
		String json = null;
		JSONArray array = new JSONArray();
		for (int i = 0; i < ai.length; i++) {
			if (ai[i] != null) {
				JSONObject object = new JSONObject();
				object.put("OpenID", ai[i].getOpenid());
				object.put("Identity", ai[i].getIdentity());
				array.put(object);
			} else {
				break;
			}
		}
		json = array.toString();
		return json;
	}
	
	public String getJsonInfo(String keyWords){
		String result = null;
		try {
			result = infos2json(getDataByAccount(keyWords));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		JustInTimeCrawler j = new JustInTimeCrawler();
		AccountInfo[] ai = j.getDataByAccount("成都火锅");
//		System.out.println("Size:" + String.valueOf(ai.length));
//		for (int i = 0; i < ai.length; i++) {
//			if (ai[i] != null) {
//				System.out.println(ai[i].getOpenid() + "    "
//						+ ai[i].getIdentity());
//			} else {
//				break;
//			}
//		}
		try {
			System.out.println(j.infos2json(ai));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
