package com.codepath.apps.simpletweets.fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.activities.DetailsActivity;
import com.codepath.apps.simpletweets.activities.TimelineActivity;
import com.codepath.apps.simpletweets.models.Tweet;
import com.codepath.apps.simpletweets.others.HelperMethods;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
/**
 * Created by Swati on 10/31/2016.
 */

public class ComposeDialogFragment extends DialogFragment {

    @BindView(R.id.etContent) EditText etContent;
    @BindView(R.id.btnCompose) Button btnCompose;
    @BindView(R.id.tvAvailableChars) TextView tvAvailableChars;
    @BindView(R.id.ivProfile) ImageView ivProfile;
    @BindView(R.id.ivNavigationUp) ImageView ivNavigationUp;
    @BindView(R.id.tvNotice) TextView tvNotice;
    @BindView(R.id.tvScreenName) TextView tvScreenName;
    @BindView(R.id.tvUsername) TextView tvUsername;
    private Unbinder unbinder;
    private Tweet tweet;
    private Tweet retweetedStatus;
    private Tweet targetTweet;
    private int requestCode;
    Activity activity;

    // define listener to pass setting to activity
    public interface ComposeDialogListener {
        void onFinishComposeDialog(int requestCode, Tweet tweet);
    }

    public ComposeDialogFragment() {}


    public static ComposeDialogFragment newInstance(int requestCode, Tweet tweet) {
        // pass setting to fragment
        ComposeDialogFragment composeDialogFragment = new ComposeDialogFragment();
        Bundle args = new Bundle();
        args.putInt("request_code", requestCode);
        if (tweet != null) {
            args.putParcelable("tweet", Parcels.wrap(tweet));
        }
        composeDialogFragment.setArguments(args);
        return composeDialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container);
        // bind fragment with ButterKnife
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        activity = getActivity();

        // Show fake toolbar
        Glide.with(activity).load(TimelineActivity.ACCOUNT.getProfileImageUrl())
            .placeholder(R.drawable.ic_launcher)
            .bitmapTransform(new RoundedCornersTransformation(view.getContext(), 5, 0))
            .into(ivProfile);

        tvUsername.setText(TimelineActivity.ACCOUNT.getName());
        tvScreenName.setText("@" + TimelineActivity.ACCOUNT.getScreenName());

        // Get Tweet and request code
        requestCode = getArguments().getInt("request_code");

        // Put hashtags before input
        if (requestCode == DetailsActivity.REQUEST_REPLY) {
            tweet = Parcels.unwrap(getArguments().getParcelable("tweet"));
            retweetedStatus = tweet.getRetweetedStatus();
            targetTweet = retweetedStatus != null ? retweetedStatus : tweet;
            // @ status user screen name
            StringBuilder sb = new StringBuilder("@")
                .append(targetTweet.getUser().getScreenName())
                .append(' ');
            // @ retweet user's screen name
            if (retweetedStatus != null) {
                sb.append('@').append(tweet.getUser().getScreenName()).append(' ');
            }
            // @ all mentioned user
            for (String screenName : targetTweet.getUserMentions()) {
                if (!screenName.equals(targetTweet.getUser().getScreenName())) {
                    sb.append("@").append(screenName).append(' ');
                }
            }

            etContent.setText(sb.toString());

            // Move cursor to end of reply EditText
            etContent.setSelection(sb.length());

            // Set initial remaining char count
            tvAvailableChars.setText(String.valueOf(140 - etContent.length()));

            // Set notice TextView
            tvNotice.setVisibility(View.VISIBLE);
            tvNotice.setText("Replying to " + targetTweet.getUser().getName());
        }

        // Set text change listener
        HelperMethods.setTextChangedListener(activity, etContent, tvAvailableChars, btnCompose);
    }

    @OnClick({R.id.btnCompose, R.id.ivNavigationUp})
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnCompose:
                if (etContent.getText().length() <= 0) {    // No input
                    dismiss();
                }

                Tweet newTweet = new Tweet();
                newTweet.body = etContent.getText().toString();
                newTweet.user = TimelineActivity.ACCOUNT;
                if (tweet != null && requestCode == DetailsActivity.REQUEST_REPLY) {
                    newTweet.inReplyToStatusId = String.valueOf(tweet.getTid());
                }

                // pass setting to activity via listener
                ComposeDialogListener composeDialogListener = (ComposeDialogListener) getActivity();
                composeDialogListener.onFinishComposeDialog(requestCode, newTweet);
                // close fragment
                dismiss();
                break;
            case R.id.ivNavigationUp:
                dismiss();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind frrament and ButterKnife
        unbinder.unbind();
    }
}
