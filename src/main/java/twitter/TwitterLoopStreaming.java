package twitter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

import java.net.UnknownHostException;
import java.util.Scanner;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterLoopStreaming {

	private ConfigurationBuilder cb;
	private DB db;
	private DBCollection items;

	/**
	 * static block used to construct a connection with tweeter with twitter4j
	 * configuration with provided settings. This configuration builder will be
	 * used for next search action to fetch the tweets from twitter.com.
	 */

	public static void main(String[] args) throws InterruptedException {

		TwitterLoopStreaming stream = new TwitterLoopStreaming();
		stream.loadMenu();

	}

	public void loadMenu() throws InterruptedException {

		System.out.print("Please choose a keyword for your stream:\t");

		Scanner input = new Scanner(System.in);
		String keyword = input.nextLine();
		input.close();

		connectdb(keyword);

		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("Bb9Oa7iJbIss2sYIDNCddA");
		cb.setOAuthConsumerSecret("aVaUyq344TcFE6XHPAdHKo53xfaO5Ih0tdgt15TfIp8");
		cb.setOAuthAccessToken("138994178-x8h43R6ooiw0nTNWqH49Etl08F1UQbzz5Uc4AoBL");
		cb.setOAuthAccessTokenSecret("Ey1vrYiwxvjdN76iwWQkpUDd0CAJNNw7e6XrAGgOB9tr8");

		TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
				.getInstance();
		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {
				System.out.println("@" + status.getUser().getScreenName()
						+ " - " + status.getText());

				BasicDBObject basicObj = new BasicDBObject();
				basicObj.put("user_name", status.getUser().getScreenName());
				basicObj.put("retweet_count", status.getRetweetCount());
				basicObj.put("tweet_followers_count", status.getUser()
						.getFollowersCount());
				basicObj.put("source", status.getSource());
				// basicObj.put("coordinates",tweet.getGeoLocation());

				UserMentionEntity[] mentioned = status.getUserMentionEntities();
				basicObj.put("tweet_mentioned_count", mentioned.length);
				basicObj.put("tweet_ID", status.getId());
				basicObj.put("tweet_text", status.getText());

				try {
					items.insert(basicObj);
				} catch (Exception e) {
					System.out.println("MongoDB Connection Error : "
							+ e.getMessage());

				}

			}

			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:"
						+ statusDeletionNotice.getStatusId());
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:"
						+ numberOfLimitedStatuses);
			}

			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId
						+ " upToStatusId:" + upToStatusId);
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub

			}
		};

		FilterQuery fq = new FilterQuery();
		String keywords[] = { "MachineLearning" };

		fq.track(keywords);

		twitterStream.addListener(listener);
		twitterStream.filter(fq);

	}

	public void connectdb(String keyword) {
		try {
			// on constructor load initialize MongoDB and load collection
			initMongoDB();
			items = db.getCollection(keyword);

			// make the tweet_ID unique in the database
			BasicDBObject index = new BasicDBObject("tweet_ID", 1);
			items.ensureIndex(index, new BasicDBObject("unique", true));

		} catch (MongoException ex) {
			System.out.println("MongoException :" + ex.getMessage());
		}

	}

	/**
	 * initMongoDB been called in constructor so every object creation this
	 * initialize MongoDB.
	 */
	public void initMongoDB() throws MongoException {
		try {
			System.out.println("Connecting to Mongo DB..");
			MongoClient mongoClient = new MongoClient( "127.0.0.1" , 27017 );
			db = mongoClient.getDB("Twitter");
		} catch (UnknownHostException ex) {
			System.out.println("MongoDB Connection Error :" + ex.getMessage());
		}
	}

}