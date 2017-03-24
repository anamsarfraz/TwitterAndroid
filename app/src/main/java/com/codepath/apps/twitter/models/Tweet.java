package com.codepath.apps.twitter.models;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.codepath.apps.twitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitter.databases.TwitterDatabase;
import com.codepath.apps.twitter.util.Constants;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.CurrentItemMetaData;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;
import com.volokh.danylo.visibility_utils.items.ListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.Transient;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;
import static android.content.ContentValues.TAG;


@Table(database = TwitterDatabase.class)
@Parcel(analyze={Tweet.class})
public class Tweet extends BaseModel implements ListItem {
    private final Rect mCurrentViewRect = new Rect();
    @Transient
    private final VideoPlayerManager<MetaData> videoPlayerManager;

	@PrimaryKey
	@Column
	Long uid;

    @Column
    String idStr;

	// Define table fields
	@Column
    @ForeignKey(saveForeignKeyModel = true)
	User user;

    @Column
    String createdAt;

    @Column
	String body;

    @Column
    @ForeignKey(saveForeignKeyModel = true)
    Tweet retweetedStatus;

    @Column
    @ForeignKey(saveForeignKeyModel = true)
    Media media;



	public Tweet() {
		super();
        videoPlayerManager = null;
    }

	// Parse model from JSON
	public Tweet(JSONObject jsonObject, VideoPlayerManager videoPlayerManager){
		super();

        this.videoPlayerManager = videoPlayerManager;
		try {
            this.uid = jsonObject.getLong("id");
            this.idStr = jsonObject.getString("id_str");
            this.user = new User(jsonObject.getJSONObject("user"));
            this.body = jsonObject.getString("text");
            this.createdAt = jsonObject.getString("created_at");
            JSONObject retweetedStatus = jsonObject.optJSONObject("retweeted_status");
            if (retweetedStatus != null) {
                this.retweetedStatus = new Tweet(retweetedStatus, this.videoPlayerManager);
            }
            JSONObject entities = jsonObject.optJSONObject("entities");
            JSONObject extendedEntities = jsonObject.optJSONObject("extended_entities");
            JSONArray mediaArray = null;
            this.media = null;
            if (entities != null) {
                mediaArray = entities.optJSONArray("media");
                if (mediaArray != null) {
                    this.media = new Media(mediaArray, extendedEntities);
                }

            }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// Getters and setters


	public Long getUid() {
		return uid;
	}

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

    public Tweet getRetweetedStatus() {
        return retweetedStatus;
    }

    public void setRetweetedStatus(Tweet retweetedStatus) {
        this.retweetedStatus = retweetedStatus;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray, VideoPlayerManager videoPlayerManager) {
        ArrayList<Tweet> tweets = new ArrayList<>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = new Tweet(tweetJson, videoPlayerManager);
            tweet.save();
            tweets.add(tweet);
        }

        return tweets;
    }
	
	// Record Finders
	public static Tweet byId(long uid) {
		return new Select().from(Tweet.class).where(Tweet_Table.uid.eq(uid)).querySingle();
	}

	public static List<Tweet> recentItems(long maxId) {
        Condition condition = maxId <= 0 ? Tweet_Table.uid.greaterThan(maxId) : Tweet_Table.uid.lessThanOrEq(maxId);
		return new Select()
                .from(Tweet.class)
                .where(condition)
                .orderBy(Tweet_Table.uid, false)
                .limit(Constants.MAX_TWEET_COUNT)
                .queryList();
	}

    @Override
    public int getVisibilityPercents(View currentView) {

        int percents = 100;

        currentView.getLocalVisibleRect(mCurrentViewRect);
        int height = currentView.getHeight();

        if (viewIsPartiallyHiddenTop()) {
            // view is partially hidden behind the top edge
            percents = (height - mCurrentViewRect.top) * 100 / height;
        } else if (viewIsPartiallyHiddenBottom(height)) {
            percents = mCurrentViewRect.bottom * 100 / height;
        }


        return percents;

    }

    private boolean viewIsPartiallyHiddenBottom(int height) {
        return mCurrentViewRect.bottom > 0 && mCurrentViewRect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop() {
        return mCurrentViewRect.top > 0;
    }

    @Override
    public void setActive(View newActiveView, int newActiveViewPosition) {
        if (this.media != null && this.media.getType().equals(Constants.VIDEO_STR)) {
            TweetsArrayAdapter.ViewHolder viewHolder = new TweetsArrayAdapter.ViewHolder(newActiveView);
            playNewVideo(new CurrentItemMetaData(newActiveViewPosition, newActiveView), viewHolder.getVvMultiMedia(), videoPlayerManager);
        }

    }

    @Override
    public void deactivate(View currentView, int position) {

    }

    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player, VideoPlayerManager<MetaData> videoPlayerManager) {
        Log.d("DEBUG", "PLAY NEW VIDEO");
        videoPlayerManager.playNewVideo(currentItemMetaData, player, this.media.getVideoUrl());
    }

    public void stopPlayback(VideoPlayerManager videoPlayerManager) {
        Log.d("DEBUG", "STOP PLAYBACK");
        videoPlayerManager.stopAnyPlayback();
    }
}
