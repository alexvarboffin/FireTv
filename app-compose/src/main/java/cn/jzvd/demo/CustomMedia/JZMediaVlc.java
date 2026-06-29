package cn.jzvd.demo.CustomMedia;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.net.HttpHeaders;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.Map;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;

public class JZMediaVlc extends JZMediaInterface implements MediaPlayer.EventListener {

    private MediaPlayer vlcPlayer;
    private VLCVideoLayout vlcVideoLayout;
    private volatile boolean isReleased;
    private boolean preparedNotified;
    private boolean pendingStart;
    private boolean videoOutputAttached;
    private int attachedWidth;
    private int attachedHeight;
    private volatile long cachedPosition;
    private volatile long cachedDuration;
    private volatile boolean cachedPlaying;
    private volatile int prepareGeneration;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public JZMediaVlc(Jzvd jzvd) {
        super(jzvd);
    }

    @Override
    public void start() {
        if (isReleased || vlcPlayer == null) {
            return;
        }
        mainHandler.post(() -> {
            if (isReleased || vlcPlayer == null) {
                return;
            }
            if (!attachVideoOutputOnce()) {
                pendingStart = true;
                return;
            }
            pendingStart = false;
            postToMediaThread(() -> {
                if (!isReleased && vlcPlayer != null) {
                    vlcPlayer.play();
                }
            });
        });
    }

    @Override
    public void prepare() {
        release();
        isReleased = false;
        preparedNotified = false;
        pendingStart = false;
        videoOutputAttached = false;
        attachedWidth = 0;
        attachedHeight = 0;
        cachedPosition = 0;
        cachedDuration = 0;
        cachedPlaying = false;
        final int generation = ++prepareGeneration;
        mMediaHandlerThread = new HandlerThread("JZVD-VLC");
        mMediaHandlerThread.start();
        mMediaHandler = new Handler(mMediaHandlerThread.getLooper());
        handler = new Handler(Looper.getMainLooper());

        mMediaHandler.post(() -> {
            if (isReleased || generation != prepareGeneration) {
                return;
            }

            MediaPlayer player = new MediaPlayer(VlcLibHolder.get(jzvd.getContext()));
            player.setEventListener(this);

            String url = jzvd.jzDataSource.getCurrentUrl().toString();
            Media media = new Media(player.getLibVLC(), Uri.parse(url));
            applyHeaders(media);
            applyStreamOptions(media, url);
            media.setHWDecoderEnabled(false, false);

            if (isReleased || generation != prepareGeneration) {
                media.release();
                player.release();
                return;
            }

            player.setMedia(media);
            media.release();
            vlcPlayer = player;

            mainHandler.post(this::notifyPreparedWhenSurfaceReady);
        });
    }

