package com.codepath.apps.simpletweets.others;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;
/**
 * Created by Swati on 10/31/2016.
 */

public class ParseRelativeDate {
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = 0;
            dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        int index = 0;
        while (index < relativeDate.length() && relativeDate.charAt(index) != ' ') {
            index++;
        }
        return relativeDate.substring(0, index + 2);
    }
}
