package team.xinyuan519.VSearch.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class HotTopic {
	private final String baidu = "http://top.baidu.com/?fr=tph_right";
	private final String weixin = "http://weixin.sogou.com/";

	public Queue<String> getHotTopics() {
		WebDriver driver = null;
		PhantomJSDriverService.Builder builder = new PhantomJSDriverService.Builder();
		PhantomJSDriverService service = builder.usingPhantomJSExecutable(
				new File(EnvironmentInfo.PhantomJSExecutablePath)).build();
		DesiredCapabilities cap = DesiredCapabilities.phantomjs();
		cap.setCapability("phantomjs.page.settings.resourceTimeout", 20 * 1000);
		driver = new PhantomJSDriver(service, cap);

		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
		driver.manage().timeouts().setScriptTimeout(10, TimeUnit.MINUTES);

		driver.get(baidu);
		List<WebElement> list = driver.findElements(By.className("list-title"));
		Iterator<WebElement> iterator = list.iterator();
		Queue<String> keyWords = new LinkedList<String>();
		while (iterator.hasNext()) {
			WebElement element = iterator.next();
			if (!element.getText().equals("") && !element.getText().equals(" ")) {
				keyWords.offer(element.getText());
			}
		}
		driver.get(weixin);
		WebElement topwords1 = driver.findElement(By.id("topwords_1"));
		String[] strs1 = topwords1.getText().split("\n");
		for (int i = 0; i < strs1.length; i++) {
			keyWords.offer(strs1[i].substring(1, strs1[i].length()));
		}
		WebElement topwords2 = driver.findElement(By.id("topwords_2"));
		String[] strs2 = topwords2.getText().split("\n");
		for (int i = 0; i < strs2.length; i++) {
			keyWords.offer(strs2[i].substring(1, strs2[i].length()));
		}
		driver.quit();
		return keyWords;
	}

	public void handleHotTopics(Queue<String> hotTopics) {
		WebDriver driver = null;
		PhantomJSDriverService.Builder builder = new PhantomJSDriverService.Builder();
		PhantomJSDriverService service = builder.usingPhantomJSExecutable(
				new File(EnvironmentInfo.PhantomJSExecutablePath)).build();
		DesiredCapabilities cap = DesiredCapabilities.phantomjs();
		cap.setCapability("phantomjs.page.settings.resourceTimeout", 20 * 1000);
		driver = new PhantomJSDriver(service, cap);

		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
		driver.manage().timeouts().setScriptTimeout(10, TimeUnit.MINUTES);

		Iterator<String> iterator = hotTopics.iterator();
		ExecutorService pool = Executors.newFixedThreadPool(3);
		while (iterator.hasNext()) {
			String keyWord = iterator.next();
			String utf8KeyWord = null;
			try {
				utf8KeyWord = URLEncoder.encode(keyWord, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (utf8KeyWord != null) {
				String url = String
						.format("http://weixin.sogou.com/weixin?type=2&ie=utf-8&query=%s",
								utf8KeyWord);
				driver.get(url);
				List<WebElement> elements = driver.findElements(By
						.id("weixin_account"));
				Iterator<WebElement> iter = elements.iterator();
				while (iter.hasNext()) {
					WebElement ele = iter.next();
					String openID = ele.getAttribute("i");
					ProfileInfo profileInfo = new ProfileInfo(null, openID);
					pool.execute(new RefreshThread(profileInfo));
				}
			}
			pool.shutdown();
			while (!pool.isTerminated()) {
				try {
					pool.awaitTermination(5, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		driver.quit();
	}

	public void scheduledExtendAccoutnInfo() {
		while (true) {
			this.handleHotTopics(this.getHotTopics());
			try {
				Thread.sleep(6 * 3600 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		HotTopic hotTopic = new HotTopic();
		hotTopic.scheduledExtendAccoutnInfo();
	}
}
