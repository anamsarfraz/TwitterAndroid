package com.codepath.apps.twitter.models;

import com.codepath.apps.twitter.databases.TwitterDatabase;
import com.codepath.apps.twitter.util.Constants;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.parceler.Parcel;

import java.util.List;


@Table(database = TwitterDatabase.class)
@Parcel(analyze={Draft.class})
public class Draft extends BaseModel {

    @PrimaryKey(autoincrement = true)
    @Column
    int uid;

    @Column
    String draft;

    public Draft() {
        super();
    }

    public Draft(String draft) {
        this.draft = draft;
    }

    public static void saveDraft(String draft) {
        new Draft(draft).save();
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getDraft() {
        return draft;
    }

    public void setDraft(String draft) {
        this.draft = draft;
    }

    public static List<Draft> getDrafts() {
        return new Select()
                .from(Draft.class)
                .orderBy(Draft_Table.uid, true)
                .queryList();
    }

    public static long getDraftCount() {
        return new Select(Method.count()).from(Draft.class).count();
    }
}
