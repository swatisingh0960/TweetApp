package com.codepath.apps.simpletweets.models;

import android.support.v7.widget.RecyclerView;

import com.codepath.apps.simpletweets.databinding.ItemTweetBinding;

/**
 * Created by Swati on 10/30/2016.
 */

public class ViewHolder extends RecyclerView.ViewHolder {

    ItemTweetBinding binding;

    public ViewHolder(ItemTweetBinding binding) {
        // Stores the itemView in a public final member variable that can be used
        // to access the context from any ViewHolder instance.
        super(binding.getRoot());
        this.binding = binding;
    }

    public ItemTweetBinding getBinding() {
        return binding;
    }
}
