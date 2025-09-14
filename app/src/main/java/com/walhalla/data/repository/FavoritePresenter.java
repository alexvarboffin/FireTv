package com.walhalla.data.repository;

import android.content.Context;
import android.os.Handler;

import com.walhalla.data.model.Channel;

import java.util.List;

public class FavoritePresenter extends BasePresenter {

    public FavoritePresenter(Handler handler, Context context) {
        super(handler, context);
    }

    public void getFavorite(long playlistId, RepoCallback<List<Channel>> callback) {
        executeInBackground(() -> {
            try {
                List<Channel> channels = db_repo.getFavorite(playlistId);
                postToMainThread(() -> callback.successResult(channels));
            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }
}
