package com.krishna.twitterclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.krishna.twitterclient.data.AllTweets;
import com.krishna.twitterclient.data.Status;
import com.krishna.twitterclient.data.TwitterToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView lvTweetList;
    private EditText editText;
    private ImageButton btnSearch;
    private ProgressBar progressBar;
    private ArrayList<Status> tweetList;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvTweetList = (ListView) findViewById(R.id.tweet_list);
        editText = (EditText) findViewById(R.id.tv_search);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSearch = (ImageButton) findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                String searchText = editText.getText().toString();
                if (checkConnectivity()) {
                    if (searchText.length() != 0)
                        new TwitterTask().execute(searchText);
                    else
                        Toast.makeText(MainActivity.this, "Enter text to search", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean checkConnectivity() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;

    }

    private class TwitterTask extends AsyncTask<String, Void, String> {
        // Removing consumer key and secret key for security reasons
        static final String CONSUMER_KEY = "your consumer key";
        static final String CONSUMER_SECRET_KEY = "your secret key";
        static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth2/token";
        static final String TWEET_URL = "https://api.twitter.com/1.1/search/tweets.json";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            TwitterToken twitterToken = getTwitterAccessToken();
            if (twitterToken == null)
                return null;
            String searchText = params[0];
            return downloadRecentTweets(twitterToken, searchText);
        }

        @Override
        protected void onPostExecute(String tweetString) {
            super.onPostExecute(tweetString);
            progressBar.setVisibility(View.GONE);
            if (tweetString != null) {
                Gson gson = new Gson();
                AllTweets allTweets = gson.fromJson(tweetString, AllTweets.class);
                if (adapter == null) {
                    tweetList = allTweets.statuses;
                    adapter = new Adapter(MainActivity.this, tweetList, R.layout.item_tweet_list);
                    lvTweetList.setAdapter(adapter);
                } else {
                    tweetList.clear();
                    tweetList.addAll(allTweets.statuses);
                    adapter.notifyDataSetChanged();
                }
            } else
                Toast.makeText(MainActivity.this, "ERROR_OCCURRED", Toast.LENGTH_SHORT).show();
        }

        private TwitterToken getTwitterAccessToken() {
            TwitterToken twitterToken = null;
            HttpURLConnection tokenUrlConnection = null;
            try {
                // Encode consumer and secret keys
                String urlApiKey = URLEncoder.encode(CONSUMER_KEY, "UTF-8");
                String urlApiSecret = URLEncoder.encode(CONSUMER_SECRET_KEY, "UTF-8");

                // concatenate the encoded consumer key and secret keys with separator colon
                String combinedAccessKeys = urlApiKey + ":" + urlApiSecret;

                // Encode the combined keys into base64
                String base64EncodedKeys = Base64.encodeToString(combinedAccessKeys.getBytes(), Base64.NO_WRAP);
                // Open HttpUrlConnection
                URL myURL = new URL(ACCESS_TOKEN_URL);
                tokenUrlConnection = (HttpURLConnection) myURL.openConnection();
                tokenUrlConnection.setReadTimeout(10000);
                tokenUrlConnection.setConnectTimeout(15000);
                tokenUrlConnection.setRequestProperty("Authorization", "Basic " + base64EncodedKeys);
                tokenUrlConnection.setRequestMethod("POST");
                tokenUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                tokenUrlConnection.setUseCaches(false);
                tokenUrlConnection.setDoInput(true);
                tokenUrlConnection.setDoOutput(true);
                //write to the outputstream
                String entity = "grant_type=client_credentials";
                OutputStream os = tokenUrlConnection.getOutputStream();
                os.write(entity.getBytes());
                os.flush();

                int responseCode = tokenUrlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStr = tokenUrlConnection.getInputStream();
                    String response = getResponseBody(inputStr);
                    if (response == null)
                        return null;
                    twitterToken = getTwitterTokenFromJson(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Close the connection
                if (tokenUrlConnection != null)
                    tokenUrlConnection.disconnect();
            }
            return twitterToken;
        }

        private TwitterToken getTwitterTokenFromJson(String json) {
            TwitterToken twitterToken = null;
            try {
                Gson gson = new Gson();
                twitterToken = gson.fromJson(json, TwitterToken.class);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            return twitterToken;
        }

        private String downloadRecentTweets(TwitterToken twitterToken, String searchText) {
            String result = null;
            HttpURLConnection tweetUrlConnection = null;
            if (twitterToken.token_type.equals("bearer")) {
                // Build Uri
                Uri uri = Uri.parse(TWEET_URL)
                        .buildUpon()
                        .appendQueryParameter("q", URLEncoder.encode(searchText))
                        .appendQueryParameter("result_type", "recent")
                        .build();
                try {
                    URL url = new URL(uri.toString());
                    tweetUrlConnection = (HttpURLConnection) url.openConnection();
                    tweetUrlConnection.setReadTimeout(10000);
                    tweetUrlConnection.setConnectTimeout(15000);
                    tweetUrlConnection.setRequestMethod("GET");
                    tweetUrlConnection.setRequestProperty("Authorization", "Bearer " + twitterToken.access_token);
                    tweetUrlConnection.setRequestProperty("Content-Type", "application/json");
                    tweetUrlConnection.connect();

                    int responseCode = tweetUrlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStr = tweetUrlConnection.getInputStream();
                        result = getResponseBody(inputStr);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //close the connection
                    if (tweetUrlConnection != null)
                        tweetUrlConnection.disconnect();
                }
            }
            return result;
        }

        private String getResponseBody(InputStream inputStr) {
            StringBuffer stringBuffer = null;
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStr));
                stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // close the BufferedReader
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (stringBuffer.length() > 0)
                return stringBuffer.toString();
            return null;
        }
    }
}
