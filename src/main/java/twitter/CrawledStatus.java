package twitter;

public class CrawledStatus {
	private Long userid;
	private String statusjson;
	
	CrawledStatus(Long userid, String statusjson){
		this.userid = userid;
		this.statusjson = statusjson;
	}
	
	public String toString(){
		return userid +","+statusjson;
	}
	
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public String getStatusjson() {
		return statusjson;
	}
	public void setStatusjson(String statusjson) {
		this.statusjson = statusjson;
	}
	
}
