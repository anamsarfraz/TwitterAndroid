package com.codepath.apps.twitter.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.text.format.DateUtils;
import java.util.Date;

import java.util.Locale;

import static android.R.id.input;


public class DateUtil {
    static final String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    public static String getRelativeTimeAgo(String rawJsonDate) {

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

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(twitterFormat);
        return sdf.format(new Date());
    }

    public static String getDateTimeInFormat(String inputStrDate, String outputFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(twitterFormat);
        try {
            Date inputDate = sdf.parse(inputStrDate);
            SimpleDateFormat osdf = new SimpleDateFormat(outputFormat);
            return osdf.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

}
