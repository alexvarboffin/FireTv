package cn.jzvd.demo.CustomMedia;

import static androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.exoplayer.upstream.BandwidthMeter;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;

import com.google.common.net.HttpHeaders;
import com.walhalla.ui.DLog;

import java.net.UnknownHostException;
import java.util.HashMap;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.MyApp;

/**
 * Created by MinhDV on 5/3/18.
 */

@OptIn(markerClass = UnstableApi.class)
public class JZMediaExo extends JZMediaInterface implements Player.Listener {
    private SimpleExoPlayer simpleExoPlayer;
    private Runnable callback;

    private final String TAG = "@@@";

    private long previousSeek = 0;
    private DefaultDataSourceFactory dataSourceFactory;


    public JZMediaExo(Jzvd jzvd) {
        super(jzvd);
    }

    @Override
    public void start() {
        simpleExoPlayer.setPlayWhenReady(true);
    }


    @Override
    public void prepare() {
        Log.e(TAG, "prepare");
        Context context = jzvd.getContext();

        release();
        mMediaHandlerThread = new HandlerThread("JZVD");
        mMediaHandlerThread.start();
        mMediaHandler = new Handler(context.getMainLooper());//主线程还是非主线程，就在这里
        handler = new Handler();
        mMediaHandler.post(() -> {

            // 1. Create a TrackSelector
            TrackSelector trackSelector = new DefaultTrackSelector(context);

            // 2. Create LoadControl with the necessary configurations

//            LoadControl loadControl = new DefaultLoadControl.Builder()
//                    .setBufferDurationsMs(
//                            60000,  // Минимальная продолжительность буфера для воспроизведения (в миллисекундах)
//                            120000, // Максимальная продолжительность буфера для воспроизведения (в миллисекундах)
//                            1000,   // Минимальная продолжительность буфера перед стартом воспроизведения
//                            5000    // Продолжительность буфера после которого будет остановка воспроизведения
//                    )
//                    .build();

//            LoadControl loadControl = new DefaultLoadControl.Builder()
//                    .setAllocator(new DefaultAllocator(true,
//                                    C.DEFAULT_BUFFER_SEGMENT_SIZE//65536
//                            )
//                    )
//                    .setBufferDurationsMs(
//                            360000,
//                            600_000,
//                            1000,
//                            5000
//                    )
//                    .setPrioritizeTimeOverSizeThresholds(false)
//                    .setTargetBufferBytes(C.LENGTH_UNSET)//-1
//                    .build();

            final String currUrl = jzvd.jzDataSource.getCurrentUrl().toString();
            LoadControl loadControl;
            if (currUrl.contains(".m3u8")) {
                loadControl = new DefaultLoadControl.Builder()
                        .setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                        .setBufferDurationsMs(
                                60000,
                                // 60 секунд буферизации перед стартом воспроизведения
                                120_000, // 2 минуты максимального буфера
                                3000,   // 3 секунды буферизации перед началом воспроизведения
                                10000   // 10 секунд буферизации после паузы или прерывания
                        )
                        .setPrioritizeTimeOverSizeThresholds(true) // Может помочь избежать отставания
                        .setTargetBufferBytes(C.LENGTH_UNSET) // Пусть система управляет целевым размером буфера
                        .build();
            } else {
                loadControl = new DefaultLoadControl.Builder()
                        .setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                        .setBufferDurationsMs(

                                20_000,  //буферизации до начала воспроизведения
                                300000,  // 5 минут максимального буфера
                                5000,    // 5 секунд буфера перед началом воспроизведения
                                10000    // 10 секунд буфера после прерывания
                        )
                        .setPrioritizeTimeOverSizeThresholds(true)
                        .setTargetBufferBytes(C.LENGTH_UNSET) // Позволить системе автоматически выбирать размер буфера
                        .build();
            }


// 3. Create BandwidthMeter
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();

// 4. Create RenderersFactory
            RenderersFactory renderersFactory = new DefaultRenderersFactory(context);

// 5. Create the SimpleExoPlayer instance
            simpleExoPlayer = new SimpleExoPlayer.Builder(context, renderersFactory)
                    .setTrackSelector(trackSelector)
                    .setLoadControl(loadControl)
                    .setBandwidthMeter(bandwidthMeter)
                    .build();


            HashMap<String, String> headers = jzvd.jzDataSource.headerMap;


            String extUserAgent = headers.get(HttpHeaders.USER_AGENT);
            String referer = headers.get("Referer");
            String userAgent = "";
            if (!TextUtils.isEmpty(extUserAgent)) {
                userAgent = extUserAgent;
                headers.remove(HttpHeaders.USER_AGENT);
            } else {
                //userAgent = Util.getUserAgent(context, context.getResources().getString(R.string.app_name));
                //YourApplicationName/1.4.240821.DEMO (Linux;Android 9) ExoPlayerLib/2.19.1
                //StreamHub1724256501396/1.4.240821.DEMO (Linux;Android 9) ExoPlayerLib/2.19.1
                userAgent = Util.getUserAgent(MyApp.getInstance(), "StreamHub-" + System.currentTimeMillis());
            }
            if (!TextUtils.isEmpty(referer)) {
                //@@ ;
            }


            DLog.d("@--ua--@" + userAgent);

            MyDefaultHttpDataSource.Factory httpDataSourceFactory = new MyDefaultHttpDataSource.Factory()
                    .setUserAgent(userAgent)
                    .setDefaultRequestProperties(headers);
            // 6. Create a DataSource.Factory instance
            //DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, ua);
            dataSourceFactory = new DefaultDataSourceFactory(context, httpDataSourceFactory);


// 7. Create the MediaSource depending on the URL



            MediaSource videoSource;

            if (currUrl.contains(".m3u8")) {
                videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(currUrl)));
            } else {
                videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(currUrl)));
            }

