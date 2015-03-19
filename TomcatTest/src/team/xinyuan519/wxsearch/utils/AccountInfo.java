package team.xinyuan519.wxsearch.utils;
public class AccountInfo {
	private String openid;
	private String identity;

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public AccountInfo(String openid, String identity) {
		super();
		this.openid = openid;
		this.identity = identity;
	}

}
