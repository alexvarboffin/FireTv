package com.walhalla.data.repository;


import static tv.hdonlinetv.besttvchannels.movies.watchfree.NetworkUtils.makeOkhttp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.walhalla.data.model.Channel;
import com.walhalla.data.model.PlaylistImpl;
import com.walhalla.ui.DLog;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.BasePresenter0;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class PlaylistManagementPresenterImpl extends BasePresenter0 {


    public PlaylistManagementPresenterImpl(Handler handler, PlaylistManagementView view, Context context) {
        super(handler, view, context);
    }

    public void onSubscribeFileClick(String playListName, Uri uri) {
        if (uri == null) {
            view.showValidationError(
                    null,
                    R.string.error_playlist_link,
                    null
                    , null
            );
            return;
        }
        if (TextUtils.isEmpty(playListName)) {
            String url = uri.getPath();
            if (url != null) {
                int lastSlashIndex = url.lastIndexOf('/');
                if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
                    String name = url.substring(lastSlashIndex + 1).trim();
                    if (!name.isEmpty()) {
                        view.showPlaylistName(name);
                        playListName = name;
                        return;
                    } else {
                        view.showValidationError(
                                R.string.error_playlist_name,
                                null, null, null
                        );
                        return;
                    }
                } else {
                    view.showValidationError(
                            R.string.error_playlist_name,
                            null, null, null
                    );
                    return;
                }
            }
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                // Получаем содержимое файла в виде строки
                String fileContent = stringBuilder.toString();
                try {
                    List<Channel> result = M3UParser.parseM3U(context, fileContent);
                    PlaylistImpl playlist = new PlaylistImpl(
                            playListName,
                            uri.getPath(),
                            DateFormatUtils.importDate(),
                            -1,
                            true
                            , TypeUtils.TYPE_M3U_LOCAL
                    );
                    executeInBackground(() -> {
                        handleResult0(result, playlist);
                    });
                } catch (Exception e) {
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                reader.close();
            }
        } catch (IOException e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onSubscribeUriClick(String playListName, String playlistLink) {

        if (TextUtils.isEmpty(playlistLink)) {
            view.showValidationError(
                    null,
                    R.string.error_playlist_link,
                    null
                    , null
            );
            return;
        }

        if (TextUtils.isEmpty(playListName)) {
            if (TextUtils.isEmpty(playlistLink)) {
                view.showValidationError(
                        R.string.error_playlist_name,
                        null, null, null
                );
                return;
            } else {
                String url = playlistLink;
                int lastSlashIndex = url.lastIndexOf('/');

                if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
                    String name = url.substring(lastSlashIndex + 1).trim();
                    if (!name.isEmpty()) {
                        view.showPlaylistName(name);
                        playListName = name;
                    } else {
                        view.showValidationError(
                                R.string.error_playlist_name,
                                null, null, null
                        );
                        return;
                    }
                } else {
                    view.showValidationError(
                            R.string.error_playlist_name,
                            null, null, null
                    );
                    return;
                }
            }
        }

        String finalPlayListName = playListName;
        executeInBackground(() -> {
            //...
            loadNewPlayList000(playlistLink, finalPlayListName);
        });
    }

    public void saveProfile0(
            String profileName, String playlistLink,
            String username,
            String password
    ) {


        Integer errUserName = TextUtils.isEmpty(username) ? R.string.error_playlist_username : null;
        Integer errPassword = TextUtils.isEmpty(password) ? R.string.error_playlist_password : null;

        Integer errProfileUrl = null;
        Integer errPlaylistName = null;

        if (TextUtils.isEmpty(playlistLink)) {
            errProfileUrl = R.string.error_playlist_link;
        }


        if (TextUtils.isEmpty(playlistLink) || (errPassword != null || errUserName != null)) {

            if (TextUtils.isEmpty(profileName)) {
                errPlaylistName = R.string.error_playlist_name;
            }

            view.showValidationError(
                    errPlaylistName,
                    errProfileUrl,
                    errUserName,
                    errPassword
            );
            return;
        }

        final String formatedUrl = formatUrl(playlistLink);
        DLog.d("===" + formatedUrl);

        //@@@@@@
        if (TextUtils.isEmpty(profileName)) {


            String playlistLink1 = playlistLink;
            int lastSlashIndex = playlistLink1.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < playlistLink1.length() - 1) {
                String name = playlistLink1.substring(lastSlashIndex + 1).trim();
                HttpUrl baseUrl = HttpUrl.parse(formatedUrl);
                if (name.isEmpty() && baseUrl != null) {
                    name = baseUrl.host();
                }
                if (name.isEmpty()) {
                    //name = @@@@@ url.substring(lastSlashIndex + 1).trim();
                    view.showValidationError(
                            R.string.error_playlist_name,
                            null, null, null
                    );
                    return;
                } else {
                    view.showPlaylistName(name);
                    profileName = name;
                }
            } else {
                HttpUrl baseUrl = HttpUrl.parse(formatedUrl);
                if (baseUrl != null) {
                    profileName = baseUrl.host();
                }
                if (profileName == null || profileName.isEmpty()) {
                    view.showValidationError(
                            R.string.error_playlist_name,
                            null, null, null
                    );
                    return;
                } else {
                    view.showPlaylistName(profileName);
                }
            }
        }
        //@@@@@@


        String finalProfileName = profileName;
        executeInBackground(() -> {
            try {
                postToMainThread(() -> {
                    view.showProgressBar();
                });


                String baseUrl = test0(formatedUrl, username, password);
                //String baseUrl = playlistLink + "/player_api.php?username=" + username + "&password=" + password;


                this.client = makeOkhttp();
                Request request = new Request.Builder().url(baseUrl).build();
                Response response = client.newCall(request).execute();
                DLog.d("@@@@@@@@@@@" + baseUrl);
                if (response.isSuccessful()) {
                    String fileContent = "";
                    ResponseBody body = response.body();
                    if (body != null) {
                        try {
                            fileContent = body.string();
                            //List<Channel> result = M3UParser.parseM3U(context, fileContent);
                            PlaylistImpl playlist = new PlaylistImpl(
                                    finalProfileName,
                                    baseUrl,
                                    DateFormatUtils.importDate(),
                                    -1,
                                    false
                                    , TypeUtils.TYPE_XTREAM_URL
                            );
                            handleXtreamResult(playlist);

                        } catch (IOException e) {
                            DLog.handleException(e);
                            postToMainThread(() -> {
                                view.showError0(e.getLocalizedMessage());
                            });
                        }
                    }
                    postToMainThread(() -> {
                        view.hideProgressBar();
                        view.showToast(R.string.xtream_success_saved);
                    });
                } else {
                    // Если запрос завершился неудачно, отправляем сообщение об ошибке
                    postToMainThread(() -> view.showError0("Failed to save profile. Error: " + response.message()));
                }
            } catch (Exception e) {
                DLog.handleException(e);
                postToMainThread(() -> view.showError0("Network error: " + e.getMessage()));
            }
            postToMainThread(view::hideProgressBar);
        });
    }

    private String formatUrl(String playlistLink) {
        if (!playlistLink.startsWith("http://") && !playlistLink.startsWith("https://")) {
            return "http://" + playlistLink;
        }
        return playlistLink;
    }


    private String test0(String playlistLink, String username, String password) {
        HttpUrl baseUrl = HttpUrl.parse(playlistLink);
        if (baseUrl != null) {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme(baseUrl.scheme())
                    .host(baseUrl.host())
                    .port(baseUrl.port())
                    .addPathSegment("player_api.php")
                    .addQueryParameter("username", username)
                    .addQueryParameter("password", password)
                    //.addPathSegment(streamId + "." + containerExtension)
                    .build();

            DLog.d("@@@@@@@@@@" + playlistLink + " == " + url);
            return url.toString();
        }
        return playlistLink;
    }


    private void loadNewPlayList000(String playlistLink, String playListName) {
        this.client = makeOkhttp();
        Request request = new Request.Builder()
                .url(playlistLink)
                .build();

        postToMainThread(() -> {
            view.showProgressBar();
        });

        String finalPlayListName = playListName;
        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                postToMainThread(() -> {
                    view.hideProgressBar();
                    view.showToast(R.string.error_download_failed);
                });
                return;
            }

            String fileContent;
            ResponseBody body = response.body();
            if (body != null) {
                try {
                    fileContent = body.string();
                    List<Channel> result = M3UParser.parseM3U(context, fileContent);
                    PlaylistImpl playlist = new PlaylistImpl(
                            finalPlayListName,
                            playlistLink,
                            DateFormatUtils.importDate(),
                            -1,
                            false
                            , TypeUtils.TYPE_M3U_CLOUD
                    );
                    handleResult0(result, playlist);

                } catch (IOException e) {
                    DLog.handleException(e);
                    postToMainThread(() -> {
                        view.showError0(e.getLocalizedMessage());
                    });
                }
            }
        } catch (IOException e) {
            DLog.handleException(e);
            postToMainThread(() -> {
                view.showError0(e.getLocalizedMessage());
            });
        } finally {
            postToMainThread(view::hideProgressBar);
        }
    }

    public void backup() {
    }

    public void restore() {
    }

    public void parseClipboardM3U() {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();

            if (clip != null && clip.getItemCount() > 0) {
                ClipData.Item item = clip.getItemAt(0);
                CharSequence clipboardText = item.getText();
                if (clipboardText != null) {
                    String raw = clipboardText.toString();
                    List<Channel> result = M3UParser.parseM3U(context, raw);

                    String playListName = "";
                    if (raw.length() > 15) {
                        playListName = raw.substring(0, 15);
                    }

                    PlaylistImpl playlist = new PlaylistImpl(
                            playListName,
                            null,
                            DateFormatUtils.importDate(),
                            0,
                            false
                            , TypeUtils.TYPE_M3U_BUFFER
                    );
                    executeInBackground(() -> {
                        handleResult0(result, playlist);
                    });
                } else {
                    view.showErrorToast(R.string.clipboard_contains_no_text);
                }
            } else {
                Log.d("ClipboardUtil", "Clipboard is empty.");
            }
        } else {
            Toast.makeText(context, "Clipboard has no data or is unavailable.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleXtreamResult(PlaylistImpl playlist) {
//            List<Long> oldIds = new ArrayList<>();
//            //old_ids
//            for (Channel channel : channels) {
//                oldIds.add(channel._id);
//            }
        db_repo.addXtreamPlaylist(playlist);
        //view.updatePlaylistContent(fileContent);
        postToMainThread(() ->
        {
            view.showToast(R.string.download_successful);
        });
    }

    private void handleResult0(List<Channel> channels, PlaylistImpl playlist) {
        if (channels.isEmpty()) {
            postToMainThread(() -> {
                Toast.makeText(context, R.string.no_lines_found, Toast.LENGTH_SHORT).show();
            });
        } else {

//            List<Long> oldIds = new ArrayList<>();
//            //old_ids
//            for (Channel channel : channels) {
//                oldIds.add(channel._id);
//            }
            db_repo.addChannelAndPlaylist(channels, playlist);
            //view.updatePlaylistContent(fileContent);
            postToMainThread(() ->
            {
                view.showToast(R.string.download_successful);
            });
        }
    }
    // Другие методы для parseClipboard, backup, restore
}