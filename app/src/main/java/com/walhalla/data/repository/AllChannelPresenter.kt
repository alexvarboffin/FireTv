package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import com.walhalla.data.model.Channel
import kotlinx.coroutines.runBlocking
import tv.hdonlinetv.compose.core.databridge.channel.ChannelMapper
import tv.hdonlinetv.compose.core.databridge.channel.LocalChannelRepository

/**
 * Legacy MVP presenter — delegates to shared [LocalChannelRepository].
 * Used by legacy :app activities; Compose uses channel ViewModels directly.
 */
class AllChannelPresenter(handler: Handler, context: Context) : BasePresenter(handler, context) {

    private val repository = LocalChannelRepository(context.applicationContext)

    fun getAllChannels(sortOption: Int, callback: RepoCallback<List<Channel>>) {
        executeInBackground {
            try {
                val channels = runBlocking {
                    repository.getAllChannels(sortOption)
                }.map { ui -> ChannelMapper.toData(ui) }
                postToMainThread { callback.successResult(channels) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message ?: "error") }
            }
        }
    }

    fun getChannelById(id: Long, callback: RepoCallback<Channel>) {
        executeInBackground {
            try {
                val channel = runBlocking { repository.getChannelById(id) }
                if (channel != null) {
                    postToMainThread { callback.successResult(ChannelMapper.toData(channel)) }
                } else {
                    postToMainThread { callback.errorResult("not found") }
                }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message ?: "error") }
            }
        }
    }

    fun getChannelsInCategory(categoryName: String, callback: RepoCallback<List<Channel>>) {
        executeInBackground {
            try {
                val channels = runBlocking {
                    repository.getChannelsInCategory(categoryName)
                }.map { ui -> ChannelMapper.toData(ui) }
                postToMainThread { callback.successResult(channels) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message ?: "error") }
            }
        }
    }

    fun isFavoriteChannel(channel: Channel, callback: RepoCallback<Boolean?>) {
        executeInBackground {
            try {
                val isFavorite = runBlocking { repository.isFavorite(channel._id) }
                postToMainThread { callback.successResult(isFavorite) }
            } catch (e: Exception) {
                postToMainThread {
                    callback.errorResult(e.message ?: "error")
                    callback.successResult(false)
                }
            }
        }
    }

    fun addFavorite(channel: Channel, callback: RepoCallback<Int>) {
        executeInBackground {
            try {
                runBlocking { repository.setFavorite(channel._id, true) }
                postToMainThread { callback.successResult(1) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message ?: "error") }
            }
        }
    }

    fun deleteFavorite(channel: Channel) {
        executeInBackground {
            try {
                runBlocking { repository.setFavorite(channel._id, false) }
            } catch (_: Exception) {
            }
        }
    }

    fun getAllFavorite(callback: RepoCallback<List<Channel>>) {
        executeInBackground {
            try {
                val channels = runBlocking {
                    repository.getFavorites()
                }.map { ui -> ChannelMapper.toData(ui) }
                postToMainThread { callback.successResult(channels) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message ?: "error") }
            }
        }
    }

    fun searchChannel(categoryName: String, callback: RepoCallback<List<Channel>>) {
        executeInBackground {
            try {
                val channels = runBlocking {
                    repository.search(categoryName)
                }.map { ui -> ChannelMapper.toData(ui) }
                postToMainThread { callback.successResult(channels) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message ?: "error") }
            }
        }
    }
}
