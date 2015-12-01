package com.example.myexample.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getName();
	private static final long REFRESH_TIME_INTERVAL = 30000;
	TwitterFunctions mTweetFunctions = null;
	TweetListAdaptor mTweetAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTweetFunctions = TwitterFunctions.getInstance();
		mTweetAdapter = new TweetListAdaptor(MainActivity.this, new ArrayList<UserTweet>());
		
		findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Login().execute();

			}
		});
		
		findViewById(R.id.login_name).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String text = ((TextView)v).getText().toString();
				Log.d(TAG, "Text in Login:"+ text);
				if(text!=null && text.equals(getString(R.string.login))){
					new Login().execute();
				}
			}
		});
		

		findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Logout().execute();

			}
		});

		findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String status = ((TextView) findViewById(R.id.status_text)).getText().toString();
				if (!mTweetFunctions.isTwitterLoggedInAlready(MainActivity.this)) {
					Toast.makeText(getApplicationContext(), getString(R.string.please_login), Toast.LENGTH_LONG).show();
				} else {
					if (status != null && !status.equals("")) {
						new UpdateStatus().execute(status);
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.please_enter_status_text),
								Toast.LENGTH_LONG).show();
					}

				}

			}
		});
		
		ListView listView = ((ListView) findViewById(R.id.listView));
		listView.setAdapter(mTweetAdapter);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		final Uri uri = getIntent().getData();
		Log.d(TAG, "onResume:" + uri);
		if (uri != null && uri.toString().startsWith(TwitterFunctions.TWITTER_CALLBACK_URL)) {
			new VerifyLogin().execute(uri);

		}
	}

	private void uiPostLogin(String userName) {
		findViewById(R.id.logout).setVisibility(View.VISIBLE);
		if (userName != null) {
			((TextView) findViewById(R.id.login_name)).setText(userName);
		}

		findViewById(R.id.login).setVisibility(View.GONE);
		findViewById(R.id.bottombar).setVisibility(View.VISIBLE);
		new GetTimeline().execute();
	}

	private void uiPostLogout() {
		findViewById(R.id.login).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.login_name)).setText(getString(R.string.login));
		findViewById(R.id.logout).setVisibility(View.GONE);
		findViewById(R.id.bottombar).setVisibility(View.GONE);
		updateTweetData(new ArrayList<UserTweet>());
	}

	public void updateTweetData(List<UserTweet> itemsArrayList) {
		mTweetAdapter.clear();
		if (itemsArrayList != null) {
			for (UserTweet userTweet : itemsArrayList) {
				mTweetAdapter.insert(userTweet, mTweetAdapter.getCount());
			}
		}
		mTweetAdapter.notifyDataSetChanged();
	}

	private void updateTweetList() {
		Log.d(TAG, "call to updateTweetList function");
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Log.d(TAG, "call to updateTweetList:scheduleAtFixedRate");
				try {

					new GetTimeline().execute();

				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}, MainActivity.REFRESH_TIME_INTERVAL, MainActivity.REFRESH_TIME_INTERVAL);
	}

	class VerifyLogin extends AsyncTask<Uri, Void, String> {
		@Override
		protected String doInBackground(Uri... arg0) {
			try {
				return mTweetFunctions.verifyLogin(arg0[0], MainActivity.this);
			} catch (Exception e) {
				Log.e(TAG, "Error loading JSON", e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				uiPostLogin(result);
			}
		}
	}

	class Login extends AsyncTask<Void, Void, String> {
		private ProgressDialog progressDialog;

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading. Please wait...", true);
		}

		@Override
		protected String doInBackground(Void... arg0) {
			try {
				boolean loggedIn = mTweetFunctions.loginToTwitter(MainActivity.this);
				if (loggedIn)
					return mTweetFunctions.getUserName(MainActivity.this);
				else
					return null;
			} catch (Exception e) {
				Log.e(TAG, "Error loading JSON", e);
				return null;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			if (result != null) {
				Toast.makeText(getApplicationContext(), "Already Logged into twitter", Toast.LENGTH_LONG).show();
				uiPostLogin(result);
				updateTweetList();
			}
		}

	}

	class Logout extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this, "", "Logging out. Please wait...", true);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				mTweetFunctions.logoutFromTwitter(MainActivity.this);
			} catch (Exception e) {
				Log.e(TAG, "Error loading JSON", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			uiPostLogout();
		}

	}

	class GetTimeline extends AsyncTask<Void, Void, List<twitter4j.Status>> {
		@Override
		protected List<twitter4j.Status> doInBackground(Void... arg0) {
			try {
				Log.d(TAG, "call to GetTimeline doInBackground function");
				return mTweetFunctions.getUserTimeLine(MainActivity.this);
			} catch (Exception e) {
				Log.e(TAG, "Error loading JSON", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<twitter4j.Status> userTweetList) {
			ArrayList<UserTweet> tweetList = new ArrayList<UserTweet>();
			if (userTweetList != null) {
				for (twitter4j.Status userTweet : userTweetList) {
					UserTweet tweet = new UserTweet();
					tweet.author = userTweet.getUser().getName();
					tweet.content = userTweet.getText();
					tweetList.add(tweet);
				}
				updateTweetData(tweetList);
			}
		}
	}

	class UpdateStatus extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... status) {
			try {
				Log.d(TAG, "call to GetTimeline doInBackground function");
				return mTweetFunctions.updateStatus(status[0]);
			} catch (Exception e) {
				Log.e(TAG, "Error loading JSON", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String status) {
			if (status != null) {
				Toast.makeText(getApplicationContext(), "Status updated with:" + status, Toast.LENGTH_LONG).show();
				((EditText) findViewById(R.id.status_text)).setText("");
				new GetTimeline().execute();
			}
		}

	}

}
