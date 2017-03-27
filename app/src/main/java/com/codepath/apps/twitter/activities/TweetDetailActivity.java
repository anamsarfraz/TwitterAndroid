package com.codepath.apps.twitter.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.twitter.fragments.ComposeFragment;
import com.codepath.apps.twitter.models.Media;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;
import com.codepath.apps.twitter.util.TwitterApplication;
import com.codepath.apps.twitter.util.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.codepath.apps.twitter.R.color.twitter_blue;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;


public class TweetDetailActivity extends AppCompatActivity implements ComposeFragment.OnComposeListener {

    private static final int PROFILE_IMG_ROUND = 6;
    private static final int MEDIA_IMG_ROUND = 10;
    private static final String RETWEEETS = "RETWEETS";
    private static final String LIKES = "LIKES";
    private static final String REPLY_TO = "Reply to ";
    private static final int MAX_COUNT = 140;
    final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
    final StyleSpan nss = new StyleSpan(Typeface.NORMAL); //Span to make text normal
    public static final String ERROR = "ERROR";


    private ActivityTweetDetailBinding binding;
    Tweet tweet;
    String screenName;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet_detail);
        client = TwitterApplication.getRestClient();

        setSupportActionBar(binding.tbDetail);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation((float)10.0);

        processIntent();
        populateViews();
        setUpClickListeners();

    }

    private void setUpClickListeners() {
        binding.btnExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showComposeDialog(binding.etReply.getText().toString());
            }
        });

        binding.etReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etReply.setText(screenName);
                binding.etReply.setSelection(screenName.length());
                binding.etReply.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                binding.tvCharCountDetail.setText(String.format("%d", MAX_COUNT-screenName.length()));
                binding.tvCharCountDetail.setVisibility(View.VISIBLE);
                binding.btnRTextDetail.setVisibility(View.VISIBLE);
                binding.etReply.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        int currLength = binding.etReply.getText().toString().length();

                        int remainingCount = MAX_COUNT - currLength;
                        binding.tvCharCountDetail.setText(String.format("%d", remainingCount));

                        binding.btnRTextDetail.setEnabled(remainingCount < 0 ? false: true);
                        binding.btnRTextDetail.setTextColor(ContextCompat.getColor(getApplicationContext(), remainingCount < 0 ? android.R.color.darker_gray : android.R.color.holo_blue_light));

                        binding.tvCharCountDetail.setTextColor(ContextCompat.getColor(getApplicationContext(), remainingCount < 0 ? android.R.color.holo_red_dark : android.R.color.darker_gray));



                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (binding.etReply.getText().length() == 0) {
                            binding.etReply.removeTextChangedListener(this);
                            binding.etReply.setText(String.format("%s%s", REPLY_TO, tweet.getUser().getName()));
                            binding.etReply.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                            binding.etReply.addTextChangedListener(this);
                        }

                    }

                });

            }
        });
    }

    private void populateViews() {
        // Load images
        Glide.with(this)
                .load(tweet.getUser().getProfileImageUrl())
                .bitmapTransform(new RoundedCornersTransformation(this, PROFILE_IMG_ROUND, 0))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(binding.ivProfileDetailImage);

        // Check if multimedia image is available
        Media media = tweet.getMedia();
        if (media != null) {
            binding.ivDetailMultiMedia.setVisibility(View.VISIBLE);
            Glide
                    .with(this)
                    .load(media.getImageUrl())
                    .bitmapTransform(new RoundedCornersTransformation(this, MEDIA_IMG_ROUND, 0))
                    .placeholder(R.drawable.tweet_social)
                    .crossFade()
                    .into(binding.ivDetailMultiMedia);
            if (media.getType().equals(Constants.VIDEO_STR)) {
                Log.d("DEBUG", String.format("Got Video Url for tweet: %s", media.getVideoUrl()));
            }
        } else {
            binding.ivDetailMultiMedia.setVisibility(View.GONE);
        }

        binding.tvDetailUserName.setText(tweet.getUser().getName());
        binding.tvDetailScreenName.setText(screenName);
        binding.tvDetailBody.setText(tweet.getBody());

        if (tweet.isFavorited()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.btnLikeDetail.setBackground(getDrawable(R.drawable.like_selected));
            }
        }

        binding.tvLikes.setText(buildSpan(Constants.format(tweet.getFavoriteCount()), LIKES));
        binding.tvRetweets.setText(buildSpan(Constants.format(tweet.getRetweetCount()), RETWEEETS));
        if (tweet.isRetweeted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.btnRetweetDetail.setBackground(getDrawable(R.drawable.retweet_selected));
            }
        }
        binding.tvDetailDate.setText(DateUtil.getDateTimeInFormat(tweet.getCreatedAt(), "dd MMM yy"));
        binding.tvDetailTime.setText(DateUtil.getDateTimeInFormat(tweet.getCreatedAt(), "h:mm a"));
        binding.etReply.setText(String.format("%s%s", REPLY_TO, tweet.getUser().getName()));

    }

    private void showComposeDialog(String replyContent) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeFragment composeFragment = ComposeFragment.newInstance(replyContent);
        composeFragment.show(fm, "fragment_compose");
    }

    private SpannableStringBuilder buildSpan(String countStr, String suffix) {
        String formatString = String.format("%s %s", countStr, suffix);

        SpannableStringBuilder sb = new SpannableStringBuilder(formatString);
        int countLength = countStr.length();

        // make count characters Bold
        sb.setSpan(bss, 0, countLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(nss, countLength, formatString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return sb;
    }

    private void processIntent() {
        Intent intent = getIntent();
        tweet = Parcels.unwrap(intent.getParcelableExtra("tweet"));
        screenName = String.format("%s%s ", Constants.ATRATE, tweet.getUser().getScreenName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void createTweet(Tweet tweet) {
        postTweet(tweet);
    }

    private void postTweet(Tweet tweet) {
        client.postTweet(tweet.getBody(), this.tweet.getIdStr(), new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Tweet.saveTweet(jsonObject);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(ERROR, "Error creating tweet: " + errorResponse.toString());

            }
        });
    }
}
