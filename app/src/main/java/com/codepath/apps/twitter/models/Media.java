package com.codepath.apps.twitter.models;

import com.codepath.apps.twitter.databases.TwitterDatabase;
import com.codepath.apps.twitter.util.Constants;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;


@Table(database = TwitterDatabase.class)
@Parcel(analyze={Media.class})
public class Media extends BaseModel {
    @PrimaryKey
    @Column
    Long uid;

    @Column
    String idStr;

    @Column
    String type;

    @Column
    String imageUrl;

    @Column
    String videoUrl;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String url) {
        this.imageUrl = url;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Media() {
        super();
    }

    // Parse model from JSON
    public Media(JSONArray mediaArray, JSONObject extendedEntities){
        super();

        try {
            JSONObject mediaObj = mediaArray.getJSONObject(0);
            this.uid = mediaObj.getLong("id");
            this.idStr = mediaObj.getString("id_str");
            this.imageUrl = mediaObj.getString("media_url");

            if (extendedEntities != null) {
                JSONObject extendedMedia = extendedEntities
                        .optJSONArray("media")
                        .getJSONObject(0);
                this.type = extendedMedia.getString("type");
                if (this.type.equals(Constants.VIDEO_STR)) {
                    this.videoUrl = extendedMedia
                            .getJSONObject("video_info")
                            .getJSONArray("variants")
                            .getJSONObject(0)
                            .getString("url");
                }
            } else {
                this.type = mediaObj.getString("type");
            }
            if (!this.type.equals(Constants.VIDEO_STR)) {
                this.videoUrl = null;
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }


}
