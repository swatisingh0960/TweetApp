package com.codepath.apps.simpletweets.adapters;

import android.databinding.BindingAdapter;
import android.net.ParseException;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.others.ParseRelativeDate;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
/**
 * Created by Swati on 10/31/2016.
 */

public class BindingAdapterUtils {
    @BindingAdapter({"bind:profileImageUrl"})
    public static void loadImage(ImageView view, String url) {
        Glide.with(view.getContext())
            .load(url)
            .placeholder(R.drawable.ic_launcher)
            .bitmapTransform(new RoundedCornersTransformation(view.getContext(), 5, 0))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view);
    }

    @BindingAdapter({"bind:relativeTime"})
    public static void loadRelativeTime(TextView view, String data) {
        String time = ParseRelativeDate.getRelativeTimeAgo(data);
        view.setText(time);
    }

    @BindingAdapter({"bind:timeFormat"})
    public static void loadTime(TextView view, String data) {
        SimpleDateFormat input = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        SimpleDateFormat output = new SimpleDateFormat("HH:mm aa - dd MMM yy");
        String result = null;
        try {
            Date oneWayTripDate = input.parse(data);                 // parse input
            result = output.format(oneWayTripDate);    // format output
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        view.setText(result);
    }
}
