/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package examples;

import twitter.CommonUtils;
import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;

/**
 * Lists friends' ids
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class GetFriendsIDs {
    /**
     * Usage: java twitter4j.examples.friendsandfollowers.GetFriendsIDs [screen name]
     *
     * @param args message
     */
    public static void main(String[] args) {
        try {
            Twitter twitter = CommonUtils.getTwitterInstance();
            long cursor = -1;
            PagableResponseList<User> userList;
            System.out.println("Listing following ids.");
            do {
            	userList = twitter.getFriendsList("tsantoo", cursor);
                for (User user : userList) {
                    System.out.println(user.toString());
                }
            } while ((cursor = userList.getNextCursor()) != 0);
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get friends' ids: " + te.getMessage());
            System.exit(-1);
        }
    }
}
