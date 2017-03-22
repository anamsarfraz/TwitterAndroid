package com.codepath.apps.twitter.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Locale;


public class DateUtil {
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";

        long dateMillis = 0;
        try {
            dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String[] timeComponents = relativeDate.split(" ");


        if (relativeDate.equals(Constants.YESTERDAY_STR)) {
            return Constants.ONE_DAY_STR;
        } else {
            return String.format("%s%c", timeComponents[0], timeComponents[1].charAt(0));
        }
    }
}
