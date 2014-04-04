package twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

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

		logger.exit("Finished crawling.");
	}
}

class TwitterSocialGraph {

	private static Logger logger = LogManager
			.getFormatterLogger(TwitterSocialGraph.class.getName());

	public static Twitter twitter;
	private static int rateLimitFollowers = TwitterAPISettings.RATELIMIT15;
	private static int rateLimitFriends = TwitterAPISettings.RATELIMIT15;
	private static int rateLimitFollowersList = TwitterAPISettings.RATELIMIT30;
	private static int rateLimitFriendsList = TwitterAPISettings.RATELIMIT30;
	private static int rateLimitUserTimeline = TwitterAPISettings.RATELIMIT180;
	private static int rateLimitUserLookup = TwitterAPISettings.RATELIMIT15;

	// For each user, a HashSet of the Users he follows.
	private static HashMap<User, HashSet<User>> followsMap = new HashMap<User, HashSet<User>>();
	private static HashSet<Long> friendsHs = new HashSet<Long>();
	private static HashSet<Long> userIDList = new HashSet<Long>();
	private static ArrayList<CrawledStatus> statuses;

	public static void crawl() throws InterruptedException, IOException,
			TwitterException {

		logger.entry();

		try {

			twitter = CommonUtils.getTwitterInstance();

			// Look up for the initial user or subject.

			User subjectUser = twitter
					.showUser(TwitterAPISettings.INITIAL_USER_ID);
			if (subjectUser.getStatus() != null) {
				logger.info("Subject exists: " + subjectUser.toString());
				userIDList.add(subjectUser.getId());
			} else {
				// the user is protected
				logger.error("Protected user: " + subjectUser.toString());
				System.exit(0);
			}

			Long subject = subjectUser.getId();
			logger.info("Successfully looked up for subject.");

			// /////////////////////////////////////////////////////////////////
			// Extract first level users, and enqueue them.
			// This is called at first time for subject. So no loop.

			long cursor = -1;
			PagableResponseList<User> followingUserList = null;

			do {
				rateLimitCheck("rateLimitFriendsList");

				try {
					// Every one subject is following
					followingUserList = twitter.getFriendsList(subject, cursor);
					rateLimitFriendsList--;

				} catch (TwitterException te) {
					if (te.getMessage().contains("Rate limit exceeded")) {
						te.printStackTrace();
						waitForRateLimit15();
						continue;
					} else {
						te.printStackTrace();
						continue;
					}
				}

				HashSet<User> hs = new HashSet<User>();
				for (User user : followingUserList) {
					if (user.getFollowersCount() > 1000) {
						logger.trace("Celebrity account. Ignore. "
								+ user.toString());
					} else {
						hs.add(user);
						logger.trace(subject + " is following "
								+ user.toString());
					}
				}

				if (followsMap.get(subjectUser) == null) {
					followsMap.put(subjectUser, hs);
				} else {
					HashSet<User> hs2 = new HashSet<User>();
					hs2 = followsMap.get(subjectUser);
					hs2.addAll(hs);
					followsMap.put(subjectUser, hs2);
				}
			} while ((cursor = followingUserList.getNextCursor()) != 0);

			// In the followsMap we have all the people subject follows.
			// We have to find who is following him back so that we know
			// the friends of the subjectUser.

			// For each person subject is following
			HashSet<User> subjectHs = followsMap.get(subjectUser);
			int friendCount = 0;
			for (User user : subjectHs) {
				logger.trace("Finding details about: " + user.toString());
				cursor = -1;

				IDs followsFollowingIDs;

				// Get that users that person is following
				rateLimitCheck("rateLimitFriends");

				try {

					if (user.isProtected()) {
						logger.trace(user.getScreenName() + "is protected.");
						continue;
					}
					followsFollowingIDs = twitter.getFriendsIDs(user.getId(),
							cursor);
					rateLimitFriends--;
				} catch (TwitterException te) {
					if (te.getMessage().contains("Rate limit exceeded")) {
						te.printStackTrace();
						waitForRateLimit15();
						continue;
					}

					te.printStackTrace();
					continue;
				}

				HashSet<Long> hs = new HashSet<Long>();
				for (Long followsFollowingId : followsFollowingIDs.getIDs()) {
					hs.add(followsFollowingId);
				}

				if (hs.contains(subjectUser.getId())) {
					// The subject's follower is following him back.
					// Get user objects of the subject's follower's followers
					// and store them in
					// followsMap
					// We will do this later, but for now, just keep all of them
					// in a HashSet
					
					friendsHs.add(subjectUser.getId());
					friendCount++;
					if(TwitterAPISettings.MAX_FRIENDS_COUNT > 0 && friendCount>=TwitterAPISettings.MAX_FRIENDS_COUNT){
						logger.trace("Reached Max friends count.");
						break;
					}else{
						logger.trace("friendCount: "+friendCount);
					}

				}
			}

			// Find the user objects of 2nd level users and add them to
			// followsMap
			int querySize = 0;
			String query = new String();
			for (Long friendID : friendsHs) {
				User friendUser = getFriendUserObj(subjectUser, friendID);
				if (querySize <= 100) {
					query += friendUser.getScreenName() + ",";
				} else {

					ResponseList<User> userObjs = null;

					try {

						rateLimitCheck("rateLimitUserLookup");

						userObjs = twitter.lookupUsers(query.split(","));

					} catch (TwitterException te) {
						if (te.getMessage().contains("Rate limit exceeded")) {
							te.printStackTrace();
							waitForRateLimit15();
							continue;
						}
						te.printStackTrace();
						continue;
					}

					HashSet<User> hs = new HashSet<User>();

					for (User user : userObjs) {
						hs.add(user);
						logger.trace(subject + " is following "
								+ user.toString());

					}

					if (followsMap.get(friendUser) == null) {
						followsMap.put(friendUser, hs);
					} else {
						HashSet<User> hs2 = new HashSet<User>();
						hs2 = followsMap.get(friendUser);
						hs2.addAll(hs);
						followsMap.put(friendUser, hs2);
					}

					query = "";
					querySize = 0;
				}

			}

			// Now, we know who is our subject, people who is friends with
			// subject, people who the subject is following. People the
			// subject's friends
			// are following. For all the users we know, we need to get the
			// tweets.

			for (Map.Entry<User, HashSet<User>> entry : followsMap.entrySet()) {

				User user = entry.getKey();
				HashSet<User> hs = entry.getValue();

				userIDList.add(user.getId());

				for (User fuser : hs) {
					userIDList.add(fuser.getId());
				}
			}

			// Collect tweets of all users in userIDList.

			statuses = getUserTweets(userIDList);
			logger.info("Printing all statuses");
			for (CrawledStatus status : statuses) {
				logger.trace(status.toString());
			}
			
			// Write followsMap, friendsHs and statuses to files.
			DataAccessUtils.writeToFile(followsMap);
			DataAccessUtils.writeToFile(subject, friendsHs);
			DataAccessUtils.writeToFile(statuses);
			

		} catch (TwitterException te) {
			te.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static User getFriendUserObj(User subjectUser, Long friendUser) {

		HashSet<User> hs = followsMap.get(subjectUser);

		for (User user : hs) {
			if (user.getId() == friendUser) {
				return user;
			}
		}

		return null;
	}

	private static void rateLimitCheck(String rateLimitType)
			throws InterruptedException {

		logger.entry(rateLimitType);

		try {

			switch (rateLimitType) {
			case "rateLimitFriendsList":
				if (rateLimitFriendsList <= 0) {
					Thread.sleep(getRateLimit("/friends/list") * 1000);
					resetRateLimits();
				}
				break;
			case "rateLimitFollowersList":
				if (rateLimitFollowersList <= 0) {
					Thread.sleep(getRateLimit("/followers/list") * 1000);
					resetRateLimits();
				}
				break;
			case "rateLimitFriends":
				if (rateLimitFriends <= 0) {
					Thread.sleep(getRateLimit("/friends/ids") * 1000);
					resetRateLimits();
				}
				break;
			case "rateLimitFollowers":
				if (rateLimitFollowers <= 0) {
					Thread.sleep(getRateLimit("/followers/ids") * 1000);
					resetRateLimits();
				}
				break;
			case "rateLimitUserLookup":
				if (rateLimitUserLookup <= 0) {
					Thread.sleep(getRateLimit("/users/lookup") * 1000);
					resetRateLimits();
				}
				break;
			case "rateLimitUserTimeline":
				if (rateLimitUserTimeline <= 0) {
					Thread.sleep(getRateLimit("/statuses/user_timeline") * 1000);
					resetRateLimits();
				}
				break;
			default:
				logger.error("Unknown rate limit type. This may cause some error later.");
				
			}


		} catch (Exception te) {
			te.printStackTrace();
			waitForRateLimit15();
		}
	}

	private static int getRateLimit(String rateLimitString)
			throws InterruptedException {
		try {

			Twitter twitter = CommonUtils.getTwitterInstance();

			Map<String, RateLimitStatus> rateLimitStatus = twitter
					.getRateLimitStatus();
			for (String endpoint : rateLimitStatus.keySet()) {
				RateLimitStatus status = rateLimitStatus.get(endpoint);
				if (endpoint.equals(rateLimitString)) {
					logger.info("Endpoint: " + endpoint);
					logger.info(" Limit: " + status.getLimit());
					logger.info(" Remaining: " + status.getRemaining());
					logger.info(" ResetTimeInSeconds: "
							+ status.getResetTimeInSeconds());
					logger.info(" SecondsUntilReset: "
							+ status.getSecondsUntilReset());
					return status.getResetTimeInSeconds();
				}
			}
		} catch (TwitterException te) {
			te.printStackTrace();
			logger.fatal("Failed to get rate limit status: " + te.getMessage());
			waitForRateLimit15();
			return 1;
		}

		return 900;
	}

	private static void waitForRateLimit15() throws InterruptedException {
		logger.entry("Waiting for 900000");
		for (int i = 9; i >=0 ; i--) {
			logger.entry("Sleeping for 90000, will sleep for " + i
					+ " more times.");
			Thread.sleep(90000);
		}

		resetRateLimits();

		logger.exit();
	}

	private static void resetRateLimits() {
		logger.entry();
		rateLimitFriendsList = TwitterAPISettings.RATELIMIT30;
		rateLimitFollowersList = TwitterAPISettings.RATELIMIT30;
		rateLimitFriends = TwitterAPISettings.RATELIMIT15;
		rateLimitFollowers = TwitterAPISettings.RATELIMIT15;
		rateLimitUserTimeline = TwitterAPISettings.RATELIMIT180;
		rateLimitUserLookup = TwitterAPISettings.RATELIMIT15;
		logger.exit();
	}

	private static ArrayList<CrawledStatus> getUserTweets(
			HashSet<Long> userIDList) throws TwitterException,
			InterruptedException {
		logger.entry();
		List<Status> statuses;
		ArrayList<CrawledStatus> statusList = new ArrayList<CrawledStatus>();

		for (Long user : userIDList) {
			Paging paging = new Paging(1, 6);
			int page = 1;
			paging.setPage(page);
			paging.setCount(200);

			do {
				rateLimitCheck("rateLimitUserTimeline");
				try {

					statuses = twitter.getUserTimeline(user, paging);
					rateLimitUserTimeline--;
				} catch (TwitterException te) {
					if (te.getMessage().contains("Rate limit exceeded")) {
						te.printStackTrace();
						waitForRateLimit15();
						continue;
					}
					te.printStackTrace();
					continue;
				}
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