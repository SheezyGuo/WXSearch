package team.xinyuan519.wxsearch.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ProfileInfos {
	private String name; // 微信名
	private String identity; // 微信号
	private String openid; // openid
	private String info;
	private String WebURL; // 微信搜索公众号首页地址

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

	// private final String encode = "utf-8";

//	private final String PhantomJSExecutablePath = "D:\\PhantomJS\\phantomjs-2.0.0-windows\\bin\\phantomjs.exe";
	private final String PhantomJSExecutablePath ="/home/dtlvhyy/APPS/Phanjomjs/phantomjs/bin/phantomjs";

	public ProfileInfos(String identity, String openid) {
		this.identity = identity;
		this.openid = openid;
		this.WebURL = "http://weixin.sogou.com/gzh?openid=" + openid;
	}

	public Queue<String> getLinkList() {
		WebDriver driver = null;
		try {
			PhantomJSDriverService.Builder builder = new PhantomJSDriverService.Builder();
			PhantomJSDriverService service = builder.usingPhantomJSExecutable(
					new File(PhantomJSExecutablePath)).build();
			DesiredCapabilities cap = DesiredCapabilities.phantomjs();
			cap.setCapability("phantomjs.page.settings.resourceTimeout",
					20 * 1000);
			driver = new PhantomJSDriver(service, cap);

			driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
			driver.manage().timeouts().setScriptTimeout(10, TimeUnit.MINUTES);
			String HTMLCode = null;
			int getCount = 5;
			while (getCount-- > 0) {
				try {
					driver.get(this.WebURL);
					Thread.sleep(5000);
					HTMLCode = driver.getPageSource();
					// System.out.println(HTMLCode);
				} catch (Exception e) {
					driver.quit();
					Thread.sleep(5 * 1000);
					driver = new PhantomJSDriver(service, cap);
				}
				if (HTMLCode != null) {
					break;
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			driver.quit();
			Thread.sleep(5 * 1000);
			if (HTMLCode == null) {
				return null;
			}
			// System.out.println(HTMLCode);

			Queue<String> queue = new LinkedList<String>();
			Pattern pattern = Pattern
					.compile("(?<=href=\")http://mp\\.weixin\\.qq\\.com/(.*?)(?=\")");
			Matcher matcher = pattern.matcher(HTMLCode);
			while (matcher.find()) {
				String str = matcher.group().replace("&amp;", "&");
				// System.out.println(str);
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

			return queue;
		} catch (Exception e) {
			if (driver != null) {
				driver.quit();
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			System.out.println("Nani-.-...........What happend?===>"
					+ e.getMessage());
			return null;
		}
	}

	public static void main(String[] args) {

		ProfileInfos pl = new ProfileInfos(null, "oIWsFt2Xdvlz_w4LRhTdpck5rOYQ");
		Queue<String> qs = pl.getLinkList();
		while (!qs.isEmpty()) {
			System.out.println("Url:" + qs.poll());
		}
	}
}