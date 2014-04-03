package examples;
import twitter.CommonUtils;
import twitter4j.*;

/**
 * Looks up users.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class LookupUsers {
    /**
     * Usage: java twitter4j.examples.user.LookupUsers [screen name[,screen name..]]
     *
     * @param args message
     */
    public static void main(String[] args) {
        
    	String LOOKUPKEY = "abhisemweb";
    	
        try {
        	Twitter twitter = CommonUtils.getTwitterInstance();
            ResponseList<User> users = twitter.lookupUsers(LOOKUPKEY.split(","));
            for (User user : users) {
                if (user.getStatus() != null) {
                	
                	System.out.println(user.toString());
                	
                    System.out.println("@" + user.getScreenName() + " - " + user.getStatus().getText());
                } else {
                    // the user is protected
                    System.out.println("@" + user.getScreenName());
                }
            }
            System.out.println("Successfully looked up users [" + LOOKUPKEY + "].");
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to lookup users: " + te.getMessage());
            System.exit(-1);
        }
    }
}