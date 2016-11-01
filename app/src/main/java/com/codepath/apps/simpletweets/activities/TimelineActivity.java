package com.codepath.apps.simpletweets.activities;

import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.adapters.ContactsAdapter;
import com.codepath.apps.simpletweets.fragments.ComposeDialogFragment;
import com.codepath.apps.simpletweets.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.apps.simpletweets.models.Tweet;
import com.codepath.apps.simpletweets.models.User;
import com.codepath.apps.simpletweets.others.HelperMethods;
import com.codepath.apps.simpletweets.others.ItemClickSupport;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity
        implements ComposeDialogFragment.ComposeDialogListener {

    private TwitterClient client;
    private List<Tweet> tweets;
    public static ContactsAdapter adapter;
    LinearLayoutManager mLinearLayoutManager;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rvTweets) RecyclerView rvTweets;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.fab) FloatingActionButton fab;
    final int TAP_THRESHOLD = 4000;
    long timeStamp;
    public static final int REQUEST_COMPOSE = 20;
    public static final int REQUEST_DETAILS = 22;
    public static User ACCOUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // Bind views
        ButterKnife.bind(this);
        // Set Toolbar as Actionbar
        setSupportActionBar(toolbar);

        // Get saved Tweets from DB
        if (!HelperMethods.isConnected(getApplicationContext())) {
            tweets = ContactsAdapter.getAll();
        } else {
            tweets = new ArrayList<>();
            ContactsAdapter.clearAll();
        }
        // Contact adapter from data source
        adapter = new ContactsAdapter(this, tweets);
        // Construct the adapter from data source
        rvTweets.setAdapter(adapter);
        // Create layout manager
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // Contact layout manager
        rvTweets.setLayoutManager(mLinearLayoutManager);

        // Get the client
        client = TwitterApplication.getRestClient();    // singleton client
        // Populate new data
        if (HelperMethods.isNetworkAvailable(this) && HelperMethods.isOnline()) {
            populateTimeline(null, null);
        }

        // Set endless scroll
        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                populateTimeline(null, tweets.get(totalItemsCount - 1).getTid() - 1);
            }
        });

        // Add item click feature to RecyclerView
        ItemClickSupport.addTo(rvTweets).setOnItemClickListener(
            new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    // Go to detail activity
                    Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                    intent.putExtra("tweet", Parcels.wrap(tweets.get(position)));
                    intent.putExtra("position", position);
                    startActivityForResult(intent, REQUEST_DETAILS);
                }
            }
        );

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Check connection
                if (HelperMethods.isConnected(getApplicationContext())) {
                    // Clear existing data
                    adapter.clear();
                    // Populate new data
                    populateTimeline(null, null);
                } else {
                    swipeContainer.setRefreshing(false);
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        getAccount();
    }

    private void getAccount() {
        client.getAccount(new JsonHttpResponseHandler(){
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                // Find account or create and save
                ACCOUNT = User.findOrCreateFromJson(jsonObject);
            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                JSONObject errorResponse) {
                if (errorResponse != null) {
                    Log.d("DEBUG", "Verify credentials error: " + errorResponse.toString());
                }
            }
        });
    }

    // Sent API request to get Timeline JSON
    // Fill the listView by creating Tweet object from JSON
    // since: id > since (newer)
    // max: id <= max (older)
    private void populateTimeline(Long since, Long max) {
        client.getHomeTimeline(since, max, new JsonHttpResponseHandler(){
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                ArrayList<Tweet> newTweets = Tweet.fromJSONArray(jsonArray);
                if (newTweets != null) {
                    adapter.addAll(newTweets);
                    if (swipeContainer != null) {
                        // Disable refreshing icon
                        swipeContainer.setRefreshing(false);
                    }
                }
            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                JSONObject errorResponse) {
                Log.d("DEBUG", "Fetch timeline error: " + errorResponse.toString());
            }
        });
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void composeTweet() {
        ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance(REQUEST_COMPOSE, null);
        composeDialogFragment.show(getFragmentManager(), "fragment_compose");
    }

    @Override
    public void onFinishComposeDialog(int requestCode, Tweet tweet) {
        if (requestCode == REQUEST_COMPOSE) {
            postTweet(tweet);
        } else if (requestCode == DetailsActivity.REQUEST_REPLY) {
            HelperMethods.postTweet(tweet);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_DETAILS && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            Tweet updatedTweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update Tweet
            tweets.set(position, updatedTweet);
            // Update position
            adapter.update(position);
        }
    }

    private void postTweet(Tweet tweet) {
        client.composeTweet(tweet, new JsonHttpResponseHandler(){
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Tweet newTweet = Tweet.fromJSONObject(jsonObject);
                if (newTweet != null) {
                    // Add new Tweet to top without refreshing
                    adapter.add(0, newTweet);
                }
            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                JSONObject errorResponse) {
                if (errorResponse != null) {
                    Log.d("DEBUG", "Compose Tweet Error: " + errorResponse.toString());
                }
            }
        });
    }

    // double tap to scroll to top
    @OnClick({R.id.toolbar, R.id.fab})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar:
                long newTimeStamp = System.currentTimeMillis();
                if (newTimeStamp - timeStamp <= TAP_THRESHOLD && tweets.size() != 0) {
                    mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
                }
                timeStamp = newTimeStamp;
                break;
            case R.id.fab:
                composeTweet();
                break;
        }
    }
}
