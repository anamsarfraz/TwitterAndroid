package com.codepath.apps.twitter.activities;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.widget.LinearLayout;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class TimelineActivity extends AppCompatActivity {

    TwitterClient client;
    List<Tweet> tweets;
    TweetsArrayAdapter tweetsArrayAdapter;
    @BindView(R.id.rvTweets) RecyclerView rvTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);
        client = TwitterApplication.getRestClient();
        tweets = new ArrayList<>();
        tweetsArrayAdapter = new TweetsArrayAdapter(this, tweets);
        rvTweets.setAdapter(tweetsArrayAdapter);
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getDrawable(R.drawable.line_divider));
        }
        rvTweets.addItemDecoration(dividerItemDecoration);
        // fetch user timeline
        fetchTimeline();
    }

    private void fetchTimeline() {
        TwitterClient client = TwitterApplication.getRestClient();
        client.getHomeTimeline(1, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                Log.d("DEBUG", "timeline: " + jsonArray.toString());
                int curSize = tweetsArrayAdapter.getItemCount();
                List<Tweet> newTweets = Tweet.fromJSONArray(jsonArray);
                tweets.addAll(newTweets);
                tweetsArrayAdapter.notifyItemRangeInserted(curSize, newTweets.size());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

    public void onLogout() {
        client.clearAccessToken();
    }

}
