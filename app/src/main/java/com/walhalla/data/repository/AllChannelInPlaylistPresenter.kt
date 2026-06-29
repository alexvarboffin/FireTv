package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import com.walhalla.data.model.Channel
import tv.hdonlinetv.besttvchannels.movies.watchfree.model.CategoryUI

class AllChannelInPlaylistPresenter(handler: Handler, context: Context) :
    BasePresenter(handler, context) {
    fun getChannelsInPlaylist(
        playlistId: Long,
        sortOption: Int,
        callback: RepoCallback<List<Channel>>
    ) {
        executeInBackground {
            try {
                val channels: MutableList<Channel> =
                    db_repo.getChannelsInPlaylist(playlistId, sortOption)
                postToMainThread { callback.successResult(channels) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message!!) }
            }
        }
    }

    fun getCategoriesForPlaylist(
        playlistId: Long,
        callback: RepoCallback<List<CategoryUI>>
    ) {
        executeInBackground {
            try {
                val www: MutableList<CategoryUI> = ArrayList()
                val playlist = db_repo.getCategoriesForPlaylist(playlistId)
                for (category in playlist) {
                    val categoryUI = CategoryUI(
                        category.name,
                        category.desc,
                        category.thumb
                    )
                    www.add(categoryUI)
                }
                postToMainThread { callback.successResult(www) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message!!) }
            }
        }
    }
}