    private void applyHeaders(Media media) {
        Map<String, String> headers = jzvd.jzDataSource.headerMap;
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            if (HttpHeaders.USER_AGENT.equalsIgnoreCase(key)) {
                media.addOption(":http-user-agent=" + value);
            } else if ("Referer".equalsIgnoreCase(key)) {
                media.addOption(":http-referrer=" + value);
            }
        }
    }

    private void applyStreamOptions(Media media, String url) {
        if (url.contains(".m3u8")) {
            media.addOption(":network-caching=1500");
        } else {
            media.addOption(":network-caching=3000");
        }
    }

    private void postToMediaThread(Runnable task) {
        Handler mediaHandler = mMediaHandler;
        if (mediaHandler != null) {
            mediaHandler.post(task);
        } else {
            new Thread(task, "JZVD-VLC-op").start();
        }
    }

    private TextureView textureView() {
        return jzvd.textureView;
    }

    private void ensureVlcVideoLayout() {
        if (vlcVideoLayout != null) {
            return;
        }
        TextureView textureView = textureView();
        if (textureView == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) textureView.getParent();
        if (parent == null) {
            return;
        }
        vlcVideoLayout = new VLCVideoLayout(jzvd.getContext());
        vlcVideoLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        int index = parent.indexOfChild(textureView);
        parent.addView(vlcVideoLayout, index + 1);
        textureView.setVisibility(View.INVISIBLE);
    }

    private void removeVlcVideoLayout() {
        TextureView textureView = textureView();
        if (textureView != null) {
            textureView.setVisibility(View.VISIBLE);
        }
        if (vlcVideoLayout != null) {
            ViewGroup parent = (ViewGroup) vlcVideoLayout.getParent();
            if (parent != null) {
                parent.removeView(vlcVideoLayout);
            }
            vlcVideoLayout = null;
        }
    }

    private boolean isSurfaceReady() {
        ensureVlcVideoLayout();
        return vlcVideoLayout != null
                && vlcVideoLayout.getWidth() > 0
                && vlcVideoLayout.getHeight() > 0;
    }

    private boolean attachVideoOutputOnce() {
        if (isReleased || vlcPlayer == null || !isSurfaceReady()) {
            return false;
        }

        int width = vlcVideoLayout.getWidth();
        int height = vlcVideoLayout.getHeight();

        if (videoOutputAttached && width == attachedWidth && height == attachedHeight) {
            return true;
        }

        try {
            if (vlcPlayer.getVLCVout().areViewsAttached()) {
                vlcPlayer.detachViews();
            }
        } catch (Exception ignored) {
        }

        vlcPlayer.attachViews(vlcVideoLayout, null, false, false);
        videoOutputAttached = true;
        attachedWidth = width;
        attachedHeight = height;
        return true;
    }

    private void updateVideoWindowSize(int width, int height) {
        if (isReleased || vlcPlayer == null || width <= 0 || height <= 0) {
            return;
        }
        if (width == attachedWidth && height == attachedHeight) {
            return;
        }
        if (!videoOutputAttached) {
            attachVideoOutputOnce();
            return;
        }
        vlcPlayer.getVLCVout().setWindowSize(width, height);
        attachedWidth = width;
        attachedHeight = height;
    }

    private void notifyPreparedWhenSurfaceReady() {
        if (isReleased || vlcPlayer == null || preparedNotified) {
            return;
        }
        ensureVlcVideoLayout();
        if (vlcVideoLayout != null && vlcVideoLayout.getWidth() == 0) {
            vlcVideoLayout.post(this::notifyPreparedWhenSurfaceReady);
            return;
        }
        if (!isSurfaceReady()) {
            return;
        }
        preparedNotified = true;
        jzvd.onPrepared();
        if (pendingStart) {
            start();
        }
    }

    @Override
    public void pause() {
        postToMediaThread(() -> {
            if (vlcPlayer != null) {
                vlcPlayer.pause();
            }
        });
    }

    @Override
    public boolean isPlaying() {
        return cachedPlaying;
    }

    @Override
    public void seekTo(long time) {
        postToMediaThread(() -> {
            if (vlcPlayer != null) {
                vlcPlayer.setTime(time);
                cachedPosition = time;
                jzvd.seekToInAdvance = time;
            }
        });
    }

    @Override
    public void release() {
        isReleased = true;
        prepareGeneration++;
        pendingStart = false;
        videoOutputAttached = false;
        cachedPlaying = false;
        Handler tmpHandler = mMediaHandler;
        HandlerThread tmpHandlerThread = mMediaHandlerThread;
        MediaPlayer tmpPlayer = vlcPlayer;
        JZMediaInterface.SAVED_SURFACE = null;
        mMediaHandler = null;
        mMediaHandlerThread = null;
        vlcPlayer = null;

        if (tmpHandler != null) {
            tmpHandler.removeCallbacksAndMessages(null);
        }

        // UI + detachViews on main; stop/release off main (avoids ANR on back key).
        mainHandler.post(() -> {
            removeVlcVideoLayout();
            try {
                if (tmpPlayer != null && tmpPlayer.getVLCVout().areViewsAttached()) {
                    tmpPlayer.detachViews();
                }
            } catch (Exception ignored) {
            }

            Runnable stopAndRelease = () -> stopAndReleasePlayer(tmpPlayer, tmpHandlerThread);
            if (tmpHandler != null) {
                tmpHandler.post(stopAndRelease);
            } else {
                new Thread(stopAndRelease, "JZVD-VLC-release").start();
            }
        });
    }

    private static void stopAndReleasePlayer(MediaPlayer player, HandlerThread handlerThread) {
        if (player != null) {
            player.setEventListener(null);
            try {
                player.stop();
            } catch (Exception ignored) {
            }
            try {
                player.release();
            } catch (Exception ignored) {
            }
        }
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
    }

    @Override
    public long getCurrentPosition() {
        return cachedPosition;
    }

    @Override
    public long getDuration() {
        return cachedDuration;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        postToMediaThread(() -> {
            if (vlcPlayer != null) {
                vlcPlayer.setVolume((int) (Math.max(leftVolume, rightVolume) * 100));
            }
        });
    }

    @Override
    public void setSpeed(float speed) {
        postToMediaThread(() -> {
            if (vlcPlayer != null) {
                vlcPlayer.setRate(speed);
            }
        });
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        if (isReleased) {
            return;
        }
        switch (event.type) {
            case MediaPlayer.Event.Buffering:
                handler.post(() -> jzvd.setBufferProgress((int) event.getBuffering()));
                break;
            case MediaPlayer.Event.Playing:
                cachedPlaying = true;
                handler.post(() -> jzvd.onStatePlaying());
                break;
            case MediaPlayer.Event.Paused:
            case MediaPlayer.Event.Stopped:
                cachedPlaying = false;
                break;
            case MediaPlayer.Event.TimeChanged:
                cachedPosition = event.getTimeChanged();
                break;
            case MediaPlayer.Event.LengthChanged:
                cachedDuration = Math.max(event.getLengthChanged(), 0);
                break;
            case MediaPlayer.Event.Vout:
                postToMediaThread(this::notifyVideoSizeChanged);
                break;
            case MediaPlayer.Event.EndReached:
                cachedPlaying = false;
                handler.post(() -> jzvd.onCompletion());
                break;
            case MediaPlayer.Event.EncounteredError:
                cachedPlaying = false;
                handler.post(() -> jzvd.onError(1, 1));
                break;
            default:
                break;
        }
    }

    private void notifyVideoSizeChanged() {
        MediaPlayer player = vlcPlayer;
        if (player == null) {
            return;
        }
        Media.VideoTrack track = player.getCurrentVideoTrack();
        if (track == null) {
            return;
        }
        handler.post(() -> jzvd.onVideoSizeChanged(track.width, track.height));
    }

    @Override
    public void setSurface(Surface surface) {
        mainHandler.post(this::notifyPreparedWhenSurfaceReady);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (SAVED_SURFACE == null) {
            SAVED_SURFACE = surface;
            prepare();
        } else {
            jzvd.textureView.setSurfaceTexture(SAVED_SURFACE);
        }
        mainHandler.post(this::notifyPreparedWhenSurfaceReady);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mainHandler.post(() -> {
            updateVideoWindowSize(width, height);
            notifyPreparedWhenSurfaceReady();
        });
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
