package com.walhalla.data.repository

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.walhalla.data.model.Channel
import com.walhalla.data.model.PlaylistImpl
import com.walhalla.data.repository.M3UParser.parseM3U
import com.walhalla.ui.DLog.d
import com.walhalla.ui.DLog.handleException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import tv.hdonlinetv.besttvchannels.movies.watchfree.NetworkUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.BasePresenter0
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class PlaylistManagementPresenterImpl(
    handler: Handler?,
    view: PlaylistManagementView?,
    context: Context?
) : BasePresenter0(handler, view, context) {
    fun onSubscribeFileClick(playListName: String, uri: Uri?) {
        var playListName = playListName
        if (uri == null) {
            view.showValidationError(
                null,
                R.string.error_playlist_link,
                null,
                null
            )
            return
        }
        if (TextUtils.isEmpty(playListName)) {
            val url = uri.getPath()
            if (url != null) {
                val lastSlashIndex = url.lastIndexOf('/')
                if (lastSlashIndex != -1 && lastSlashIndex < url.length - 1) {
                    val name = url.substring(lastSlashIndex + 1).trim { it <= ' ' }
                    if (!name.isEmpty()) {
                        view.showPlaylistName(name)
                        playListName = name
                        return
                    } else {
                        view.showValidationError(
                            R.string.error_playlist_name,
                            null, null, null
                        )
                        return
                    }
                } else {
                    view.showValidationError(
                        R.string.error_playlist_name,
                        null, null, null
                    )
                    return
                }
            }
        }
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?

                while ((reader.readLine().also { line = it }) != null) {
                    stringBuilder.append(line).append("\n")
                }

                // Получаем содержимое файла в виде строки
                val fileContent = stringBuilder.toString()
                try {
                    val result: List<Channel> = parseM3U(context, fileContent)
                    val playlist = PlaylistImpl(
                        playListName,
                        uri.getPath()!!,
                        DateFormatUtils.importDate(),
                        -1,
                        true,
                        TypeUtils.TYPE_M3U_LOCAL
                    )
                    executeInBackground(Runnable {
                        handleResult0(result, playlist)
                    })
                } catch (e: Exception) {
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show()
                }
                reader.close()
            }
        } catch (e: IOException) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show()
        }
    }

    fun onSubscribeUriClick(playListName: String, playlistLink: String) {
        var playListName = playListName
        if (TextUtils.isEmpty(playlistLink)) {
            view.showValidationError(
                null,
                R.string.error_playlist_link,
                null,
                null
            )
            return
        }

        if (TextUtils.isEmpty(playListName)) {
            if (TextUtils.isEmpty(playlistLink)) {
                view.showValidationError(
                    R.string.error_playlist_name,
                    null, null, null
                )
                return
            } else {
                val url = playlistLink
                val lastSlashIndex = url.lastIndexOf('/')

                if (lastSlashIndex != -1 && lastSlashIndex < url.length - 1) {
                    val name = url.substring(lastSlashIndex + 1).trim { it <= ' ' }
                    if (!name.isEmpty()) {
                        view.showPlaylistName(name)
                        playListName = name
                    } else {
                        view.showValidationError(
                            R.string.error_playlist_name,
                            null, null, null
                        )
                        return
                    }
                } else {
                    view.showValidationError(
                        R.string.error_playlist_name,
                        null, null, null
                    )
                    return
                }
            }
        }

        val finalPlayListName = playListName
        executeInBackground(Runnable {
            //...
            loadNewPlayList000(playlistLink, finalPlayListName)
        })
    }

    fun saveProfile0(
        profileName: String?, playlistLink: String,
        username: String?,
        password: String?
    ) {
        var profileName = profileName
        val errUserName =
            if (TextUtils.isEmpty(username)) R.string.error_playlist_username else null
        val errPassword =
            if (TextUtils.isEmpty(password)) R.string.error_playlist_password else null

        var errProfileUrl: Int? = null
        var errPlaylistName: Int? = null

        if (TextUtils.isEmpty(playlistLink)) {
            errProfileUrl = R.string.error_playlist_link
        }


        if (TextUtils.isEmpty(playlistLink) || (errPassword != null || errUserName != null)) {
            if (TextUtils.isEmpty(profileName)) {
                errPlaylistName = R.string.error_playlist_name
            }

            view.showValidationError(
                errPlaylistName,
                errProfileUrl,
                errUserName,
                errPassword
            )
            return
        }

        val formatedUrl = formatUrl(playlistLink)
        d("===" + formatedUrl)

        //@@@@@@
        if (TextUtils.isEmpty(profileName)) {
            val playlistLink1 = playlistLink
            val lastSlashIndex = playlistLink1.lastIndexOf('/')
            if (lastSlashIndex != -1 && lastSlashIndex < playlistLink1.length - 1) {
                var name = playlistLink1.substring(lastSlashIndex + 1).trim { it <= ' ' }
                val baseUrl = formatedUrl.toHttpUrlOrNull()
                if (name.isEmpty() && baseUrl != null) {
                    name = baseUrl.host
                }
                if (name.isEmpty()) {
                    //name = @@@@@ url.substring(lastSlashIndex + 1).trim();
                    view.showValidationError(
                        R.string.error_playlist_name,
                        null, null, null
                    )
                    return
                } else {
                    view.showPlaylistName(name)
                    profileName = name
                }
            } else {
                val baseUrl = formatedUrl.toHttpUrlOrNull()
                if (baseUrl != null) {
                    profileName = baseUrl.host
                }
                if (profileName == null || profileName.isEmpty()) {
                    view.showValidationError(
                        R.string.error_playlist_name,
                        null, null, null
                    )
                    return
                } else {
                    view.showPlaylistName(profileName)
                }
            }
        }


        //@@@@@@
        val finalProfileName = profileName
        executeInBackground(Runnable {
            try {
                postToMainThread(Runnable {
                    view.showProgressBar()
                })


                val baseUrl = test0(formatedUrl, username, password)


                //String baseUrl = playlistLink + "/player_api.php?username=" + username + "&password=" + password;
                this.client = NetworkUtils.makeOkhttp()
                val request = Request.Builder().url(baseUrl).build()
                val response = client.newCall(request).execute()
                d("@@@@@@@@@@@" + baseUrl)
                if (response.isSuccessful) {
                    var fileContent = ""
                    val body = response.body
                    if (body != null) {
                        try {
                            fileContent = body.string()
                            //List<Channel> result = M3UParser.parseM3U(context, fileContent);
                            val playlist = PlaylistImpl(
                                finalProfileName!!,
                                baseUrl,
                                DateFormatUtils.importDate(),
                                -1,
                                false,
                                TypeUtils.TYPE_XTREAM_URL
                            )
                            handleXtreamResult(playlist)
                        } catch (e: IOException) {
                            handleException(e)
                            postToMainThread(Runnable {
                                view.showError0(e.getLocalizedMessage())
                            })
                        }
                    }
                    postToMainThread(Runnable {
                        view.hideProgressBar()
                        view.showToast(R.string.xtream_success_saved)
                    })
                } else {
                    // Если запрос завершился неудачно, отправляем сообщение об ошибке
                    postToMainThread(Runnable { view.showError0("Failed to save profile. Error: " + response.message) })
                }
            } catch (e: Exception) {
                handleException(e)
                postToMainThread(Runnable { view.showError0("Network error: " + e.message) })
            }
            postToMainThread(Runnable { view.hideProgressBar() })
        })
    }

    private fun formatUrl(playlistLink: String): String {
        if (!playlistLink.startsWith("http://") && !playlistLink.startsWith("https://")) {
            return "http://" + playlistLink
        }
        return playlistLink
    }


    private fun test0(playlistLink: String, username: String?, password: String?): String {
        val baseUrl = playlistLink.toHttpUrlOrNull()
        if (baseUrl != null) {
            val url = HttpUrl.Builder()
                .scheme(baseUrl.scheme)
                .host(baseUrl.host)
                .port(baseUrl.port)
                .addPathSegment("player_api.php")
                .addQueryParameter("username", username)
                .addQueryParameter(
                    "password",
                    password
                ) //.addPathSegment(streamId + "." + containerExtension)
                .build()

            d("@@@@@@@@@@$playlistLink == $url")
            return url.toString()
        }
        return playlistLink
    }


    private fun loadNewPlayList000(playlistLink: String, playListName: String) {
        this.client = NetworkUtils.makeOkhttp()
        val request = Request.Builder()
            .url(playlistLink)
            .build()

        postToMainThread(Runnable {
            view.showProgressBar()
        })

        val finalPlayListName = playListName
        try {
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                postToMainThread(Runnable {
                    view.hideProgressBar()
                    view.showToast(R.string.error_download_failed)
                })
                return
            }

            val fileContent: String?
            val body = response.body
            if (body != null) {
                try {
                    fileContent = body.string()
                    val result: List<Channel> = parseM3U(context, fileContent?:"")
                    val playlist = PlaylistImpl(
                        finalPlayListName,
                        playlistLink,
                        DateFormatUtils.importDate(),
                        -1,
                        false,
                        TypeUtils.TYPE_M3U_CLOUD
                    )
                    handleResult0(result, playlist)
                } catch (e: IOException) {
                    handleException(e)
                    postToMainThread(Runnable {
                        view.showError0(e.localizedMessage)
                    })
                }
            }
        } catch (e: IOException) {
            handleException(e)
            postToMainThread(Runnable {
                view.showError0(e.localizedMessage)
            })
        } finally {
            postToMainThread(Runnable { view.hideProgressBar() })
        }
    }

    fun backup() {
    }

    fun restore() {
    }

    fun parseClipboardM3U() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

        if (clipboard != null && clipboard.hasPrimaryClip()) {
            val clip = clipboard.primaryClip

            if (clip != null && clip.itemCount > 0) {
                val item = clip.getItemAt(0)
                val clipboardText = item.text
                if (clipboardText != null) {
                    val raw = clipboardText.toString()
                    val result: List<Channel> = parseM3U(context, raw)

                    var playListName = ""
                    if (raw.length > 15) {
                        playListName = raw.substring(0, 15)
                    }

                    val playlist = PlaylistImpl(
                        playListName,
                        ""/*null*/,
                        DateFormatUtils.importDate(),
                        0,
                        false,
                        TypeUtils.TYPE_M3U_BUFFER
                    )
                    executeInBackground(Runnable {
                        handleResult0(result, playlist)
                    })
                } else {
                    view.showErrorToast(R.string.clipboard_contains_no_text)
                }
            } else {
                Log.d("ClipboardUtil", "Clipboard is empty.")
            }
        } else {
            Toast.makeText(context, "Clipboard has no data or is unavailable.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleXtreamResult(playlist: PlaylistImpl) {
//            List<Long> oldIds = new ArrayList<>();
//            //old_ids
//            for (Channel channel : channels) {
//                oldIds.add(channel._id);
//            }
        db_repo.addXtreamPlaylist(playlist)
        //view.updatePlaylistContent(fileContent);
        postToMainThread(Runnable {
            view.showToast(R.string.download_successful)
        })
    }

    private fun handleResult0(channels: List<Channel>, playlist: PlaylistImpl) {
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

            db_repo.addChannelAndPlaylist(channels, playlist)
            //view.updatePlaylistContent(fileContent);
            postToMainThread(Runnable {
                view.showToast(R.string.download_successful)
            })
        }
    } // Другие методы для parseClipboard, backup, restore
}