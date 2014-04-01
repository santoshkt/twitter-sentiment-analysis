package twitter;

import twitter4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchTweet {

	public static void main(String[] args) throws InterruptedException,
			TwitterException, IOException {

		Twitter twitter = CommonUtils.getTwitterInstance();
		long oldestTwitterID = -1;
		int requestCount = 0;
		String keyword = TwitterAPISettings.INPUT_KEYWORD;

		while (requestCount < TwitterAPISettings.MAX_QUERIES) {

			try {

				// Make sure you are not over limit.
				Map<String, RateLimitStatus> rateLimitStatus = twitter
						.getRateLimitStatus("application");
				RateLimitStatus searchTweetsRateLimit = rateLimitStatus
						.get("/application/rate_limit_status");

				if (searchTweetsRateLimit.getRemaining()  < 20) {
					System.out.println("Sleeping for " + (searchTweetsRateLimit.getSecondsUntilReset() + 2) * 1000 + "ms");
					Thread.sleep((searchTweetsRateLimit.getSecondsUntilReset() + 2) * 1000);
				}

				// Do the query.
				Query query = new Query(keyword);
				query.setCount(100);
				if (oldestTwitterID != -1) {
					query.setMaxId(oldestTwitterID - 1);
				}
				QueryResult result;

				ArrayList<Tweet> resultTweets = new ArrayList<Tweet>();

				System.out.println("Made " + requestCount
						+ " requests so far..");
				result = twitter.search(query);
				
				// Get results and store them in file.
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					Tweet resultTweet = new Tweet();
					resultTweet.setKeyword(keyword);
					resultTweet.setUsername(tweet.getUser().getScreenName());
					resultTweet.setReTweetCount(tweet.getRetweetCount());
					resultTweet.setSource(tweet.getSource());
					UserMentionEntity[] mentioned = tweet
							.getUserMentionEntities();
					resultTweet.setTweetMentionedCount(mentioned.length);
					resultTweet.setTweetId(tweet.getId());
					resultTweet.setTweetText(tweet.getText());

					resultTweets.add(resultTweet);

					if (oldestTwitterID == -1
							|| tweet.getId() < oldestTwitterID) {
						oldestTwitterID = tweet.getId();
					}
				}

				// Increment no. of times requests made.
				requestCount++;

				DataAccessUtils.writeToFile(resultTweets, keyword);
				resultTweets.clear();
				
			} catch (TwitterException te) {
				
				if(te.getMessage().contains("Rate limit exceeded")){
					te.printStackTrace();
					System.out.println("Sleeping for 90000ms");
					Thread.sleep(900000);
					continue;
				}
				
				te.printStackTrace();
				System.out.println("Failed to search tweets: "
						+ te.getMessage());
				System.exit(-1);
			}

		}

		System.exit(0);
	}
}