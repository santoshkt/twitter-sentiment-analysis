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
        
    	String LOOKUPKEY = "30849088,270771330";
    	String[] LookKeySplit = LOOKUPKEY.split(",");
    	int n = LookKeySplit.length;
    	long[] keys = new long[n];
    	int i = 0;
    	for(String key : LookKeySplit){
    		keys[i] = Long.parseLong(key);
    		i++;
    	}
    	
        try {
        	Twitter twitter = CommonUtils.getTwitterInstance();
            ResponseList<User> users = twitter.lookupUsers(keys);
            for (User user : users) {
                if (user.getStatus() != null) {
                	
                	System.out.println(user.toString());
                	
                } else {
                    // the user is protected
                    System.out.println("Protected User: @" + user.getScreenName());
                }
            }
            System.out.println("Successfully looked up users: " + keys.toString());
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to lookup users: " + te.getMessage());
            System.exit(-1);
        }
    }
}