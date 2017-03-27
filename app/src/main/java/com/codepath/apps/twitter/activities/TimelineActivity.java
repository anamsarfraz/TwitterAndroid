package com.codepath.apps.twitter.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.fragments.ComposeFragment;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Connectivity;
import com.codepath.apps.twitter.util.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import java.util.ArrayList;
import java.util.List;

import com.codepath.apps.twitter.databinding.ActivityTimelineBinding;


public class TimelineActivity extends AppCompatActivity implements ComposeFragment.OnComposeListener {

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
    private ActivityTimelineBinding binding;

    Handler handler;
    final Runnable fetchRunnable = new Runnable() {

        @Override
        public void run() {
            fetchTimeline();
        }
    };
    final Runnable postTweetRunnable = new Runnable() {

        @Override
        public void run() {
            postTweet();
        }
    };



    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        client = TwitterApplication.getRestClient();
        tweets = new ArrayList<>();
        tweetsArrayAdapter = new TweetsArrayAdapter(this, tweets);
        handler = new Handler();

        setToolbarScroll();
        processSendIntent();
        setUpRecycleView();
        setUpRefreshControl();
        setUpScrollListeners();
        setUpClickListeners();
        loadUserProfileImage();

        // fetch user timeline on first load
        beginNewSearch();
    }

    private void setToolbarScroll() {
        binding.abTimeline.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;


            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
                //Initialize the size of the scroll
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                binding.tbViews.ivAppIcon.setImageResource(android.R.color.transparent);
                //Check if the view is collapsed
                if (scrollRange + verticalOffset == 0) {
                    binding.tbTimeline.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.twitter_blue));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        binding.tbViews.ivAppIcon.setBackgroundResource(R.drawable.tweet_social_white);
                    }

                }else{
                    binding.tbTimeline.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        binding.tbViews.ivAppIcon.setBackgroundResource(R.drawable.tweet_social);
                    }

                }
            }
        });
    }


    private void loadUserProfileImage() {
        ImageView imgView = (ImageView) findViewById(R.id.ivUserImageTimeline);
        Glide.with(this)
                .load(User.getCurrentUser().getProfileImageUrl())
                .bitmapTransform(new CropCircleTransformation(this))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(imgView);
    }

    private void processSendIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Log.d(DEBUG, "Share Intent received");
            if ("text/plain".equals(type)) {

                // Make sure to check whether returned data will be null.
                String titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT);

                String sharedContent = String.format("%s\n%s", titleOfPage, urlOfPage);
                showComposeDialog(sharedContent);
            }
        }
    }

    private void setUpClickListeners() {
        binding.fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showComposeDialog(null);
            }
        });

        binding.tbViews.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        tweetsArrayAdapter.setOnItemClickListener(new TweetsArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {

                Intent intent = new Intent(TimelineActivity.this, TweetDetailActivity.class);
                intent.putExtra("tweet", Parcels.wrap(tweets.get(position)));

                Bundle animationBundle =
                        ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slide_from_left,R.anim.slide_to_left).toBundle();
                startActivity(intent, animationBundle);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setUpRecycleView() {
        binding.rvTweets.setAdapter(tweetsArrayAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dividerItemDecoration.setDrawable(getDrawable(R.drawable.line_divider));
        }
        binding.rvTweets.addItemDecoration(dividerItemDecoration);
    }

    private void setUpScrollListeners() {
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                binding.pbLoading.setVisibility(ProgressBar.VISIBLE);
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
        binding.rvTweets.addOnScrollListener(scrollListener);

    }

    private void fetchTimeline() {
        client.getHomeTimeline(currMaxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                hideRefreshControl();
                Log.d("DEBUG", "timeline: " + jsonArray.toString());
                List<Tweet> newTweets = Tweet.fromJSONArray(jsonArray);
                processFetchedTweets(newTweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideRefreshControl();
                Log.e(ERROR, "Error fetching timeline: " + errorResponse == null ? "Uknown error" : errorResponse.toString());
                int errorCode = errorResponse.optJSONArray("errors").optJSONObject(0).optInt("code", 0);

                if (errorCode == RATE_LIMIT_ERR && retryCount < RETRY_LIMIT) {
                    retryCount++;
                    handler.postDelayed(fetchRunnable, DELAY_MILLI);
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
        retryCount = 0;
        int curSize = tweetsArrayAdapter.getItemCount();
        tweets.addAll(newTweets);
        int newSize = newTweets.size();
        currMaxId = newTweets.get(newSize-1).getUid()-1;
        tweetsArrayAdapter.notifyItemRangeInserted(curSize, newSize);
        binding.pbLoading.setVisibility(ProgressBar.INVISIBLE);
        handler.removeCallbacks(fetchRunnable);
    }

    public void beginNewSearch() {
        currMaxId = 0L;
        retryCount = 0;
        tweetsArrayAdapter.clearItems();
        scrollListener.resetState();
        hideRefreshControl();
        binding.pbLoading.setVisibility(ProgressBar.VISIBLE);

        if (Connectivity.isConnected(this)) {
            fetchTimeline();
        } else {
            fetchOffline();
        }
    }

    private void hideRefreshControl() {
        if (binding.swipeContainer.isRefreshing()) {
            binding.swipeContainer.setRefreshing(false);
        }
    }

    private void setUpRefreshControl() {
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                beginNewSearch();
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void showComposeDialog(String shareContent) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeFragment composeFragment = ComposeFragment.newInstance(shareContent);
        composeFragment.show(fm, "fragment_compose");
    }

    public void logout() {

        client.clearAccessToken();
        Intent intent = new Intent(TimelineActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void createTweet(Tweet tweet) {
        tweets.add(0, tweet);
        tweetsArrayAdapter.notifyItemInserted(0);
        postTweet();

    }

    private void postTweet() {
        client.postTweet(tweets.get(0).getBody(), null, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Tweet.saveTweet(jsonObject);
                handler.removeCallbacks(postTweetRunnable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(ERROR, "Error creating tweet: " + errorResponse.toString());
                int errorCode = errorResponse.optJSONArray("errors").optJSONObject(0).optInt("code", 0);
                if (errorCode == RATE_LIMIT_ERR && retryCount < RETRY_LIMIT) {
                    retryCount++;
                    handler.postDelayed(postTweetRunnable, DELAY_MILLI);
                } else {
                    Toast.makeText(getApplicationContext(), "Error creating tweet. Please try again", Toast.LENGTH_SHORT).show();
                    retryCount = 0;
                    handler.removeCallbacks(postTweetRunnable);
                }
            }
        });
    }
}
