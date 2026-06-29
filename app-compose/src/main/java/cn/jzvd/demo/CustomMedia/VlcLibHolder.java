package cn.jzvd.demo.CustomMedia;

import android.content.Context;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;

/**
 * Shared LibVLC instance. Options aligned with cinema notKmp {@code VlcPlayerComponent}.
 */
public final class VlcLibHolder {

    private static LibVLC libVlc;

    private VlcLibHolder() {
    }

    /** Call from a background thread during app startup to avoid first-play jank. */
    public static void warmUp(Context context) {
        get(context);
    }

    static synchronized LibVLC get(Context context) {
        if (libVlc == null) {
            ArrayList<String> options = new ArrayList<>();
            options.add("--avcodec-hw=none");
            options.add("--no-drop-late-frames");
            options.add("--no-skip-frames");
            options.add("--rtsp-tcp");
            options.add("--live-caching=300");
            libVlc = new LibVLC(context.getApplicationContext(), options);
        }
        return libVlc;
    }
}
