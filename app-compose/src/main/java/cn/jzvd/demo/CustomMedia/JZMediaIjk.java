package cn.jzvd.demo.CustomMedia;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import java.io.IOException;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * Created by Nathen on 2017/11/18.
 * ijk兼容SO库:https://github.com/NamHofstadter/IjkPlayerSos
 * ijk默认不支持https协议,需要的请自行下载so库
 */

public class JZMediaIjk extends JZMediaInterface implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {
    IjkMediaPlayer ijkMediaPlayer;
    private volatile boolean isReleased;

    public JZMediaIjk(Jzvd jzvd) {
        super(jzvd);
    }

    @Override
    public void start() {
        if (isReleased || ijkMediaPlayer == null) return;
        ijkMediaPlayer.start();
    }

    @Override
    public void prepare() {
        release();
        isReleased = false;
        mMediaHandlerThread = new HandlerThread("JZVD");
        mMediaHandlerThread.start();
        mMediaHandler = new Handler(mMediaHandlerThread.getLooper());
        handler = new Handler();

        mMediaHandler.post(() -> {
            if (isReleased) {
                return;
            }

            IjkMediaPlayer player = new IjkMediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 1024);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);

            player.setOnPreparedListener(JZMediaIjk.this);
            player.setOnVideoSizeChangedListener(JZMediaIjk.this);
            player.setOnCompletionListener(JZMediaIjk.this);
            player.setOnErrorListener(JZMediaIjk.this);
            player.setOnInfoListener(JZMediaIjk.this);
            player.setOnBufferingUpdateListener(JZMediaIjk.this);
            player.setOnSeekCompleteListener(JZMediaIjk.this);
            player.setOnTimedTextListener(JZMediaIjk.this);

            try {
                if (isReleased) {
                    player.release();
                    return;
                }

                player.setDataSource(jzvd.jzDataSource.getCurrentUrl().toString());
                player.setScreenOnWhilePlaying(true);

                SurfaceTexture texture = resolveSurfaceTexture();
                if (texture != null) {
                    player.setSurface(new Surface(texture));
                }

                ijkMediaPlayer = player;
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                player.release();
                if (ijkMediaPlayer == player) {
                    ijkMediaPlayer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.release();
                if (ijkMediaPlayer == player) {
                    ijkMediaPlayer = null;
                }
            }
        });
    }

    private SurfaceTexture resolveSurfaceTexture() {
        if (SAVED_SURFACE != null) {
            return SAVED_SURFACE;
        }
        if (jzvd.textureView != null) {
            return jzvd.textureView.getSurfaceTexture();
        }
        return null;
    }

    @Override
    public void pause() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.pause();
        }
    }

    @Override
    public boolean isPlaying() {
        return ijkMediaPlayer != null && ijkMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.seekTo(time);
        }
    }

    @Override
    public void release() {
        isReleased = true;
        Handler tmpHandler = mMediaHandler;
        HandlerThread tmpHandlerThread = mMediaHandlerThread;
        IjkMediaPlayer tmpMediaPlayer = ijkMediaPlayer;
        JZMediaInterface.SAVED_SURFACE = null;
        mMediaHandler = null;
        mMediaHandlerThread = null;
        ijkMediaPlayer = null;

        if (tmpHandler != null) {
            tmpHandler.removeCallbacksAndMessages(null);
        }

        if (tmpMediaPlayer != null) {
            try {
                tmpMediaPlayer.pause();
                tmpMediaPlayer.stop();
            } catch (Exception ignored) {
            }

            if (tmpHandler != null) {
                tmpHandler.post(() -> {
                    tmpMediaPlayer.setSurface(null);
                    tmpMediaPlayer.release();
                    if (tmpHandlerThread != null) {
                        tmpHandlerThread.quit();
                    }
                });
            } else {
                tmpMediaPlayer.setSurface(null);
                tmpMediaPlayer.release();
                if (tmpHandlerThread != null) {
                    tmpHandlerThread.quit();
                }
            }
        } else if (tmpHandlerThread != null) {
            if (tmpHandler != null) {
                tmpHandler.post(tmpHandlerThread::quit);
            } else {
                tmpHandlerThread.quit();
            }
        }
    }

    @Override
    public long getCurrentPosition() {
        if (ijkMediaPlayer == null) return 0;
        return ijkMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (ijkMediaPlayer == null) return 0;
        return ijkMediaPlayer.getDuration();
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    @Override
    public void setSpeed(float speed) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setSpeed(speed);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (isReleased || handler == null) return;
        handler.post(() -> {
            if (!isReleased) {
                jzvd.onPrepared();
            }
        });
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        if (isReleased || handler == null) return;
        handler.post(() -> jzvd.onVideoSizeChanged(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight()));
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        if (isReleased || handler == null) return true;
        handler.post(() -> jzvd.onError(what, extra));
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        if (isReleased || handler == null) return false;
        handler.post(() -> jzvd.onInfo(what, extra));
        return false;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, final int percent) {
        if (isReleased || handler == null) return;
        handler.post(() -> jzvd.setBufferProgress(percent));
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        if (isReleased || handler == null) return;
        handler.post(() -> jzvd.onSeekComplete());
    }

    @Override
    public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {

    }

    @Override
    public void setSurface(Surface surface) {
        if (ijkMediaPlayer != null && surface != null) {
            ijkMediaPlayer.setSurface(surface);
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

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (isReleased || handler == null) return;
        handler.post(() -> jzvd.onCompletion());
    }
}
