package team.xinyuan519.VSearch.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ProfileInfo {
	private String name; // 微信名
	private String identity; // 微信号
	private String openid; // openid
	private String info;
	private String WebURL; // 微信搜索公众号首页地址
	private Queue<String> linkList;
	private final String charset = "utf-8";

	public String getName() {
		return name;
	}

	public String getIdentity() {
		return identity;
	}

	public String getOpenid() {
		return openid;
	}

	public String getInfo() {
		return info;
	}

	public String getWebURL() {
		return WebURL;
	}
	
	public Queue<String> getLinkList(){
		return linkList;
	}
	// private final String encode = "utf-8";

	public ProfileInfo(String identity, String openid) {
		this.identity = identity;
		this.openid = openid;
		this.WebURL = "http://weixin.sogou.com/gzh?openid=" + openid;
	}

	// recommended method here,faster than httpunit
	public String getHTMlCodeByPhantomJS() {
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
				driver.get(this.WebURL);
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
		return HTMLCode;
	}

	// getHTMLCodeByHttp can't proceed JavaScript,therefore it's not capable to
	// get link list here.However it can get raw html code instead.
	public String getHTMLCodeByHttp() {
		String HTMLCode = "";
		int count = 5;
		while (count-- > 0) {
			try {
				URL url = new URL(this.WebURL);
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

	// not recommended method here,it's slower and not full support of
	// JavaScript and CSS
	public String getHTMLCodeByHttpunit() {
		String HTMLCode = null;
		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		HtmlPage page;
		try {
			page = client.getPage(this.WebURL);
			HTMLCode = page.asXml();
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}
		client.closeAllWindows();
		return HTMLCode;
	}

	public Queue<String> getLinkListByPhantomJS() {
		String HTMLCode = getHTMlCodeByPhantomJS();
		if (HTMLCode == null) {
			return null;
		}

		Queue<String> queue = new LinkedList<String>();
		Pattern pattern = Pattern
				.compile("(?<=href=\")http://mp\\.weixin\\.qq\\.com/(.*?)(?=\")");
		Matcher matcher = pattern.matcher(HTMLCode);
		while (matcher.find()) {
			String str = matcher.group().replace("&amp;", "&");
			if (!queue.contains(str)) {
				queue.offer(str);
			}
		}

		String name = "notfound";
		String info = "notfound";
		if (HTMLCode.contains("<h3 id=\"weixinname\">")) {

			name = HTMLCode.substring(
					HTMLCode.indexOf("<h3 id=\"weixinname\">")
							+ "<h3 id=\"weixinname\">".length(),
					HTMLCode.indexOf("</h3>",
							HTMLCode.indexOf("<h3 id=\"weixinname\">")));
			// System.out.println(name);
		}
		if (HTMLCode.contains("<span class=\"sp-txt\">")) {

			info = HTMLCode.substring(
					HTMLCode.indexOf("<span class=\"sp-txt\">")
							+ "<span class=\"sp-txt\">".length(),
					HTMLCode.indexOf("</span>",
							HTMLCode.indexOf("<span class=\"sp-txt\">")));
			// System.out.println(info);
		}

		this.name = name;
		this.info = info;
		this.linkList = queue;
		return queue;

	}

	public Queue<String> getLinkListByHttpunit() {
		String HTMLCode = getHTMLCodeByHttpunit();
		if (HTMLCode == null) {
			return null;
		}

		Queue<String> queue = new LinkedList<String>();
		Pattern pattern = Pattern
				.compile("(?<=href=\")http://mp\\.weixin\\.qq\\.com/(.*?)(?=\")");
		Matcher matcher = pattern.matcher(HTMLCode);
		while (matcher.find()) {
			String str = matcher.group().replace("&amp;", "&");
			if (!queue.contains(str)) {
				queue.offer(str);
			}
		}

		String name = "notfound";
		String info = "notfound";
		if (HTMLCode.contains("<h3 id=\"weixinname\">")) {
			name = HTMLCode.substring(
					HTMLCode.indexOf("<h3 id=\"weixinname\">")
							+ "<h3 id=\"weixinname\">".length(),
					HTMLCode.indexOf("</h3>",
							HTMLCode.indexOf("<h3 id=\"weixinname\">"))).trim();
			// System.out.println(name);
		}
		if (HTMLCode.contains("<span class=\"sp-txt\">")) {
			info = HTMLCode.substring(
					HTMLCode.indexOf("<span class=\"sp-txt\">")
							+ "<span class=\"sp-txt\">".length(),
					HTMLCode.indexOf("</span>",
							HTMLCode.indexOf("<span class=\"sp-txt\">")))
					.trim();
			// System.out.println(info);
		}

		this.name = name;
		this.info = info;
		this.linkList = queue;
		
		return queue;

	}
	
	public void init(){
		this.getLinkListByPhantomJS();
	}

	public static void main(String[] args) {

		ProfileInfo pl = new ProfileInfo(null, "oIWsFt8RjvhNflqVGzpWyjV9dzGg");
		// Queue<String> qs = pl.getLinkListByHttpunit();
		// while (!qs.isEmpty()) {
		// System.out.println("Url:" + qs.poll());
		// }
		pl.init();
		Queue<String> list = pl.getLinkList();
		while(!list.isEmpty()){
			String str = list.poll();
			System.out.println(str);
		}
		
//		System.out.println(pl.getHTMLCodeByHttp());
//		long start = Calendar.getInstance().getTimeInMillis();
//		pl.getHTMlCodeByPhantomJS();
//		System.out.println(Calendar.getInstance().getTimeInMillis() - start);
//		start = Calendar.getInstance().getTimeInMillis();
//		pl.getLinkListByHttpunit();
//		System.out.println(Calendar.getInstance().getTimeInMillis() - start);
	}
}