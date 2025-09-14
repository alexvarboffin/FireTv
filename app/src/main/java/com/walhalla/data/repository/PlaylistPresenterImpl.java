package com.walhalla.data.repository;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.NetworkUtils.makeOkhttp;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.walhalla.data.model.Channel;
import com.walhalla.data.model.PlaylistImpl;
import com.walhalla.ui.DLog;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.BasePresenter0;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView;

public class PlaylistPresenterImpl extends BasePresenter0 {


    public PlaylistPresenterImpl(Handler handler, PlaylistManagementView view, Context context) {
        super(handler, view, context);
    }


    public void loadAndSavePlaylist(PlaylistImpl playlist) {
        loadNewPlayList(playlist);
    }

    public void loadNewPlayList(PlaylistImpl oldValue) {

        String playlistLink = oldValue.getFileName();
        String playListName = oldValue.getTitle();

        this.client = makeOkhttp();
        Request request = new Request.Builder()
                .url(playlistLink)
                .build();

        executeInBackground(() -> {
            try {
                // Выполнение синхронного запроса
                Response response = client.newCall(request).execute();

                // Обработка ответа
                if (!response.isSuccessful()) {
                    // Вызов в главном потоке для показа сообщения
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
                        handleResult0(result, oldValue);

                    } catch (IOException e) {
                        DLog.handleException(e);
                        // Вызов в главном потоке для показа сообщения об ошибке
                        postToMainThread(() -> view.showToast(R.string.error_download_failed));
                    }
                }
            } catch (IOException e) {
                DLog.handleException(e);
                // Вызов в главном потоке для показа сообщения об ошибке
                postToMainThread(() -> view.showToast(R.string.error_download_failed));
            } finally {
                // Скрыть прогресс бар независимо от результата
                postToMainThread(view::hideProgressBar);
            }
        });
    }

    private void handleResult0(List<Channel> channels, PlaylistImpl oldValue) {
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

            int m = db_repo.deletePlaylistAndRelatedChannels(oldValue);
            if (m > 0) {
                PlaylistImpl newValue = new PlaylistImpl(
                        oldValue.getTitle(),
                        oldValue.getFileName(),
                        oldValue.getImportDate(),
                        oldValue.getCount(),
                        oldValue.isAutoUpdate(),
                        TypeUtils.TYPE_M3U_CLOUD
                );

                newValue.updateDate = DateFormatUtils.importDate();
                newValue.setCount(-1);
                db_repo.addChannelAndPlaylist(channels, newValue);
                //view.updatePlaylistContent(fileContent);

                postToMainThread(() ->
                {
                    view.onPlaylistUpdated();
                    view.showToast(R.string.download_successful);
                });
            }
        }
    }

    public void selectAllPlaylist(RepoCallback<List<PlaylistImpl>> callback) {
        executeInBackground(() -> {
            List<PlaylistImpl> playlists = db_repo.selectAllPlaylist();
            postToMainThread(() -> {
                callback.successResult(playlists);
            });
        });
    }

    public void deletePlaylistAndRelatedChannels(PlaylistImpl playlist, RepoCallback<Integer> callback) {
        executeInBackground(() -> {
            try {
                int k = db_repo.deletePlaylistAndRelatedChannels(playlist);
                postToMainThread(() -> {
                    callback.successResult(k);
                });
            }catch (Exception e){
                postToMainThread(() -> {
                    callback.errorResult(e.getMessage());
                });
            }
        });
    }
}
