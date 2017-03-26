package com.codepath.apps.twitter.databases;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = TwitterDatabase.NAME, version = TwitterDatabase.VERSION)
public class TwitterDatabase {

    public static final String NAME = "RestClientDatabase";

    public static final int VERSION = 2;
}
