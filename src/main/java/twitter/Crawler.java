package twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class Crawler {

	public static Twitter twitter;

	private static Logger logger = LogManager.getFormatterLogger(Crawler.class
			.getName());

	public static void main(String args[]) throws InterruptedException,
			IOException {

		logger.entry();

		// This is a driver method for the software.
		// Handle all crawling of social graph up to 2 hops in the crawl
		// method.
		logger.trace("Started crawling..");
		try {
			TwitterSocialGraph.crawl();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		logger.trace("Finished crawling.");

		logger.exit();
	}
}

class TwitterSocialGraph {

	private static Logger logger = LogManager
			.getFormatterLogger(TwitterSocialGraph.class.getName());

	public static Twitter twitter;

	public static void crawl() throws InterruptedException, IOException,
			TwitterException {

		logger.entry();

		try {

			twitter = CommonUtils.getTwitterInstance();

			HashSet<Long> userIDList = new HashSet<Long>();

			// Look up for the initial user or subject.

			User user = twitter.showUser(TwitterAPISettings.INITIAL_USER_ID);
			if (user.getStatus() != null) {
				System.out.println("Subject exists: @" + user.getScreenName()
						+ " - " + user.getStatus().getText());
				userIDList.add(user.getId());
			} else {
				// the user is protected
				System.out.println("Subject is protected: @"
						+ user.getScreenName());
			}

			Long subject = new Long(0);
			subject = user.getId();
			logger.info("Successfully looked up.");

			// /////////////////////////////////////////////////////////////////
			// Extract first level users, and enqueue them.
			// This is called at first time for subject. So no loop.

			long cursor = -1;
			IDs followsFollowerIds;
			IDs followingIds;

			// This contains all reasonable users followed by subject.
			HashSet<String> followsHs = new HashSet<String>();
			HashSet<String> friendsHs = new HashSet<String>();

			twitter = CommonUtils.getTwitterInstance();
			// Every one subject is following
			logger.trace("Subject : " + subject);
			followingIds = twitter.getFriendsIDs(subject, cursor);
			logger.trace("Subject is following " + followingIds.getIDs().length);

			// Add him to a hash set - (user, users he follow)
			followsHs.add(subject + "," + followingIds.getIDs().toString());

			int rateLimitFollowers = 15;
			int rateLimitFriends = 14;
			// For each person subject is following
			for (Long followingId : followingIds.getIDs()) {
				logger.trace("Finding details about: " + followingId);
				cursor = -1;
				// Get that users that person is following
				if (rateLimitFollowers <= 0) {
					Thread.sleep(900000);
					rateLimitFollowers = 15;
					rateLimitFriends = 15;
				}
				try {
					followsFollowerIds = twitter.getFollowersIDs(followingId,
							cursor);
					rateLimitFollowers--;
				} catch (TwitterException te) {

					if (te.getMessage().contains("Rate limit exceeded")) {
						te.printStackTrace();
						System.out.println("Sleeping for 90000ms");
						Thread.sleep(900000);
						rateLimitFollowers = 15;
						rateLimitFriends = 15;
						continue;
					}

					te.printStackTrace();
					continue;
				}
				if (followsFollowerIds.getIDs().length > 300) {
					logger.trace("User ID: " + followingId
							+ ", No. of followers: "
							+ followsFollowerIds.getIDs().length);
					logger.info("This first level user is followed by too many people. Must be a celebrity. Not a suitable subject for this experiment.");
				} else {
					logger.trace("Valid first level user: " + followingId);
					// Got a valid first level user, check if he is a friend.
					HashSet<Long> followsFollowingHs = new HashSet<Long>();

					// Every one, first level user is following.
					IDs followsFollowingIds;

					if (rateLimitFriends <= 0) {
						Thread.sleep(900000);
						rateLimitFriends = 15;
						rateLimitFollowers = 15;
					}

					try {
						cursor = -1;
						followsFollowingIds = twitter.getFriendsIDs(
								followingId, cursor);
						rateLimitFriends--;

					} catch (TwitterException te) {

						if (te.getMessage().contains("Rate limit exceeded")) {
							te.printStackTrace();
							System.out.println("Sleeping for 90000ms");
							Thread.sleep(900000);
							rateLimitFriends = 15;
							rateLimitFollowers = 15;
							continue;
						}

						logger.trace("Cannot find people the first level user is following.");
						te.printStackTrace();
						continue;
					}

					logger.trace("Obtained people subject's probable friends are following.");
					for (Long followsFollowingId : followsFollowingIds.getIDs()) {
						followsFollowingHs.add(followsFollowingId);
					}

					if (followsFollowingHs.contains(followingId)) {
						// Subject and this person as bi-directional following
						// and hence
						// are friends with each other and are more related to
						// each other.
						friendsHs.add(subject + "," + followingId);
						followsHs.add(followingId + ","
								+ followsFollowingIds.toString());
					}
				}
			}

			// Now, we know who is our subject, people who is friends with
			// subject,
			// people who the subject is following. People the subject's friends
			// are
			// following. For all the users we know, we need to get the tweets.

			// For all the people in the followHs HashSet, break the HashSet,
			// get a
			// list of users and find the status tweets of all these users.
			for (String followsStr : followsHs) {
				String[] userids = followsStr.split(",");

				// For subject, don't add the people he is following into this
				// list,
				// because we don't want to know their tweets. This list is
				// primarily
				// for finding tweets of all these users.
				if (Long.parseLong(userids[0]) == subject) {
					userIDList.add(subject);
					continue;
				}

				for (String userid : userids) {
					userIDList.add(Long.parseLong(userid));
				}

			}

			// Collect tweets of all users in userIDList.
			ArrayList<CrawledStatus> statuses;
			statuses = getUserTweets(userIDList);
			logger.info("Printing all statuses");
			for (CrawledStatus status : statuses) {
				logger.trace(status.toString());
			}

			// For all users in userIDList, get their user objects.
			// Look up for the initial user or subject.

			// create strings of 100 usersIDs

			int querySize = 0;
			String query = new String();
			for (Long userid : userIDList) {
				logger.trace("Will query about user: " + userid);
				if (querySize <= 100) {
					query += "," + userid;
				} else {

					ResponseList<User> userObjs = twitter.lookupUsers(query
							.split(","));

					// Even though we use a loop, there will be only 1 user.
					for (User userObj : userObjs) {
						if (userObj.getStatus() != null) {
							logger.trace("User details: " + userObj.toString());
						} else {
							// the user is protected
							logger.trace("Protected user: @"
									+ userObj.getScreenName());
						}
					}
					query = "";
					querySize = 0;
				}

			}

			logger.trace("Printed all users. Now trying to find friends of 2nd level users.");

			// For all the 2nd level users whom we obtain from friendsHs, find
			// their
			// friends and add to friendsHs.

			HashSet<String> secondFriendsHs = new HashSet<String>();
			for (String friendStr : friendsHs) {
				String[] userids = friendStr.split(",");
				Long userid = Long.parseLong(userids[1]);

				// Every one 2nd level subject is following
				followingIds = twitter.getFriendsIDs(userid);
				// For each person subject is following
				for (Long followingId : followingIds.getIDs()) {

					// Get that users that person is following
					followsFollowerIds = twitter.getFollowersIDs(followingId,
							cursor);
					if (followsFollowerIds.getIDs().length > 1000) {
						logger.info("This first level user is followed by too many people. Must be a celebrity. Not a suitable subject for this experiment.");
					} else {

						// Got a valid first level user, check if he is a
						// friend.
						HashSet<Long> followsFollowingHs = new HashSet<Long>();

						// Every one, first level user is following.
						IDs followsFollowingIds = twitter
								.getFriendsIDs(followingId);
						for (Long followsFollowingId : followsFollowingIds
								.getIDs()) {
							followsFollowingHs.add(followsFollowingId);
						}

						if (followsFollowingHs.contains(followingId)) {
							// Subject and this person as bi-directional
							// following and hence
							// are friends with each other and are more related
							// to each other.
							secondFriendsHs.add(userid + "," + followingId);
						}
					}
				}
			}

			friendsHs.addAll(secondFriendsHs);

			logger.trace("Print all friends.");
			logger.trace(friendsHs.toString());

		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<CrawledStatus> getUserTweets(
			HashSet<Long> userIDList) throws TwitterException {
		logger.entry();
		List<Status> statuses;
		ArrayList<CrawledStatus> statusList = new ArrayList<CrawledStatus>();

		for (Long user : userIDList) {
			Paging paging = new Paging(1, 6);
			int page = 1;
			paging.setPage(page);
			paging.setCount(200);

			do {
				statuses = twitter.getUserTimeline(user, paging);
				System.out.println("Crawling @" + user + " user timeline.");
				for (Status status : statuses) {
					CrawledStatus cs = new CrawledStatus(status.getUser()
							.getId(), status.toString());
					statusList.add(cs);
				}
				paging.setPage(++page);
			} while (paging.getPage() <= 3);

		}

		return statusList;
	}
}