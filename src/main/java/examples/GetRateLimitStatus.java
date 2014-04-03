package examples;

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

import twitter.CommonUtils;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Map;

public final class GetRateLimitStatus {

	public static void main(String[] args) {
		try {

			Twitter twitter = CommonUtils.getTwitterInstance();

			Map<String, RateLimitStatus> rateLimitStatus = twitter
					.getRateLimitStatus();
			for (String endpoint : rateLimitStatus.keySet()) {
				RateLimitStatus status = rateLimitStatus.get(endpoint);
				if (endpoint.equals("/friends/ids")) {
					System.out.println("Endpoint: " + endpoint);
					System.out.println(" Limit: " + status.getLimit());
					System.out.println(" Remaining: " + status.getRemaining());
					System.out.println(" ResetTimeInSeconds: "
							+ status.getResetTimeInSeconds());
					System.out.println(" SecondsUntilReset: "
							+ status.getSecondsUntilReset());
				}
			}
			System.exit(0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get rate limit status: "
					+ te.getMessage());
			System.exit(-1);
		}
	}
}