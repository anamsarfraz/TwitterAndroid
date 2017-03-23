package com.codepath.apps.twitter.activities;

import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import java.util.ArrayList;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class TimelineActivity extends AppCompatActivity {

    public static final String DEBUG = "DEBUG";
    TwitterClient client;
    List<Tweet> tweets;
    long currMaxId;
    TweetsArrayAdapter tweetsArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    @BindView(R.id.rvTweets) RecyclerView rvTweets;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;

    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);
        client = TwitterApplication.getRestClient();
        tweets = new ArrayList<>();
        tweetsArrayAdapter = new TweetsArrayAdapter(this, tweets);

        setUpRecycleView();
        setUpRefreshControl();
        setUpScrollListener();

        // fetch user timeline on first load
        beginNewSearch();
    }

    private void setUpRecycleView() {
        rvTweets.setAdapter(tweetsArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getDrawable(R.drawable.line_divider));
        }
        rvTweets.addItemDecoration(dividerItemDecoration);
    }


    private void fetchTimeline() {
        client.getHomeTimeline(currMaxId, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                hideRefreshControl();
                Log.d("DEBUG", "timeline: " + jsonArray.toString());
                int curSize = tweetsArrayAdapter.getItemCount();
                List<Tweet> newTweets = Tweet.fromJSONArray(jsonArray);
                tweets.addAll(newTweets);
                int newSize = newTweets.size();
                currMaxId = newTweets.get(newSize-1).getUid()-1;
                tweetsArrayAdapter.notifyItemRangeInserted(curSize, newSize);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
                beginNewSearch();
            }
        });


        /* For debugging, loading from database only*/
        /*int curSize = tweetsArrayAdapter.getItemCount();

        List<Tweet> newTweets = Tweet.recentItems();
        tweets.addAll(newTweets);
        tweetsArrayAdapter.notifyItemRangeInserted(curSize, newTweets.size());
*/
    }

    public void beginNewSearch() {
        currMaxId = 0L;
        tweetsArrayAdapter.clearItems();
        scrollListener.resetState();
        hideRefreshControl();
        if (Connectivity.isConnected(this)) {
            fetchTimeline();
        } else {
            Toast.makeText(this, "Unable to access internet. Network Error?", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideRefreshControl() {
        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }
    }

    private void setUpRefreshControl() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                beginNewSearch();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }


    private void setUpScrollListener() {
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                fetchTimeline();
                Log.d(DEBUG, "Scroll initiated");
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);
    }

    public void onLogout() {
        client.clearAccessToken();
    }

}
