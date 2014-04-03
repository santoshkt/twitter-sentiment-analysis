package examples;

import twitter.CommonUtils;
import twitter4j.*;

/**
 * Search users with the specified query.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class SearchUsers {
    /**
     * Usage: java twitter4j.examples.user.SearchUsers [query]
     *
     * @param args message
     */
    public static void main(String[] args) {
        
    	String LOOKUPKEY = "130458288";
    	
        try {
        	Twitter twitter = CommonUtils.getTwitterInstance();
            int page = 1;
            ResponseList<User> users;
            do {
                users = twitter.searchUsers(LOOKUPKEY, page);
                for (User user : users) {
                    if (user.getStatus() != null) {
                        System.out.println("@" + user.getScreenName() + " - " + user.getStatus().getText());
                    } else {
                        // the user is protected
                        System.out.println("@" + user.getScreenName());
                    }
                }
                page++;
            } while (users.size() != 0 && page < 50);
            System.out.println("done.");
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search users: " + te.getMessage());
            System.exit(-1);
        }
    }
}
