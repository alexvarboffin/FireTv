package tv.hdonlinetv.besttvchannels.movies.watchfree.utils;

import com.walhalla.ui.DLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataUtils {

    public static String convertUnixToDate(String unixSeconds) {
        try {
            long unixTime = Long.parseLong(unixSeconds); // Парсим строку в long
            Date date = new Date(unixTime * 1000L); // умножаем на 1000, чтобы перевести в миллисекунды
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(date);
        } catch (NumberFormatException e) {
            DLog.handleException(e);
            return unixSeconds;
        } catch (IllegalArgumentException e) {
            DLog.handleException(e);
            return unixSeconds;
        } catch (Exception e) {
            DLog.handleException(e);
            return unixSeconds;
        }
    }
}
