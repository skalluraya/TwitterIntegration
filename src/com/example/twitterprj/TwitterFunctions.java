package com.example.twitterprj;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public 
class TwitterFunctions {
	private static Twitter twitter;
	private static TwitterFunctions instance;
	private static RequestToken requestToken;
	private static SharedPreferences mSharedPreferences;
	static final String TWITTER_CONSUMER_KEY = "NGypfnfoddKqcCokU8G7OxHzb";
	static final String TWITTER_CONSUMER_SECRET = "7NhJHehD6IoBX4K5w7iCdeq8CHQLZwm9lDCR8kZ31GAoeZSx1T";
	static final String PREFERENCE_NAME = "twitter_oauth";
	static final String TWITTER_CALLBACK_URL = "oauth://trackertest";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	private static final String TAG = TwitterFunctions.class.getName();
	private static final String SHARED_PREFERNCE_NAME = "TwitterDB";

	private TwitterFunctions() {

	}

	public static TwitterFunctions getInstance() {
		instance = new TwitterFunctions();
		return instance;
	}

	public String verifyLogin(Uri uri, Context context) {
		if (!isTwitterLoggedInAlready(context)) {
			if (uri != null && uri.toString().startsWith(TwitterFunctions.TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri.getQueryParameter(TwitterFunctions.URL_TWITTER_OAUTH_VERIFIER);
				Log.d(TAG, "verifier:::" + verifier);
				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
					storeAccessToken(accessToken, context);
					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					return user.getName();
				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}
			}
		}
		return null;
	}

	public String getUserName(Context context) {
		mSharedPreferences = context.getApplicationContext()
				.getSharedPreferences(TwitterFunctions.SHARED_PREFERNCE_NAME, 0);

		// Access Token
		String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
		// Access Token Secret
		String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
		AccessToken accessToken = new AccessToken(access_token, access_token_secret);
		long userID = accessToken.getUserId();
		User user;
		try {
			initTwitterObj(accessToken);
			user = twitter.showUser(userID);
			return user.getName();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void initTwitterObj(AccessToken accessToken) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
		builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
		if (accessToken != null) {
			builder.setOAuthAccessToken(accessToken.getToken());
			builder.setOAuthAccessTokenSecret(accessToken.getTokenSecret());
		}

		Configuration configuration = builder.build();
		TwitterFactory factory = new TwitterFactory(configuration);
		twitter = factory.getInstance();
	}

	public Boolean loginToTwitter(Context context) {
		Log.d(TAG, "twitter:::" + twitter);
		if (!isTwitterLoggedInAlready(context)) {
			try {
				initTwitterObj(null);				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				Log.d(TAG, "requestToken:" + requestToken.getAuthenticationURL());
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
//				((Activity)context).finish();;
				return false;
			} catch (TwitterException e) {
				e.printStackTrace();
				return null;
			}

		} else {
			return true;
		}
	}

	public void logoutFromTwitter(Context context) {
		// Clear the shared preferences
		mSharedPreferences = context.getApplicationContext()
				.getSharedPreferences(TwitterFunctions.SHARED_PREFERNCE_NAME, 0);
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();

	}

	public boolean isTwitterLoggedInAlready(Context context) {
		mSharedPreferences = context.getApplicationContext()
				.getSharedPreferences(TwitterFunctions.SHARED_PREFERNCE_NAME, 0);
		Log.d(TAG, "isTwitterLoggedInAlready:" + mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false));
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	private static void storeAccessToken(AccessToken accessToken, Context context) {
		try {
			mSharedPreferences = context.getApplicationContext()
					.getSharedPreferences(TwitterFunctions.SHARED_PREFERNCE_NAME, 0);
			Editor e = mSharedPreferences.edit();
			e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
			e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
			e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
			e.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Status> getUserTimeLine(Context context) {
		try {
			if (isTwitterLoggedInAlready(context)) {
				List<Status> list = twitter.getHomeTimeline();
				Log.d(TAG, "getUserTimeLine:" + list);
				return list;
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String updateStatus(String status) {
		try {
			Status updated_status = twitter.updateStatus(status);
			Log.d(TAG, "ge statuse:" + updated_status.getText());
			return updated_status.getText();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}