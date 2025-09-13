package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZMediaSystem;
import cn.jzvd.Jzvd;

import cn.jzvd.demo.CustomMedia.JZMediaExo;

import cn.jzvd.demo.CustomMedia.JZMediaIjk;
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.BaseActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.ChannelViewModel;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPlayerBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.common.net.HttpHeaders;
import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.AllChannelPresenter;
import com.walhalla.data.repository.RepoCallback;
import com.walhalla.ui.DLog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PlrActivity extends BaseActivity {


    private static final String EXTRA_OBJ = "liked";


    private CastContext castContext;
    private CastSession castSession = null;
    private RemoteMediaClient remoteMediaClient;

    private final SessionManagerListener<CastSession> sessionManagerListener = new MySessionManagerListener() {

        @Override
        public void onSessionEnded(@NonNull CastSession session, int error) {
            remoteMediaClient = null;
            if (BuildConfig.DEBUG) {
                Toast.makeText(PlrActivity.this, "SessionEnded", Toast.LENGTH_SHORT).show();
            }
            PlrActivity.this.castSession = null;
        }

        @Override
        public void onSessionStarted(@NonNull CastSession session, @NonNull String s) {
            castSession = session;
            //if (BuildConfig.DEBUG) {
            DLog.d("{{SessionStarted}}..... " + session.isConnected());
            //}
            remoteMediaClient = session.getRemoteMediaClient();
            playMedia();
        }

        @Override
        public void onSessionResumed(@NonNull CastSession session, boolean wasSuspended) {
            castSession = session;
            if (BuildConfig.DEBUG) {
                Toast.makeText(PlrActivity.this,
                        "SessionResumed: " + castSession.isConnected(),
                        Toast.LENGTH_SHORT).show();
            }
            playMedia();
        }

    };

    private ActivityPlayerBinding binding;


    private PrefManager pr;
    private Menu mMenuItem;


    private AllChannelPresenter presenter;
    private Channel channel;

    private final RepoCallback<Integer> addToFavoriteCallback = new RepoCallback<>() {
        @Override
        public void successResult(Integer data) {
            if (data > 0) {
                setMenuIcon(true);
                Toast.makeText(PlrActivity.this, R.string.add_fav, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void errorResult(String err) {

        }
    };
    private ChannelViewModel viewModel;
    private HashMap<String, String> headers;
    private JSONObject customData = null;


    private void handleIntent0() {
        Intent intent = getIntent();
        channel = (Channel) intent.getSerializableExtra(EXTRA_OBJ);
        //mUserAgent = intent.getStringExtra(HttpHeaders.USER_AGENT);
    }

//    public static Intent newInstance0(Context context, String name, String image, String link) {
//        Intent intent = new Intent(context, PlayerActivity.class);
//        intent.putExtra(EXTRA_NAME, name);
//        intent.putExtra(EXTRA_IMAGE, image);
//        intent.putExtra(EXTRA_LINK, link);
//
//        intent.putExtra(EXTRA_EXT_UA, ,,,);
//        intent.putExtra(EXTRA_EXT_REF, ,,,);
//
//        return intent;
//    }

    public static Intent playerIntent(Context context, Channel channel) {
        Intent intent = new Intent(context, PlrActivity.class);
        //String channelImage0 = channel.getLang();
        //String channelImage0 = channel.getImg();
//        String ua = TextUtils.isEmpty(channel.extUserAgent) ? channel.getUa() : channel.extUserAgent;
//        intent.putExtra(HttpHeaders.USER_AGENT, ua);
        intent.putExtra(EXTRA_OBJ, channel);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pr = new PrefManager(this);
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new AllChannelPresenter(handler, this);
        setSupportActionBar(binding.toolbar);

        castContext = CastContext.getSharedInstance(this);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), binding.mediaRouteButton);


        viewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        //viewModel = new ViewModelProvider(getApplication()).get(ChannelViewModel.class);

        DLog.d("@@CHANNEL -- ->>@@" + viewModel.getChannel().getValue());

        handleIntent0();

        viewModel.getChannel().observe(this, this::updateUI);
        viewModel.setChannel(channel);

        String channelLink = channel.getLnk();
        String channelImage = channel.getCover();

        //setTitle((BuildConfig.DEBUG) ? channelLink : null);
        setTitle((channel.getName() != null) ? channel.getName() : channelLink);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (BuildConfig.DEBUG) {
            Toast.makeText(this, String.valueOf(channelLink), Toast.LENGTH_SHORT).show();
        }
        Glide.with(this)
                .load(channelImage)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        DLog.d("@@@@Image load failed: " + channelImage);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, @NonNull Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(binding.videoPlayer.posterImageView);

        int mm = Jzvd.SCREEN_NORMAL;
//int mm = Jzvd.SCREEN_LIST;
//        int mm = Jzvd.SCREEN_FULLSCREEN;
//        int mm = Jzvd.SCREEN_TINY;

        String tmpUa = TextUtils.isEmpty(channel.extUserAgent) ? channel.getUa() : channel.extUserAgent;

        headers = new HashMap<>();
        if (!TextUtils.isEmpty(tmpUa)) {
            headers.put(HttpHeaders.USER_AGENT, tmpUa);
        }
        if (!TextUtils.isEmpty(channel.extReferer)) {
            headers.put("Referer", channel.extReferer);
        }

        //binding.videoPlayer.setUp(channelLink, channelName, mm);
        JZDataSource jzDataSource = new JZDataSource(channelLink, channel.getName());
        int mediaPlayerOption = pr.getMediaPlayerOption();
        if (headers.isEmpty()) {
            //Without custom header
            setUp00(jzDataSource, mm, mediaPlayerOption);
        } else {
            // Создаем объект JZDataSource с заголовками
            DLog.d("@w@" + headers);
            jzDataSource.headerMap = headers;
            setUp00(jzDataSource, mm, mediaPlayerOption);


            customData = new JSONObject();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                try {
                    customData.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    DLog.handleException(e);
                }
            }
        }


        //@@@binding.videoPlayer.startVideo();
        //@@@@binding.videoPlayer.startWindowFullscreen();
        //@@@@binding.videoPlayer.startWindowTiny();

        if (Const.DEBUG) {
            binding.changeToIjkplayer.setOnClickListener(v -> {
                setUp00(jzDataSource, mm, 3);
            });
            binding.changeToSystemMediaplayer.setOnClickListener(v -> {
                setUp00(jzDataSource, mm, 0);
            });
            binding.changeToExo.setOnClickListener(v -> {
                setUp00(jzDataSource, mm, 2);
            });
            binding.changeToAliyun.setOnClickListener(v -> {
                setUp00(jzDataSource, mm, 1);
            });
        } else {
            binding.changeToIjkplayer.setVisibility(View.GONE);
            binding.changeToSystemMediaplayer.setVisibility(View.GONE);
            binding.changeToExo.setVisibility(View.GONE);
            binding.changeToAliyun.setVisibility(View.GONE);
        }
    }


    private void mmm() {
        MediaMetadata movieMetadata = new MediaMetadata(
                MediaMetadata.MEDIA_TYPE_MOVIE
                //MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
        );

        movieMetadata.putString(MediaMetadata.KEY_TITLE, channel.getName());
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, channel.getCat());
        movieMetadata.addImage(new WebImage(Uri.parse(channel.getCover())));
//  @          movieMetadata.addImage(new WebImage(Uri.parse(mSelectedMedia.getImage(1))));

//            MediaInfo mediaInfo = new MediaInfo.Builder(channel.getLnk())
//                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//                    .setContentType(
//                            "video/m3u8"
//                            //"application/x-mpegURL"
//                            //"videos/mp4"
//                    )
//                    .setMetadata(movieMetadata)
//                    //.setStreamDuration(mSelectedMedia.getDuration() * 1000)
//                    .build();


        MediaInfo mediaInfo;
        if (customData == null) {
            mediaInfo = new MediaInfo.Builder(channel.getLnk())
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    //.setContentUrl(channel.getLnk())
                    .setContentType("application/x-mpegURL") // Убедитесь, что это тип контента для M3U8
                    .setMetadata(movieMetadata) // Добавьте метаданные, если необходимо
                    .build();
        } else {
            mediaInfo = new MediaInfo.Builder(channel.getLnk())
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    //.setContentUrl(channel.getLnk())
                    .setContentType("application/x-mpegURL") // Убедитесь, что это тип контента для M3U8
                    .setMetadata(movieMetadata) // Добавьте метаданные, если необходимо
                    .setCustomData(customData)
                    .build();
        }
        remoteMediaClient = castSession.getRemoteMediaClient();
        if (remoteMediaClient != null) {
            //remoteMediaClient.load(mediaInfo, true, 0);
            remoteMediaClient.load(new MediaLoadRequestData.Builder().setMediaInfo(mediaInfo).build());
        }
    }

    private void updateUI(Channel channel) {
        DLog.d("@@CHANNEL->>@@" + viewModel.getChannel().getValue());
    }


    private void setUp00(JZDataSource jzDataSource, int mm, int mediaPlayerOption) {

        if (mediaPlayerOption == 0) {
            clickChangeToSystem(jzDataSource, mm);
        } else if (mediaPlayerOption == 1) {
            clickChangeToAliyun(jzDataSource, mm);
        } else if (mediaPlayerOption == 2) {
            clickChangeToExo(jzDataSource, mm);
        } else if (mediaPlayerOption == 3) {
            clickChangeToIjkplayer(jzDataSource, mm);
        } else {
            // Инициализация по умолчанию, если нужно
            binding.videoPlayer.setUp(jzDataSource, mm, JZMediaSystem.class);//default
        }
        //binding.videoPlayer.setUp(channelLink, channelName, mm, JZMediaExo.class);
    }

    public void clickChangeToIjkplayer(JZDataSource jzDataSource, int mm) {
        Jzvd.releaseAllVideos();
        binding.videoPlayer.setUp(jzDataSource, mm, JZMediaIjk.class);
        binding.videoPlayer.startVideo();
        Toast.makeText(this, "Change to Ijkplayer", Toast.LENGTH_SHORT).show();
    }


    public void clickChangeToAliyun(JZDataSource jzDataSource, int mm) {
//        Jzvd.releaseAllVideos();
//        binding.videoPlayer.setUp(jzDataSource, mm, CustomMedia.JZMediaAliyun.class);
//        binding.videoPlayer.startVideo();
//        Toast.makeText(this, "Change to AliyunPlayer", Toast.LENGTH_SHORT).show();
    }

    //setUp(new JZDataSource(url, title), screen, mediaInterfaceClass)
    public void clickChangeToSystem(JZDataSource jzDataSource, int mm) {
        Jzvd.releaseAllVideos();
        binding.videoPlayer.setUp(jzDataSource, mm, JZMediaSystem.class);
        binding.videoPlayer.startVideo();
        Toast.makeText(this, "Change to MediaPlayer", Toast.LENGTH_SHORT).show();
    }

    public void clickChangeToExo(JZDataSource jzDataSource, int mm) {
        Jzvd.releaseAllVideos();
        binding.videoPlayer.setUp(jzDataSource, mm, JZMediaExo.class);
        binding.videoPlayer.startVideo();
        //Toast.makeText(this, "Change to ExoPlayer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_player, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);

        mMenuItem = menu;

        setMenuIcon(channel.liked > 0);

//        presenter.isFavoriteChannel(channel, new RepoCallback<>() {
//            @Override
//            public void successResult(Boolean isFavoriteChannel) {
//                if (isFavoriteChannel)
//                    mMenuItem.getItem(0).setIcon(R.drawable.ic_favorite_fill);
//                else
//                    --false--
//            }
//
//            @Override
//            public void errorResult(String err) {
//
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_fav) {

//            presenter.isFavoriteChannel(channel, new RepoCallback<Boolean>() {
//                @Override
//                public void successResult(Boolean isFavoriteChannel) {
//                    if (!isFavoriteChannel) {
//                        mMenuItem.getItem(0).setIcon(R.drawable.ic_favorite_fill);
//                        Toast.makeText(DetailsActivity.this, R.string.add_fav, Toast.LENGTH_SHORT).show();
//                        try {
////                    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
////                    DLog.d(GSON.toJson(channel));
//                            repo.addFavorite(channel);
//                        } catch (Exception e) {
//                            DLog.handleException(e);
//                        }
//                    } else {
//                        --false--
//                        Toast.makeText(DetailsActivity.this, R.string.remove_fav, Toast.LENGTH_SHORT).show();
//                        repo.deleteFavorite(channel);
//                    }
//                }
//
//                @Override
//                public void errorResult(String err) {
//
//                }
//            });

            if (channel.liked <= 0) {
                try {
//                    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
//                    DLog.d(GSON.toJson(channel));
                    presenter.addFavorite(channel, addToFavoriteCallback);

                } catch (Exception e) {
                    DLog.handleException(e);
                }
            } else {
                setMenuIcon(false);
                Toast.makeText(this, R.string.remove_fav, Toast.LENGTH_SHORT).show();
                presenter.deleteFavorite(channel);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMenuIcon(boolean b) {
        mMenuItem.getItem(0).setIcon(
                b ? R.drawable.ic_favorite_fill_red : R.drawable.ic_favorite_border_red);
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Register session manager listener
        castContext.getSessionManager().addSessionManagerListener(sessionManagerListener, CastSession.class);
        CastSession tmp = castContext.getSessionManager().getCurrentCastSession();
        if (tmp != null) {
            castSession = tmp;
        }

        if (BuildConfig.DEBUG) {
            Toast.makeText(this, String.valueOf(castSession), Toast.LENGTH_SHORT).show();
        }
        playMedia();

    }

    private void playMedia() {
        //Toast.makeText(this, "@[connected]@" + isConnectedOrNot(), Toast.LENGTH_SHORT).show();
        if (isConnectedOrNot()) {
            DLog.d("Connected to Chromecast, starting media playback...");
            mmm();
        } else {
            DLog.d("Not connected to Chromecast.");
            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "Chromecast not connected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isConnectedOrNot() {
//        if (castSession == null) {
//            return false;
//        }
//        return castSession.isConnected();
        CastSession session = castContext.getSessionManager().getCurrentCastSession();
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "00000 " + session, Toast.LENGTH_SHORT).show();
        }
        DLog.d("00000 " + castSession);

        return session != null && session.isConnected();
    }

    @Override
    protected void onPause() {
        castContext.getSessionManager().removeSessionManagerListener(sessionManagerListener, CastSession.class);
        super.onPause();
        Jzvd.goOnPlayOnPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Jzvd.releaseAllVideos();
    }

}
