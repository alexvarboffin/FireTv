package com.walhalla.data.repository;

import android.content.Context;
import android.os.Handler;

import com.walhalla.data.model.Category;
import com.walhalla.data.model.Channel;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.model.CategoryUI;

public class AllChannelInPlaylistPresenter extends BasePresenter {

    public AllChannelInPlaylistPresenter(Handler handler, Context context) {
        super(handler, context);
    }

    public void getChannelsInPlaylist(long playlistId, int sortOption, RepoCallback<List<Channel>> callback) {
        executeInBackground(() -> {
            try {
                List<Channel> channels = db_repo.getChannelsInPlaylist(playlistId, sortOption);
                postToMainThread(() -> callback.successResult(channels));
            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }

    public void getCategoriesForPlaylist(long playlistId, RepoCallback<List<CategoryUI>> callback) {
        executeInBackground(() -> {
            try {
                List<CategoryUI> www = new ArrayList<>();
                List<Category> playlist = db_repo.getCategoriesForPlaylist(playlistId);
                for (Category category : playlist) {
                    CategoryUI categoryUI = new CategoryUI(
                            category.name,
                            category.desc,
                            category.thumb
                    );
                    www.add(categoryUI);
                }
                postToMainThread(() -> callback.successResult(www));
            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }
}
