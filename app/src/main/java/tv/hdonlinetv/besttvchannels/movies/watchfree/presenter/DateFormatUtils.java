package tv.hdonlinetv.besttvchannels.movies.watchfree.presenter;

import android.util.Log;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatUtils {


    public static String formatUpdateTime(long currentTimeMillis) {
        if (currentTimeMillis <= 0L) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(currentTimeMillis));
    }

    public static long importDate() {
        long currentTimeMillis = System.currentTimeMillis();
        long timestamp = currentTimeMillis;//currentTimeMillis / 1000;
        Log.d("Timestamp", "Unix timestamp: " + timestamp);
        return timestamp;
    }
}
