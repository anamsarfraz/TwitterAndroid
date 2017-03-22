package com.codepath.apps.twitter.adapters;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.Constants;
import com.codepath.apps.twitter.util.DateUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class TweetsArrayAdapter extends RecyclerView.Adapter<TweetsArrayAdapter.ViewHolder> {

    // Define listener member variable
    private static OnItemClickListener listener;

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
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        Tweet tweet = mTweets.get(position);
        boolean isRetweet = false;

        // Check retweet status
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

        // find the image view
        ImageView ivProfileImage = holder.ivProfileImage;

        // clear out recycled image from convertView from last time
        ivProfileImage.setImageResource(android.R.color.transparent);

        // populate the thumbnail image
        // remote download the image in the background

            Glide
                    .with(getContext())
                    .load(tweet.getUser().getProfileImageUrl())
                    .bitmapTransform(new RoundedCornersTransformation(mContext, 4, 0))
                    .placeholder(R.drawable.ic_launcher)
                    .crossFade()
                    .into(ivProfileImage);


    }

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