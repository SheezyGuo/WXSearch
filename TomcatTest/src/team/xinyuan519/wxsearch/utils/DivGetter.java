package team.xinyuan519.wxsearch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DivGetter {
	private String jsonArrayStr;

	public DivGetter(String keyWords) {
		this.jsonArrayStr = this.getHTMLCodeByHttp(String.format(
				"http://%s:%d/?keyWords=%s",
				EnvironmentInfo.pythonServerIP,
				EnvironmentInfo.pythonServerPort, keyWords));
	}

	private String calStars(int stars) {
		String starsSpan = "";
		for (int lStars = stars; lStars > 0; lStars -= 2) {
			if (lStars >= 2) {
				starsSpan += "<span class=\"glyphicon glyphicon-star\"></span>";
			}
			if (lStars == 1) {
				starsSpan += "<span class=\"glyphicon glyphicon-star-empty\"></span>";
			}
		}
		return starsSpan;
	}

	public String getContent() {
		String html = "";
		try {
			JSONArray array = new JSONArray(this.jsonArrayStr);
			int length = array.length();
			for (int i = 0; i < length; i++) {
				JSONObject item = array.getJSONObject(i);
				String listItem = String
						.format("<div name=\"list-item\">"
								+ "<p><font name=\"name\"><a href=\"%s\">%s</a></font><font name=\"identity\">微信号:%s</font></p>"
								+ "<p><font name=\"info\">%s</font></p>"
								+ "<div name=\"stars\">"
								+ "<font class=\"text-muted\" name=\"starsFont\">相关度:</font>"
								+ calStars(item.getInt("Stars")) + "</div>"
								+ "</div>", item.getString("Url"),
								item.getString("Name"),
								item.getString("Identity"),
								item.getString("Info"));
				html += listItem;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return html;
	}

	public String getHTMLCodeByHttp(String Url) {
		String HTMLCode = "";
		int count = 5;
		while (count-- > 0) {
			try {
				URL url = new URL(Url);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(10 * 1000);
				conn.setReadTimeout(10 * 1000);
				conn.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E; rv:11.0) like Gecko");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), "utf-8"));
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
//			System.out.println(HTMLCode);
			return HTMLCode;
		}
	}
}
