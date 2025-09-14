package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;

public class TypeUtils {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_M3U_CLOUD, TYPE_M3U_LOCAL, TYPE_M3U_BUFFER, TYPE_XTREAM_URL})
    public @interface M3UType {
    }

    public static final int TYPE_M3U_CLOUD = 0;
    public static final int TYPE_M3U_LOCAL = 1;
    public static final int TYPE_M3U_BUFFER = 2;
    public static final int TYPE_XTREAM_URL = 3;

    private static final Map<Integer, Integer> ICON_MAP = new HashMap<>();
    private static final Map<Integer, Integer> MSG_MAP = new HashMap<>();


    static {
        ICON_MAP.put(TYPE_M3U_CLOUD, R.drawable.ic_cloud);
        ICON_MAP.put(TYPE_M3U_LOCAL, R.drawable.ic_local);
        ICON_MAP.put(TYPE_M3U_BUFFER, R.drawable.ic_buffer);
        ICON_MAP.put(TYPE_XTREAM_URL, R.drawable.ic_xtream); // Default for unhandled types

        MSG_MAP.put(TYPE_M3U_CLOUD, null);
        MSG_MAP.put(TYPE_M3U_LOCAL, null);
        MSG_MAP.put(TYPE_M3U_BUFFER, null);
        MSG_MAP.put(TYPE_XTREAM_URL, R.string.msg_xtream_playlist); // Default for unhandled types


    }


    public static int getIconByType(@M3UType int type) {
        Integer value = ICON_MAP.get(type);
        return (value != null || ICON_MAP.containsKey(type)) ? (value == null ? R.drawable.ic_cloud : value) : R.drawable.ic_cloud;
    }

    public static Integer getMsgByType(@M3UType int type) {
        Integer value = MSG_MAP.get(type);
        return (value != null || MSG_MAP.containsKey(type)) ? (value) : null;
    }

}
