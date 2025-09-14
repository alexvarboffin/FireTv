package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.test;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.cast.MediaInfo;

import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.walhalla.ui.DLog;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.MySessionManagerListener;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityMainBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

public class TestActivity extends AppCompatActivity {

    private CastContext castContext;
    private SessionManager sessionManager;
    private RemoteMediaClient remoteMediaClient;

    private static final String M3U8_URL = "https://mn-nl.mncdn.com/alhiwar_live/smil:alhiwar.smil/playlist.m3u8";
    private final SessionManagerListener<CastSession> sessionManagerListener = new MySessionManagerListener() {


        @Override
        public void onSessionStarted(@NonNull CastSession session, @NonNull String s) {
            //if (BuildConfig.DEBUG) {
            DLog.d("{{SessionStarted}}..... " + session.isConnected());
            //}
            remoteMediaClient = session.getRemoteMediaClient();
            loadMedia();
        }

        @Override
        public void onSessionResumed(@NonNull CastSession session, boolean wasSuspended) {

            if (BuildConfig.DEBUG) {
                Toast.makeText(TestActivity.this,
                        "SessionResumed: " //+ remoteMediaClient.sescastSession.isConnected()
                        ,
                        Toast.LENGTH_SHORT).show();
            }
            loadMedia();
        }

        @Override
        public void onSessionEnded(@NonNull CastSession session, int error) {
            //@@@remoteMediaClient = null;
            if (BuildConfig.DEBUG) {
                Toast.makeText(TestActivity.this, "SessionEnded", Toast.LENGTH_SHORT).show();
            }
            remoteMediaClient = null;
        }
    };

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        castContext = CastContext.getSharedInstance(this);
        sessionManager = castContext.getSessionManager();
        sessionManager.addSessionManagerListener(sessionManagerListener, CastSession.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_player, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    private void loadMedia() {
        if (remoteMediaClient != null) {
            MediaInfo mediaInfo = new MediaInfo.Builder(M3U8_URL)
                    .setContentType("application/x-mpegURL") // Укажите корректный MIME-тип
                    .setStreamType(MediaInfo.STREAM_TYPE_LIVE) // Укажите, если поток живой
                    .build();
            MediaLoadOptions loadOptions = new MediaLoadOptions.Builder().setAutoplay(true).build();
            remoteMediaClient.load(mediaInfo, loadOptions);
            Toast.makeText(this, "@@@@", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Не удалось загрузить медиапоток.", Toast.LENGTH_SHORT).show();
        }
    }

//@@@@@@@@@@@@@   @Override
//@@@@@@@@@@@@@   protected void onDestroy() {
//@@@@@@@@@@@@@       super.onDestroy();
//@@@@@@@@@@@@@       sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession.class);
//@@@@@@@@@@@@@   }
}