// Note: The `addEventListener` and related methods are now part of the `Player.Listener` interface.
// You can implement `Player.Listener` and override the relevant methods to handle events.


            Log.e(TAG, "URL Link = " + currUrl);

            simpleExoPlayer.addListener(this);
            boolean isLoop = jzvd.jzDataSource.looping;
            if (isLoop) {
                simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            } else {
                simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
            simpleExoPlayer.prepare(videoSource);
            simpleExoPlayer.setPlayWhenReady(true);
            callback = new onBufferingUpdate();

            if (jzvd.textureView != null) {
                SurfaceTexture surfaceTexture = jzvd.textureView.getSurfaceTexture();
                if (surfaceTexture != null) {
                    simpleExoPlayer.setVideoSurface(new Surface(surfaceTexture));
                }
            }
        });

    }


    @Override
    public void onRenderedFirstFrame() {
        Log.e(TAG, "onRenderedFirstFrame");
    }

    @Override
    public void pause() {
        simpleExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public boolean isPlaying() {
        return simpleExoPlayer.getPlayWhenReady();
    }

    @Override
    public void seekTo(long time) {
        if (simpleExoPlayer == null) {
            return;
        }
        if (time != previousSeek) {
            if (time >= simpleExoPlayer.getBufferedPosition()) {
                jzvd.onStatePreparingPlaying();
            }
            simpleExoPlayer.seekTo(time);
            previousSeek = time;
            jzvd.seekToInAdvance = time;

        }
    }

    @Override
    public void release() {
        if (mMediaHandler != null && mMediaHandlerThread != null && simpleExoPlayer != null) {//不知道有没有妖孽
            HandlerThread tmpHandlerThread = mMediaHandlerThread;
            SimpleExoPlayer tmpMediaPlayer = simpleExoPlayer;
            JZMediaInterface.SAVED_SURFACE = null;

            mMediaHandler.post(() -> {
                tmpMediaPlayer.release();//release就不能放到主线程里，界面会卡顿
                tmpHandlerThread.quit();
            });
            simpleExoPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        if (simpleExoPlayer != null)
            return simpleExoPlayer.getCurrentPosition();
        else return 0;
    }

    @Override
    public long getDuration() {
        if (simpleExoPlayer != null)
            return simpleExoPlayer.getDuration();
        else return 0;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        simpleExoPlayer.setVolume(leftVolume);
        simpleExoPlayer.setVolume(rightVolume);
    }

    @Override
    public void setSpeed(float speed) {
        PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1.0F);
        simpleExoPlayer.setPlaybackParameters(playbackParameters);
    }


    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.e(TAG, "onLoadingChanged");
    }

    @Override
    public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
        Log.e(TAG, "onPlayerStateChanged" + playbackState + "/ready=" + String.valueOf(playWhenReady));
        handler.post(() -> {
            switch (playbackState) {
                case Player.STATE_IDLE: {
                }
                break;
                case Player.STATE_BUFFERING: {
                    jzvd.onStatePreparingPlaying();
                    handler.post(callback);
                }
                break;
                case Player.STATE_READY: {
                    if (playWhenReady) {
                        jzvd.onStatePlaying();
                    } else {
                    }
                }
                break;
                case Player.STATE_ENDED: {
                    jzvd.onCompletion();
                }
                break;
            }
        });
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        Player.Listener.super.onPlayerError(error);

        final Throwable cause = error.getCause();
