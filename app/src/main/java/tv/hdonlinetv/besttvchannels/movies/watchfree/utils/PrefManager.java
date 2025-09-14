package tv.hdonlinetv.besttvchannels.movies.watchfree.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Base64;
import android.view.View;
import android.view.Window;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;

import java.io.UnsupportedEncodingException;

public class PrefManager {
    private static final String PREF_SORT_KEY = "sort_key";

    private static final String PREF_MEDIA_PLAYER_KEY = "media_player_key";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String PREF_NAME = "status_app";

    
    public static final String TYPE_GRID = "Grid";
    public static final String TYPE_LIST = "List";

    private static final String KEY_ACTIVITY_MODE = "activityMode";

    //public static final String URL = "https://yashchouhan.com/verify/api.php?nid=";
    int PRIVATE_MODE = 0;
    String TAG_NIGHT_MODE = "nightmode";
    Context _context;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;


    public PrefManager(Context context) {
        this._context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
        this.sharedPreferences = sharedPreferences;
        this.editor = sharedPreferences.edit();
    }

    public void setBoolean(String str, Boolean bool) {
        this.editor.putBoolean(str, bool.booleanValue());
        this.editor.commit();
    }

    public void setString(String str, String str2) {
        this.editor.putString(str, str2);
        this.editor.commit();
    }

    public static String decodeString(String encoded) {
        byte[] dataDec = Base64.decode(encoded, Base64.DEFAULT);
        String decodedString = "";
        try {

            decodedString = new String(dataDec, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        } finally {

            return decodedString;
        }
    }

    public void setInt(String str, int i) {
        this.editor.putInt(str, i);
        this.editor.commit();
    }

    public void setStatusColor(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public boolean getBoolean(String str) {
        return this.sharedPreferences.getBoolean(str, true);
    }

    public void remove(String str) {
        if (this.sharedPreferences.contains(str)) {
            this.editor.remove(str);
            this.editor.commit();
        }
    }

    public String getString(String str) {
        return this.sharedPreferences.contains(str) ? this.sharedPreferences.getString(str, null) : "";
    }

    public int getInt(String str) {
        return this.sharedPreferences.getInt(str, 3);
    }

    public void setDarkMode(String str) {
        this.editor.putString(this.TAG_NIGHT_MODE, str);
        this.editor.apply();
    }

    //save
    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    //load
    public Boolean loadNightModeState() {
        Boolean state = sharedPreferences.getBoolean("NightMode", false);
        return state;
    }

    public String getChannelDisplayItemType() {
        return sharedPreferences.getString(Const.KEY_COL_COUNT, TYPE_GRID);
    }

    public int getChannelDisplayItemTypeIndex() {
        String m = sharedPreferences.getString(Const.KEY_COL_COUNT, TYPE_GRID);
        if (TYPE_GRID.equals(m)) {
            return 0;
        }
        return 1;
    }


    //sort
    public void saveSortOption(int sortOption) {
        editor.putInt(PREF_SORT_KEY, sortOption);
        editor.apply();
    }

    // Получение сохраненной сортировки
    public int getSortOption() {
        return sharedPreferences.getInt(PREF_SORT_KEY, 0); // 0 - значение по умолчанию
    }



    // Сохранение выбранного типа медиаплеера
    public void saveMediaPlayer(int mediaPlayerOption) {
        editor.putInt(PREF_MEDIA_PLAYER_KEY, mediaPlayerOption);
        editor.apply();
    }

    // Получение сохраненного типа медиаплеера
    public int getMediaPlayerOption() {
        return sharedPreferences.getInt(PREF_MEDIA_PLAYER_KEY, 2); // 0 - значение по умолчанию
    }

    // Метод для сохранения состояния
    public void setActivityMode(boolean isDetailsMode) {
        editor.putBoolean(KEY_ACTIVITY_MODE, isDetailsMode);
        editor.apply();
    }

    // Метод для получения состояния
    public boolean isDetailsMode() {
        return sharedPreferences.getBoolean(KEY_ACTIVITY_MODE, false); // По умолчанию false
    }
}
