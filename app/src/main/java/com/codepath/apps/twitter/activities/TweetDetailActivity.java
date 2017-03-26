package com.codepath.apps.twitter.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.twitter.models.Media;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class TweetDetailActivity extends AppCompatActivity {

    private static final int PROFILE_IMG_ROUND = 6;
    private static final int MEDIA_IMG_ROUND = 10;
    private static final String RETWEEETS = "RETWEEETS";
    private static final String LIKES = "LIKES";
    final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
    final StyleSpan nss = new StyleSpan(Typeface.NORMAL); //Span to make text normal

    private ActivityTweetDetailBinding binding;
    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet_detail);

        setSupportActionBar(binding.tbDetail);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation((float)10.0);

        processIntent();
        populateViews();

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
        binding.tvDetailScreenName.setText(tweet.getUser().getScreenName());
        binding.tvDetailBody.setText(tweet.getBody());



        binding.tvLikes.setText(buildSpan(tweet.getFavoriteCount(), LIKES));
        binding.tvRetweets.setText(buildSpan(tweet.getRetweetCount(), RETWEEETS));
        binding.tvDetailDate.setText(DateUtil.getDateTimeInFormat(tweet.getCreatedAt(), "dd MMM yy"));
        binding.tvDetailTime.setText(DateUtil.getDateTimeInFormat(tweet.getCreatedAt(), "h:mm a"));


    }

    private SpannableStringBuilder buildSpan(long count, String suffix) {
        String formatString = String.format("%d %s", count, suffix);

        SpannableStringBuilder sb = new SpannableStringBuilder(formatString);
        int countLength = String.valueOf(count).length();

        // make count characters Bold
        sb.setSpan(bss, 0, countLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(nss, countLength, formatString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return sb;
    }

    private void processIntent() {
        Intent intent = getIntent();
        tweet = Parcels.unwrap(intent.getParcelableExtra("tweet"));
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
}
