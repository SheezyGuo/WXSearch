package team.xinyuan519.wxsearch.utils;

public class DbItem {
	private String title;
	private String content;
	private String date;
	private String readNum;
	private String likeNum;
	private String url;
	private String MD5;
	private String time;
	private String milliseconds;

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getDate() {
		return date;
	}

	public String getReadNum() {
		return readNum;
	}

	public String getLikeNum() {
		return likeNum;
	}

	public String getUrl() {
		return url;
	}

	public String getMD5() {
		return MD5;
	}

	public String getTime() {
		return time;
	}

	public String getMilliseconds() {
		return milliseconds;
	}

	public DbItem(String title, String content, String date, String readNum,
			String likeNum, String url, String mD5, String time,
			String milliseconds) {
		super();
		this.title = title;
		this.content = content;
		this.date = date;
		this.readNum = readNum;
		this.likeNum = likeNum;
		this.url = url;
		MD5 = mD5;
		this.time = time;
		this.milliseconds = milliseconds;
	}

}
