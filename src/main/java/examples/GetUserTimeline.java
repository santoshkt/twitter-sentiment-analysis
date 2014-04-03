package examples;

import twitter.CommonUtils;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public class GetUserTimeline {
	/**
	 * Usage: java twitter4j.examples.timeline.GetUserTimeline
	 * 
	 * @param args
	 *            String[]
	 * @throws TwitterException
	 */
	public static void main(String[] args) throws TwitterException {
		// gets Twitter instance with default credentials
		Twitter twitter = CommonUtils.getTwitterInstance();
		try {
			List<Status> statuses;
			String user = "Microsoft";

			Paging paging = new Paging(1, 6);
			int page = 1;
			paging.setPage(page);
			paging.setCount(200);

			do {
				statuses = twitter.getUserTimeline(user, paging);
				System.out.println("Showing @" + user + "'s user timeline.");
				for (Status status : statuses) {
					System.out.println(status.toString());
					System.out.println("@" + status.getUser().getScreenName()
							+ " - " + status.getText());
				}
				paging.setPage(++page);
			} while (paging.getPage() <= 6);
			
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		}
	}
}