//        if (cause instanceof HttpDataSource.HttpDataSourceException) {
//            HttpDataSource.HttpDataSourceException httpException = (HttpDataSource.HttpDataSourceException) cause;
//            int httpStatusCode = httpException.get;
//
//            switch (httpStatusCode) {
//                case 404:
//                    Log.e(TAG, "Ошибка 404: Ресурс не найден.");
//                    break;
//                case 403:
//                    Log.e(TAG, "Ошибка 403: Доступ запрещен.");
//                    break;
//                // Добавьте другие HTTP-коды по необходимости
//            }
//        } else {
//            // Обработка других ошибок
//            Log.e(TAG, "Ошибка: " + error.getMessage());
//        }

        switch (error.errorCode) {

            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED:
                String causeMessage = cause.getMessage();
                String clazz = cause.getClass().getSimpleName();
                if (cause instanceof UnknownHostException) {
                    Log.e(TAG, "Ошибка: Невозможно разрешить хост. Проверьте подключение к интернету.");

                }
                DLog.d("***" + error.errorCode + "@@" + error.getErrorCodeName()
                        + "@@" + error.getMessage() + "@@[" + causeMessage + "] " + clazz);

                //как тут проверить DefaultHttpDataSource.getResponseCode()

                break;

            case PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW:
                DLog.d("Ошибка: Поток отстал от окна прямого эфира.");
                // Здесь можно добавить логирование или другие действия по вашему усмотрению
                break;
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS:
                //JZMediaExo::onPlayerError ● @@2004@@ERROR_CODE_IO_BAD_HTTP_STATUS@@Source error@@

                String httpStatusCode = cause.getMessage();
                DLog.d("404?" + error.errorCode + "@@" + error.getErrorCodeName()
                        + "@@" + error.getMessage() + "@@[" + httpStatusCode);
                // Обработка других HTTP ошибок
                if (httpStatusCode != null && httpStatusCode.contains("404")) {
                    // Обработка ошибки 404: Ресурс не найден
                    Log.e(TAG, "Ошибка: Ресурс не найден (404). Проверьте URL.");
                    // Здесь вы можете выполнить дополнительные действия, например:
                    // - Показать сообщение пользователю
                    // - Попробовать загрузить другой ресурс
                }
                break;

            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED:


                String httpstatuscode = cause.getMessage();

                // Обработка других HTTP ошибок
                if (httpstatuscode != null && httpstatuscode.contains("204")) {
                    //204?3002@@ERROR_CODE_PARSING_MANIFEST_MALFORMED@@Source error@@[Input does not start with the #EXTM3U header.{contentIsMalformed=true, dataType=4}

                    Log.e(TAG, "Ошибка:204 No Content (204).");
                    DLog.d("204?" + error.errorCode + "@@" + error.getErrorCodeName()
                            + "@@" + error.getMessage() + "@@[" + httpstatuscode);

                } else {
                    DLog.d("204?" + error.errorCode + "@@" + error.getErrorCodeName()
                            + "@@" + error.getMessage() + "@@[" + httpstatuscode);
                }
                break;

//            case ExoPlaybackException.TYPE_SOURCE:
//                DLog.d( "TYPE_SOURCE: " + error.getSourceException().getMessage());
//                break;
//
//            case ExoPlaybackException.TYPE_RENDERER:
//                DLog.d( "TYPE_RENDERER: " + error.getRendererException().getMessage());
//                break;
//
//            case ExoPlaybackException.TYPE_UNEXPECTED:
//                DLog.d( "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
//                break;
//            case ExoPlaybackException.TYPE_SOURCE:
//                Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());
//                //Restart the playback
//                play(mediaItem);
            default:
                //@@1002@@ERROR_CODE_BEHIND_LIVE_WINDOW@@Source error@@
                DLog.d("@__err_@" + error.errorCode + "@@" + error.getErrorCodeName()
                        + "@@" + error.getMessage() + "@@");
        }
        handler.post(() -> jzvd.onError(1000, 1000));
    }


    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    @Override
    public void setSurface(Surface surface) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setVideoSurface(surface);
        } else {
            Log.e("AGVideo", "simpleExoPlayer为空");
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (SAVED_SURFACE == null) {
            SAVED_SURFACE = surface;
            prepare();
        } else {
            jzvd.textureView.setSurfaceTexture(SAVED_SURFACE);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class onBufferingUpdate implements Runnable {
        @Override
        public void run() {
            if (simpleExoPlayer != null) {
                final int percent = simpleExoPlayer.getBufferedPercentage();
                handler.post(() -> jzvd.setBufferProgress(percent));
                if (percent < 100) {
                    handler.postDelayed(callback, 300);
                } else {
                    handler.removeCallbacks(callback);
                }
            }
        }
    }


    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        int width = videoSize.width;
        int height = videoSize.height;
        float pixelWidthHeightRatio = videoSize.pixelWidthHeightRatio;

        handler.post(() -> jzvd.onVideoSizeChanged((int) (width * pixelWidthHeightRatio), height));
    }

    @Override
    public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
        Log.e(TAG, "onTimelineChanged");
        // If you need to handle this event, you can do so here.
        // You can also post it to the main thread if necessary.
//        handler.post(() -> {
//            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
//                jzvd.onInfo(reason, timeline.getPeriodCount());
//            }
//        });

        //        JZMediaPlayer.instance().mainThreadHandler.post(() -> {
//                if (reason == 0) {
//
//                    JzvdMgr.getCurrentJzvd().onInfo(reason, timeline.getPeriodCount());
//                }
//        })
    }

    //@Override
    public void onSeekProcessed() {
        handler.post(() -> jzvd.onSeekComplete());
    }
}
