package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;
import androidx.multidex.MultiDexApplication;

//import com.iheartradio.m3u8.Encoding;
//import com.iheartradio.m3u8.Format;
//import com.iheartradio.m3u8.ParseException;
//import com.iheartradio.m3u8.PlaylistException;
//import com.iheartradio.m3u8.PlaylistParser;
//import com.iheartradio.m3u8.data.Playlist;

import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class MyApp extends MultiDexApplication {

    private static final String ONESIGNAL_APP_ID = "e712dca5-0a2b-429a-91ce-7e6350069a18";

    public static final String TAG = MyApp.class.getSimpleName();

    private static MyApp mInstance;
    //private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        //OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.getDebug().setLogLevel(LogLevel.DEBUG);


        // OneSignal Initialization
        //OneSignal.initWithContext(this);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        //openPlayList("Euronews.m3u", Format.EXT_M3U);
    }

//    private void openPlayList(String fileName, Format format) {
//        try {
//            InputStream inputStream = this.getAssets().open(fileName);
//            PlaylistParser parser = new PlaylistParser(inputStream, format, Encoding.UTF_8);
//
//            Playlist playlist = parser.parse();
//            if (playlist.hasMasterPlaylist() && playlist.getMasterPlaylist().hasUnknownTags()) {
//                System.err.println(
//                        playlist.getMasterPlaylist().getUnknownTags());
//            } else if (playlist.hasMediaPlaylist() && playlist.getMediaPlaylist().hasUnknownTags()) {
//                System.err.println(
//                        playlist.getMediaPlaylist().getUnknownTags());
//            } else {
//                System.out.println("Parsing without unknown tags successful");
//            }
//            if (inputStream != null) {
//                inputStream.close();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ParseException e) {
//            DLog.handleException(e);
//
////            if(Format.EXT_M3U.equals(format)){
////                openPlayList(fileName, Format.EXT_M3U);
////            }
//
//        } catch (PlaylistException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static synchronized MyApp getInstance() {
        return mInstance;
    }

//    public RequestQueue getRequestQueue() {
//        if (mRequestQueue == null) {
//            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
//        }
//
//        return mRequestQueue;
//    }
//
//    public <T> void addToRequestQueue(Request<T> req, String tag) {
//        // This App is Created by YMG Developers
//        //This Admin panel and 4K Wallpaper app Created by YMG Developers
//        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
//        getRequestQueue().add(req);
//    }
//    public <T> void addToRequestQueue(Request<T> req) {
//        req.setTag(TAG);
//        getRequestQueue().add(req);
//    }
//    public void cancelPendingRequests(Object tag) {
//        if (mRequestQueue != null) {
//            mRequestQueue.cancelAll(tag);
//        }
//    }

}