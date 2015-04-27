package team.xinyuan519.VSearch.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class KeyWordsHandler {
	private static final int MAX_PAGE_NUM = 1;
	private static final int semaphoreSize = 5;
	private static final Semaphore semaphore = new Semaphore(semaphoreSize);

	private String keyWords;

	public KeyWordsHandler(String keyWords) {
		this.keyWords = keyWords;
	}

	@SuppressWarnings("unused")
	private String getHtmlCodeByHttp(String targetUrl) {
		try {
			URL url = new URL(targetUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(30000);
			// conn.setRequestProperty(
			// "User-agent",
			// "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.0b9pre) Gecko/20101228 Firefox/4.0b9pre");
			// conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.connect();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new Exception("HTTP Response is not HTTP_OK");
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "utf-8"));
			String html = "";
			String line = "";
			while ((line = reader.readLine()) != null) {
				html += line + "\n";
			}
			conn.disconnect();
			System.out.println("html:" + html);
			return html;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getHTMlCodeByPhantomJS(String targetUrl) {
		WebDriver driver = null;
		String HTMLCode = null;
		PhantomJSDriverService.Builder builder = new PhantomJSDriverService.Builder();
		PhantomJSDriverService service = builder.usingPhantomJSExecutable(
				new File(EnvironmentInfo.PhantomJSExecutablePath)).build();
		DesiredCapabilities cap = DesiredCapabilities.phantomjs();
		cap.setCapability("phantomjs.page.settings.resourceTimeout", 20 * 1000);
		driver = new PhantomJSDriver(service, cap);

		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
		driver.manage().timeouts().setScriptTimeout(10, TimeUnit.MINUTES);

		int getCount = 5;
		while (getCount-- > 0) {
			try {
				driver.get(targetUrl);
				// Thread.sleep(5000);
				HTMLCode = driver.getPageSource();
			} catch (Exception e) {
				driver.quit();
				driver = new PhantomJSDriver(service, cap);
			}
			if (HTMLCode != null) {
				driver.quit();
				break;
			}
		}
		// System.out.println(HTMLCode);
		return HTMLCode;
	}

	private AccountInfo[] getAccountInfo(String keyWords) {
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

				String html = getHTMlCodeByPhantomJS(WXAccountUrlHeader);
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

	@SuppressWarnings("static-access")
	public String handleKeyWords() {
		long start = Calendar.getInstance().getTimeInMillis();
		AccountInfo[] accountInfos = this.getAccountInfo(this.keyWords);
		AccountInfoExectutor executor = new AccountInfoExectutor(accountInfos,
				this.semaphore);
		executor.execute();
		String jsonInfo = this.infos2json(accountInfos);
		long timeout = Calendar.getInstance().getTimeInMillis() - start;
		System.out.println("KeyWords:"+keyWords+"\t timeout:"+timeout);
		return String.valueOf(jsonInfo);
	}

	public static void main(String[] args) {
		// long start = Calendar.getInstance().getTimeInMillis();
		// KeyWordsHandler handler = new KeyWordsHandler("苏菲");
		// handler.handleKeyWords();
		// System.out.println(Calendar.getInstance().getTimeInMillis() - start);
		// System.out.println(handler.infos2json(handler.getAccountInfo("苏菲")));
		try {
			for (int i = 0; i < 100; i++) {
				URL url = new URL(
						"http://localhost:8080/?keyWords=%12%23&list={\"a\":\"b\"}");
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), "utf-8"));
				String line = reader.readLine();
				reader.close();
				System.out.println(line);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
