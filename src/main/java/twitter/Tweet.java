package twitter;

public class Tweet {
	private String keyword;
	private String username;
	private Long tweetId;
	private String tweetText;
	private Integer reTweetCount;
	private String source;
	private Integer tweetMentionedCount;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getTweetId() {
		return tweetId;
	}

	public void setTweetId(Long tweetId) {
		this.tweetId = tweetId;
	}

	public String getTweetText() {
		return tweetText;
	}

	public void setTweetText(String tweetText) {
		this.tweetText = cleanText(tweetText);
	}

	public Integer getReTweetCount() {
		return reTweetCount;
	}

	public void setReTweetCount(Integer reTweetCount) {
		this.reTweetCount = reTweetCount;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = cleanText(source);
	}

	public Integer getTweetMentionedCount() {
		return tweetMentionedCount;
	}

	public void setTweetMentionedCount(Integer tweetMentionedCount) {
		this.tweetMentionedCount = tweetMentionedCount;
	}

	public static String cleanText(String text) {
		text = text.replace("\n", "\\n");
		text = text.replace("\t", "\\t");
		text = text.replace("\"", "'");
		return text;
	}

	public String toString() {
		String tweetString;
		tweetString = "\"" + getKeyword() + "\",\"" + getTweetId() + "\",\""
				+ getUsername() + "\",\"" + getTweetText() + "\",\""
				+ getReTweetCount() + "\",\"" + getSource() + "\",\""
				+ getTweetMentionedCount() + "\"";
		return tweetString;
	}

}