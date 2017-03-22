package com.codepath.apps.twitter.adapters;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.models.Media;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;
import com.yqritc.scalablevideoview.ScalableType;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class TweetsArrayAdapter extends RecyclerView.Adapter<TweetsArrayAdapter.ViewHolder> {

    // Define class constants
    private static final int PROFILE_IMG_ROUND = 6;
    private static final int MEDIA_IMG_ROUND = 10;
    // Define listener member variable
    private static OnItemClickListener listener;

    private int mVideoResId;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivRetweetStatus) ImageView ivRetweetStatus;
        @BindView(R.id.tvOrigUserName) TextView tvOrigUserName;
        @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
        @BindView(R.id.tvUserName) TextView tvUserName;
        @BindView(R.id.tvScreenName) TextView tvScreenName;
        @BindView(R.id.tvCreatedTime) TextView tvCreatedTime;
        @BindView(R.id.tvBody) TextView tvBody;
        @BindView(R.id.ivMultiMedia) ImageView ivMultiMedia;
        @BindView(R.id.vvMultiMedia)ScalableVideoView vvMultiMedia;
        ScalableType mScalableType;

        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }
    }

    // Store a member variable for the tweets
    private List<Tweet> mTweets;
    // Store the context for easy access
    private Context mContext;

    // Pass in the tweet array into the constructor
    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        mTweets = tweets;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }


    @Override
    public TweetsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom tweet view layout
        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get the data model based on position
        Tweet tweet = mTweets.get(position);
        boolean isRetweet = false;

        // Check retweet_default status
        if (tweet.getRetweetedStatus() != null) {
            holder.ivRetweetStatus.setVisibility(View.VISIBLE);
            holder.tvOrigUserName.setVisibility(View.VISIBLE);
            holder.tvOrigUserName.setText(String.format("%s Retweeted", tweet.getUser().getName()));
            tweet = tweet.getRetweetedStatus();
        } else {
            holder.ivRetweetStatus.setVisibility(View.GONE);
            holder.tvOrigUserName.setVisibility(View.GONE);
        }
        User user = tweet.getUser();

        // Set item views based on your views and data model

        // set the text views
        holder.tvUserName.setText(user.getName());
        holder.tvScreenName.setText(String.format("%s%s",Constants.ATRATE,
                user.getScreenName()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvBody.setText(Html.fromHtml(tweet.getBody(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvBody.setText(Html.fromHtml(tweet.getBody()));
        }
        holder.tvCreatedTime.setText(DateUtil.getRelativeTimeAgo(tweet.getCreatedAt()));

        // set the verified image view
        Drawable drawable =  getContext().getDrawable(R.drawable.verified_user);
        if (user.getVerified()) {
            holder.tvUserName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
        } else {
            holder.tvUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        // find the image views
        ImageView ivProfileImage = holder.ivProfileImage;
        final ImageView ivMultiMedia = holder.ivMultiMedia;

        // clear out recycled image from convertView from last time
        ivProfileImage.setImageResource(android.R.color.transparent);
        ivMultiMedia.setImageResource(android.R.color.transparent);

        // populate the thumbnail image
        // remote download the images for profile and media in the background

            Glide
                    .with(getContext())
                    .load(tweet.getUser().getProfileImageUrl())
                    .bitmapTransform(new RoundedCornersTransformation(mContext, PROFILE_IMG_ROUND, 0))
                    .placeholder(R.drawable.tweet_placeholder)
                    .crossFade()
                    .into(ivProfileImage);

        // Check if multimedia image is available
        Media media = tweet.getMedia();
        if (media != null) {
            Glide
                    .with(getContext())
                    .load(media.getImageUrl()+":large")
                    .bitmapTransform(new RoundedCornersTransformation(mContext, MEDIA_IMG_ROUND, 0))
                    .placeholder(R.drawable.tweet_placeholder)
                    .crossFade()
                    .into(ivMultiMedia);
            if (media.getType().equals(Constants.VIDEO_STR)) {
                Log.d("DEBUG", String.format("Got Video Url for tweet: %s", media.getVideoUrl()));
                // Create a progressbar
           /*     final ProgressDialog pDialog = new ProgressDialog(mContext);
                // Set progressbar title
                pDialog.setTitle("Android Video Streaming Tutorial");
                // Set progressbar message
                pDialog.setMessage("Buffering...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                // Show progressbar
                pDialog.show();
*/
                holder.ivMultiMedia.setVisibility(View.GONE);
                holder.vvMultiMedia.setVisibility(View.VISIBLE);
                holder.vvMultiMedia.bringToFront();
                try {
                    holder.vvMultiMedia.setDataSource(media.getVideoUrl());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                holder.vvMultiMedia.requestFocus();


            }
        }
    }

    /*@Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        setVideo(holder.vvMultiMedia);
    }

    private void setVideo(final ScalableVideoView videoView) {
        try {
            videoView.setVolume(0, 0);
            videoView.setLooping(true);
            videoView.prepare(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        } catch (IOException ioe) {
            //ignore
        }
    }*/

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    public Tweet getItem(int position) {
        return mTweets.get(position);
    }

    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

}