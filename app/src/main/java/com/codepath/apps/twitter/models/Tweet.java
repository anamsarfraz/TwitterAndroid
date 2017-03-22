package com.codepath.apps.twitter.models;

import com.codepath.apps.twitter.databases.TwitterDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;


@Table(database = TwitterDatabase.class)
@Parcel(analyze={Tweet.class})
public class Tweet extends BaseModel {

	@PrimaryKey
	@Column
	Long uid;

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


	public Tweet() {
		super();
	}

	// Parse model from JSON
	public Tweet(JSONObject jsonObject){
		super();

		try {
            this.uid = jsonObject.getLong("id");
            this.user = new User(jsonObject.getJSONObject("user"));
            this.body = jsonObject.getString("text");
            this.createdAt = jsonObject.getString("created_at");
            JSONObject retweetedStatus = jsonObject.optJSONObject("retweeted_status");
            if (retweetedStatus != null) {
                this.retweetedStatus = new Tweet(retweetedStatus);
            } else {
                this.retweetedStatus = null;
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

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = new Tweet(tweetJson);
            tweet.save();
            tweets.add(tweet);
        }

        return tweets;
    }
	
	// Record Finders
	public static Tweet byId(long uid) {
		return new Select().from(Tweet.class).where(Tweet_Table.uid.eq(uid)).querySingle();
	}

	public static List<Tweet> recentItems() {
		return new Select().from(Tweet.class).orderBy(Tweet_Table.uid, false).limit(300).queryList();
	}
}
