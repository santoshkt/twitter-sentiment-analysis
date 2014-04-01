package twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

import twitter.TwitterAPISettings;

public class CommonUtils {

	public static OAuth2Token getOAuth2Token() {
		OAuth2Token token = null;
		ConfigurationBuilder cb;
		cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey(TwitterAPISettings.browserConsumerKey);
		cb.setOAuthConsumerSecret(TwitterAPISettings.browserConsumerSecret);
		try {
			token = new TwitterFactory(cb.build()).getInstance()
					.getOAuth2Token();
		} catch (Exception e) {
			System.out.println("Can't get OAuth2 token");
			e.printStackTrace();
			System.exit(0);
		}
		return token;
	}

	public static Twitter getTwitterInstance() throws TwitterException {
		OAuth2Token token;
		token = getOAuth2Token();

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey(TwitterAPISettings.browserConsumerKey);
		cb.setOAuthConsumerSecret(TwitterAPISettings.browserConsumerSecret);
		cb.setOAuth2TokenType(token.getTokenType());
		cb.setOAuth2AccessToken(token.getAccessToken());
		
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		
		return twitter;
	}
}
