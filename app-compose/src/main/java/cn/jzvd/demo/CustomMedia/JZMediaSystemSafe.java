package cn.jzvd.demo.CustomMedia;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.Surface;

import cn.jzvd.JZMediaSystem;
import cn.jzvd.Jzvd;

/**
 * JZVD {@link JZMediaSystem} calls {@code mediaPlayer.pause()} without a null check.
 * Pause can run on the JZVD thread before prepare finishes or after release (e.g. ON_PAUSE).
 */
public class JZMediaSystemSafe extends JZMediaSystem {

    public JZMediaSystemSafe(Jzvd jzvd) {
        super(jzvd);
    }

    @Override
    public void start() {
        Handler worker = mMediaHandler;
        if (worker == null) {
            return;
        }
        worker.post(() -> {
            MediaPlayer player = mediaPlayer;
            if (player == null) {
                return;
            }
            try {
                player.start();
            } catch (IllegalStateException ignored) {
            }
        });
    }

    @Override
    public void pause() {
        Handler worker = mMediaHandler;
        if (worker == null) {
            return;
        }
        worker.post(() -> {
            MediaPlayer player = mediaPlayer;
            if (player == null) {
                return;
            }
            try {
                player.pause();
            } catch (IllegalStateException ignored) {
            }
        });
    }

    @Override
    public boolean isPlaying() {
        MediaPlayer player = mediaPlayer;
        if (player == null) {
            return false;
        }
        try {
            return player.isPlaying();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    public void seekTo(long time) {
        Handler worker = mMediaHandler;
        if (worker == null) {
            return;
        }
        worker.post(() -> {
            MediaPlayer player = mediaPlayer;
            if (player == null) {
                return;
            }
            try {
                player.seekTo((int) time);
            } catch (IllegalStateException ignored) {
            }
        });
    }

    @Override
    public void setSurface(Surface surface) {
        MediaPlayer player = mediaPlayer;
        if (player == null || surface == null) {
            return;
        }
        try {
            player.setSurface(surface);
        } catch (IllegalStateException ignored) {
        }
    }
}
