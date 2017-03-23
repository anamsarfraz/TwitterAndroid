package com.codepath.apps.twitter.activities;

import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ProgressBar;

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

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class TimelineActivity extends AppCompatActivity {

    public static final String DEBUG = "DEBUG";
    public static final String ERROR = "ERROR";
    private static final int RATE_LIMIT_ERR = 88;
    private static final int RETRY_LIMIT = 3;
    private static final long DELAY_MILLI = 3000;
    TwitterClient client;
    List<Tweet> tweets;
    long currMaxId;
    int retryCount;
    TweetsArrayAdapter tweetsArrayAdapter;
    LinearLayoutManager linearLayoutManager;
    @BindView(R.id.rvTweets) RecyclerView rvTweets;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.pbLoading) ProgressBar progressBar;
    Handler handler;
    final Runnable runnableCode = new Runnable() {

        @Override
        public void run() {
            fetchTimeline();
        }
    };

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
        handler = new Handler();

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

    private void setUpScrollListener() {
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                retryCount = 0;
                Log.d(DEBUG, "Scroll initiated");
                if (Connectivity.isConnected(getApplicationContext())) {
                    fetchTimeline();
                } else {
                    fetchOffline();
                }

            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);
    }

    private void fetchTimeline() {
        client.getHomeTimeline(currMaxId, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                hideRefreshControl();
                Log.d("DEBUG", "timeline: " + jsonArray.toString());
                List<Tweet> newTweets = Tweet.fromJSONArray(jsonArray);
                processFetchedTweets(newTweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching timeline: " + errorResponse.toString());
                int errorCode = errorResponse.optJSONArray("errors").optJSONObject(0).optInt("code", 0);

                if (errorCode == RATE_LIMIT_ERR && retryCount < RETRY_LIMIT) {
                    retryCount++;
                    handler.postDelayed(runnableCode, DELAY_MILLI);
                } else {
                    fetchOffline();
                }
            }
        });
    }

    private void fetchOffline() {
        List<Tweet> newTweets = Tweet.recentItems(currMaxId);
        processFetchedTweets(newTweets);

    }

    private void processFetchedTweets(List<Tweet> newTweets) {
        int curSize = tweetsArrayAdapter.getItemCount();
        tweets.addAll(newTweets);
        int newSize = newTweets.size();
        currMaxId = newTweets.get(newSize-1).getUid()-1;
        tweetsArrayAdapter.notifyItemRangeInserted(curSize, newSize);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        handler.removeCallbacks(runnableCode);
    }

    public void beginNewSearch() {
        currMaxId = 0L;
        retryCount = 0;
        tweetsArrayAdapter.clearItems();
        scrollListener.resetState();
        hideRefreshControl();
        progressBar.setVisibility(ProgressBar.VISIBLE);

        if (Connectivity.isConnected(this)) {
            fetchTimeline();
        } else {
            fetchOffline();
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

    public void onLogout() {
        client.clearAccessToken();
    }

}
