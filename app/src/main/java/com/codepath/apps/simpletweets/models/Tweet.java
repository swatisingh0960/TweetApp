package com.codepath.apps.simpletweets.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Swati on 10/30/2016.
 */
/*
    {"text": "just another test",
    "contributors": null,
    "id": 240558470661799936,
    "retweet_count": 0,
    "in_reply_to_status_id_str": null,
    "geo": null,
    "retweeted": false,
    "in_reply_to_user_id": null,
    "place": null,
    "source": "OAuth Dancer Reborn",
    "user": {
      "name": "OAuth Dancer",
      "profile_sidebar_fill_color": "DDEEF6",
      "profile_background_tile": true,
      "profile_sidebar_border_color": "C0DEED",
      "profile_image_url": "http://a0.twimg.com/profile_images/730275945/oauth-dancer_normal.jpg",
      "created_at": "Wed Mar 03 19:37:35 +0000 2010",
      "location": "San Francisco, CA",
      "follow_request_sent": false,
      "id_str": "119476949",
      "is_translator": false,
      "profile_link_color": "0084B4"}

 */
// Parse JSON, Pass data, Encapsulate state logic
@Table(name = "tweets")
@Parcel(analyze={Tweet.class})   // add Parceler to ignore Model
public class Tweet extends Model {

    @Column(name = "body")
    public String body;
    @Column(name = "tid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public long tid;
    @Column(name = "retweet_count")
    public int retweetCount;
    @Column(name = "favorite_count")
    public int favoriteCount;
    @Column(name = "user", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public User user;  // embedded User object
    @Column(name = "created_at")
    public String createdAt;
    @Column(name = "user_mentions")
    public String[] userMentions;
    public List<Medium> media;
    @Column(name = "in_reply_to_status_id")
    public String inReplyToStatusId;
    @Column(name = "favorited")
    public boolean favorited;
    @Column(name = "retweeted")
    public boolean retweeted;
    @Column(name = "retweeted_status")
    public Tweet retweetedStatus;

    public Tweet() {
        super();
    }

    public String getBody() {
        return body;
    }

    public long getTid() {
        return tid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public String[] getUserMentions() {
        return userMentions;
    }

    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
        if (favorited) {
            favoriteCount++;
        } else {
            favoriteCount--;
        }
    }

            public void setRetweeted(boolean retweeted) {
                this.retweeted = retweeted;
                if (retweeted) {
                    retweetCount++;
                } else {
            retweetCount--;
        }
    }

    public Tweet getRetweetedStatus() {
        return retweetedStatus;
    }

    public List<Medium> getMedia() {
        return media;
    }

    // Finds existing retweeted status based on tid or creates new retweeted status and returns
    public static Tweet findOrCreateFromJson(JSONObject json) {
        long tId = 0; // get just the remote id
        Tweet tweet = null;

        try {
            tId = json.getLong("id");
            // Search for duplicate
            tweet = new Select().from(Tweet.class).where("tid = ?", tId).executeSingle();
            if (tweet == null) {
                // create and return new user
                tweet = Tweet.fromJSONObject(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tweet;
    }

    // Deserialize JSONObject and build Tweet object
    // Twitter.fromJSONObject("{...}") => <Tweet>
    public static Tweet fromJSONObject(JSONObject jsonObject) {
        Tweet tweet = new Tweet();

        try {
            tweet.body = jsonObject.getString("text");
            tweet.tid = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.retweetCount = jsonObject.getInt("retweet_count");
            tweet.favoriteCount = jsonObject.getInt("favorite_count");
            tweet.user = User.findOrCreateFromJson(jsonObject.getJSONObject("user"));
            // Get hashtags
            JSONArray mentions = jsonObject.getJSONObject("entities").getJSONArray("user_mentions");
            tweet.userMentions = new String[mentions.length()];
            for (int i = 0; i < mentions.length(); i++) {
                tweet.userMentions[i] = mentions.getJSONObject(i).getString("screen_name");
            }
            // Get in_reply_to_status_id
            tweet.inReplyToStatusId = jsonObject.getString("in_reply_to_status_id_str");
            tweet.favorited = jsonObject.getBoolean("favorited");
            tweet.retweeted = jsonObject.getBoolean("retweeted");
            // Get retweeted_status
            if (jsonObject.has("retweeted_status")) {
                tweet.retweetedStatus = Tweet.findOrCreateFromJson(jsonObject.getJSONObject("retweeted_status"));
            }

            // Save Tweet before saving Mdeium
            tweet.save();

            // Get media
            if (jsonObject.has("extended_entities")) {
                JSONObject extendedEntities = jsonObject.getJSONObject("extended_entities");
                if (extendedEntities != null) {
                    JSONArray media = extendedEntities.getJSONArray("media");
                    if (media != null) {
                        tweet.media = Medium.fromJSONArray(media, tweet);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tweet;
    }

    // Deserialize JSONArray and build Tweet objects
    // Twitter.fromJSONArray("{...}") => ArrayList<Tweet>
    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Tweet tweet = fromJSONObject(jsonObject);
                if (tweet != null) {
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return tweets;
    }
}
