package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.walhalla.data.model.Channel
import com.walhalla.data.model.PlaylistImpl
import com.walhalla.data.repository.M3UParser.parseM3U
import com.walhalla.ui.DLog.handleException
import okhttp3.Request
import tv.hdonlinetv.besttvchannels.movies.watchfree.NetworkUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.BasePresenter0
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView
import java.io.IOException

class PlaylistPresenterImpl(handler: Handler, view: PlaylistManagementView, context: Context) :
    BasePresenter0(handler, view, context) {
    fun loadAndSavePlaylist(playlist: PlaylistImpl) {
        loadNewPlayList(playlist)
    }

    fun loadNewPlayList(oldValue: PlaylistImpl) {
        val playlistLink = oldValue.fileName
        val playListName: String? = oldValue.title

        this.client = NetworkUtils.makeOkhttp()
        val request = Request.Builder()
            .url(playlistLink)
            .build()

        executeInBackground(Runnable {
            try {
                // Выполнение синхронного запроса
                val response = client.newCall(request).execute()

                // Обработка ответа
                if (!response.isSuccessful) {
                    // Вызов в главном потоке для показа сообщения
                    postToMainThread(Runnable {
                        view.hideProgressBar()
                        view.showToast(R.string.error_download_failed)
                    })
                    return@Runnable
                }

                val fileContent: String?
                val body = response.body
                if (body != null) {
                    try {
                        fileContent = body.string()
                        val result: List<Channel> = parseM3U(context, fileContent)
                        handleResult0(result, oldValue)
                    } catch (e: IOException) {
                        handleException(e)
                        // Вызов в главном потоке для показа сообщения об ошибке
                        postToMainThread(Runnable { view.showToast(R.string.error_download_failed) })
                    }
                }
            } catch (e: IOException) {
                handleException(e)
                // Вызов в главном потоке для показа сообщения об ошибке
                postToMainThread(Runnable { view.showToast(R.string.error_download_failed) })
            } finally {
                // Скрыть прогресс бар независимо от результата
                postToMainThread(Runnable { view.hideProgressBar() })
            }
        })
    }

    private fun handleResult0(channels: List<Channel>, oldValue: PlaylistImpl) {
        if (channels.isEmpty()) {
            postToMainThread(Runnable {
                Toast.makeText(context, R.string.no_lines_found, Toast.LENGTH_SHORT).show()
            })
        } else {
            //            List<Long> oldIds = new ArrayList<>();
//            //old_ids
//            for (Channel channel : channels) {
//                oldIds.add(channel._id);
//            }

            val m = db_repo.deletePlaylistAndRelatedChannels(oldValue)
            if (m > 0) {
                val newValue = PlaylistImpl(
                    oldValue.title,
                    oldValue.fileName,
                    oldValue.importDate,
                    oldValue.count,
                    oldValue.autoUpdate,
                    TypeUtils.TYPE_M3U_CLOUD
                )

                newValue.updateDate = DateFormatUtils.importDate()
                newValue.count = -1
                db_repo.addChannelAndPlaylist(channels, newValue)

                //view.updatePlaylistContent(fileContent);
                postToMainThread(Runnable {
                    view.onPlaylistUpdated()
                    view.showToast(R.string.download_successful)
                })
            }
        }
    }

    fun selectAllPlaylist(callback: RepoCallback<List<PlaylistImpl>>) {
        executeInBackground(Runnable {
            val playlists = db_repo.selectAllPlaylist()
            postToMainThread(Runnable {
                callback.successResult(playlists)
            })
        })
    }

    fun deletePlaylistAndRelatedChannels(playlist: PlaylistImpl, callback: RepoCallback<Int>) {
        executeInBackground(Runnable {
            try {
                val k = db_repo.deletePlaylistAndRelatedChannels(playlist)
                postToMainThread(Runnable {
                    callback.successResult(k)
                })
            } catch (e: Exception) {
                postToMainThread(Runnable {
                    callback.errorResult(e.message!!)
                })
            }
        })
    }
}
