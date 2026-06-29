package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import com.walhalla.data.model.Channel

class FavoritePresenter(handler: Handler, context: Context) : BasePresenter(handler, context) {
    fun getFavorite(playlistId: Long, callback: RepoCallback<List<Channel>>) {
        executeInBackground(Runnable {
            try {
                val channels: List<Channel> = db_repo.getFavorite(playlistId)
                postToMainThread { callback.successResult(channels) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message!!) }
            }
        })
    }
}
