package twitter;

public class CrawledUser {
	private String userid;
	private String userJson;

	public CrawledUser(String userid, String userJson) {
		this.userid = userid;
		this.userJson = userJson;
	}

	public String toString() {
		return userid + "," + userJson;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUserJson() {
		return userJson;
	}

	public void setUserJson(String userJson) {
		this.userJson = userJson;
	}

}
