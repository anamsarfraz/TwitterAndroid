package com.codepath.apps.twitter.models;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.codepath.apps.twitter.databases.TwitterDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.Parcels;
import org.parceler.Transient;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;


@Table(database = TwitterDatabase.class)
@Parcel(analyze={User.class})
public class User extends BaseModel {

    @Transient
    static User currentUser = null;

    @Transient
    final static SharedPreferences pref =
            PreferenceManager.getDefaultSharedPreferences(getContext());

    // Define table fields
    @PrimaryKey
    @Column
    long uid;

    @Column
    String
    idStr;

    @Column
    String name;

    @Column
    String screenName;

    @Column
    String profileImageUrl;

    @Column
    boolean verified;

    // Getters and setters


    public static User getCurrentUser() {
        if (currentUser != null) {
            return currentUser;
        }

        long currentUserId = pref.getLong("currentUserId", -1);
        currentUser = byId(currentUserId);

        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
        Editor edit = pref.edit();
        currentUser.save();
        edit.putLong("currentUserId", currentUser.uid);
        edit.apply();
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }


    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public User() {
        super();
    }

    // Parse model from JSON
    public User(JSONObject jsonObject){
        super();

        try {
            this.uid = jsonObject.getLong("id");
            this.idStr = jsonObject.getString("id_str");
            this.name = jsonObject.getString("name");
            this.screenName = jsonObject.getString("screen_name");
            this.profileImageUrl = jsonObject.getString("profile_image_url");
            this.verified = jsonObject.getBoolean("verified");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static User byId(long uid) {
        return new Select().from(User.class).where(User_Table.uid.eq(uid)).querySingle();
    }
}
