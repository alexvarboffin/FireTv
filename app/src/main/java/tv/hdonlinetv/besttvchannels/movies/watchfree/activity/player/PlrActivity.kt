package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import cn.jzvd.JZDataSource
import cn.jzvd.JZMediaSystem
import cn.jzvd.Jzvd
import cn.jzvd.demo.CustomMedia.JZMediaExo
import cn.jzvd.demo.CustomMedia.JZMediaIjk
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.google.common.net.HttpHeaders
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.AllChannelPresenter
import com.walhalla.data.repository.RepoCallback
import com.walhalla.ui.DLog.d
import com.walhalla.ui.DLog.handleException
import org.json.JSONObject
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.BaseActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.ChannelViewModel
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPlayerBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager
import androidx.core.view.get

class PlrActivity : BaseActivity() {
    private var castContext: CastContext? = null
    private var castSession: CastSession? = null
    private var remoteMediaClient: RemoteMediaClient? = null

    private val sessionManagerListener: SessionManagerListener<CastSession> =
        object : MySessionManagerListener() {
            override fun onSessionEnded(session: CastSession, error: Int) {
                remoteMediaClient = null
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this@PlrActivity, "SessionEnded", Toast.LENGTH_SHORT).show()
                }
                this@PlrActivity.castSession = null
            }

            override fun onSessionStarted(session: CastSession, s: String) {
                castSession = session
                //if (BuildConfig.DEBUG) {
                d("{{SessionStarted}}..... " + session.isConnected)
                //}
                remoteMediaClient = session.remoteMediaClient
                playMedia()
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                castSession = session
                if (BuildConfig.DEBUG) {
                    Toast.makeText(
                        this@PlrActivity,
                        "SessionResumed: " + castSession!!.isConnected,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                playMedia()
            }
        }

    private var binding: ActivityPlayerBinding? = null


    private var pr: PrefManager? = null
    private var mMenuItem: Menu? = null


    private var presenter: AllChannelPresenter? = null
    private var channel: Channel? = null

    private val addToFavoriteCallback: RepoCallback<Int> = object : RepoCallback<Int> {
        override fun successResult(data: Int) {
            if (data > 0) {
                setMenuIcon(true)
                Toast.makeText(this@PlrActivity, R.string.add_fav, Toast.LENGTH_SHORT).show()
            }
        }

        override fun errorResult(err: String) {
        }
    }
    private var viewModel: ChannelViewModel? = null
    private var headers: HashMap<String, String>? = null
    private var customData: JSONObject? = null


    private fun handleIntent0() {
        val intent = intent
        channel = intent.getSerializableExtra(EXTRA_OBJ) as Channel?
        //mUserAgent = intent.getStringExtra(HttpHeaders.USER_AGENT);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        pr = PrefManager(this)
        val handler = Handler(Looper.getMainLooper())
        presenter = AllChannelPresenter(handler, this)
        setSupportActionBar(binding!!.toolbar)

        castContext = CastContext.getSharedInstance(this)
        CastButtonFactory.setUpMediaRouteButton(applicationContext, binding!!.mediaRouteButton)


        viewModel = ViewModelProvider(this).get(ChannelViewModel::class.java)

        //viewModel = new ViewModelProvider(getApplication()).get(ChannelViewModel.class);


        handleIntent0()

        viewModel!!.channel.observe(this) { channel: Channel -> this.updateUI(channel) }
        channel?.let { viewModel!!.setChannel(it) }

        d("@@CHANNEL -- ->>@@" + viewModel!!.channel.value?.tvgUrl)


        val channelLink = channel!!.lnk
        val channelImage = channel!!.cover

        //setTitle((BuildConfig.DEBUG) ? channelLink : null);
        title = if (channel!!.name != null) channel!!.name else channelLink

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


//        if (BuildConfig.DEBUG) {
//            Toast.makeText(this, channelLink.toString(), Toast.LENGTH_SHORT).show()
//        }
        Glide.with(this)
            .load(channelImage)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    d("@@@@Image load failed: $channelImage")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            }).into(binding!!.videoPlayer.posterImageView)

        val mm = Jzvd.SCREEN_NORMAL

        //int mm = Jzvd.SCREEN_LIST;
//        int mm = Jzvd.SCREEN_FULLSCREEN;
//        int mm = Jzvd.SCREEN_TINY;
        val tmpUa =
            if (TextUtils.isEmpty(channel!!.extUserAgent)) channel!!.ua else channel!!.extUserAgent

        headers = HashMap()
        if (tmpUa!=null&&!TextUtils.isEmpty(tmpUa)) {
            headers!![HttpHeaders.USER_AGENT] = tmpUa!!
        }
        if (channel!!.extReferer!=null&&!TextUtils.isEmpty(channel!!.extReferer)) {
            headers!!["Referer"] = channel!!.extReferer!!
        }

        //binding.videoPlayer.setUp(channelLink, channelName, mm);
        val jzDataSource = JZDataSource(channelLink, channel!!.name)
        val mediaPlayerOption = pr!!.mediaPlayerOption
        if (headers!!.isEmpty()) {
            //Without custom header
            setUp00(jzDataSource, mm, mediaPlayerOption)
        } else {
            // Создаем объект JZDataSource с заголовками
            d("@w@$headers")
            jzDataSource.headerMap = headers
            setUp00(jzDataSource, mm, mediaPlayerOption)


            customData = JSONObject()
            for ((key, value) in headers!!) {
                try {
                    customData!!.put(key, value)
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        }


        //@@@binding.videoPlayer.startVideo();
        //@@@@binding.videoPlayer.startWindowFullscreen();
        //@@@@binding.videoPlayer.startWindowTiny();
        if (Const.DEBUG) {
            binding!!.changeToIjkplayer.setOnClickListener { v: View? ->
                setUp00(jzDataSource, mm, 3)
            }
            binding!!.changeToSystemMediaplayer.setOnClickListener { v: View? ->
                setUp00(jzDataSource, mm, 0)
            }
            binding!!.changeToExo.setOnClickListener { v: View? ->
                setUp00(jzDataSource, mm, 2)
            }
            binding!!.changeToAliyun.setOnClickListener { v: View? ->
                setUp00(jzDataSource, mm, 1)
            }
        } else {
            binding!!.changeToIjkplayer.visibility = View.GONE
            binding!!.changeToSystemMediaplayer.visibility = View.GONE
            binding!!.changeToExo.visibility = View.GONE
            binding!!.changeToAliyun.visibility = View.GONE
        }
    }


    private fun mmm() {
        val movieMetadata = MediaMetadata(
            MediaMetadata.MEDIA_TYPE_MOVIE //MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
        )

        movieMetadata.putString(MediaMetadata.KEY_TITLE, channel!!.name)
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, channel!!.cat?:"")
        movieMetadata.addImage(WebImage(Uri.parse(channel!!.cover)))


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
        val mediaInfo = if (customData == null) {
            MediaInfo.Builder(channel!!.lnk?:"")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED) //.setContentUrl(channel.getLnk())
                .setContentType("application/x-mpegURL") // Убедитесь, что это тип контента для M3U8
                .setMetadata(movieMetadata) // Добавьте метаданные, если необходимо
                .build()
        } else {
            MediaInfo.Builder(channel!!.lnk?:"")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED) //.setContentUrl(channel.getLnk())
                .setContentType("application/x-mpegURL") // Убедитесь, что это тип контента для M3U8
                .setMetadata(movieMetadata) // Добавьте метаданные, если необходимо
                .setCustomData(customData)
                .build()
        }
        remoteMediaClient = castSession!!.remoteMediaClient
        if (remoteMediaClient != null) {
            //remoteMediaClient.load(mediaInfo, true, 0);
            remoteMediaClient!!.load(MediaLoadRequestData.Builder().setMediaInfo(mediaInfo).build())
        }
    }

    private fun updateUI(channel: Channel) {
        d("@@CHANNEL->>@@" + viewModel!!.channel.value)
    }


    private fun setUp00(jzDataSource: JZDataSource, mm: Int, mediaPlayerOption: Int) {
        if (mediaPlayerOption == 0) {
            clickChangeToSystem(jzDataSource, mm)
        } else if (mediaPlayerOption == 1) {
            clickChangeToAliyun(jzDataSource, mm)
        } else if (mediaPlayerOption == 2) {
            clickChangeToExo(jzDataSource, mm)
        } else if (mediaPlayerOption == 3) {
            clickChangeToIjkplayer(jzDataSource, mm)
        } else {
            // Инициализация по умолчанию, если нужно
            binding!!.videoPlayer.setUp(jzDataSource, mm, JZMediaSystem::class.java) //default
        }
        //binding.videoPlayer.setUp(channelLink, channelName, mm, JZMediaExo.class);
    }

    fun clickChangeToIjkplayer(jzDataSource: JZDataSource?, mm: Int) {
        Jzvd.releaseAllVideos()
        binding!!.videoPlayer.setUp(jzDataSource, mm, JZMediaIjk::class.java)
        binding!!.videoPlayer.startVideo()
        Toast.makeText(this, "Change to Ijkplayer", Toast.LENGTH_SHORT).show()
    }


    fun clickChangeToAliyun(jzDataSource: JZDataSource?, mm: Int) {
//        Jzvd.releaseAllVideos();
//        binding.videoPlayer.setUp(jzDataSource, mm, CustomMedia.JZMediaAliyun.class);
//        binding.videoPlayer.startVideo();
//        Toast.makeText(this, "Change to AliyunPlayer", Toast.LENGTH_SHORT).show();
    }

    //setUp(new JZDataSource(url, title), screen, mediaInterfaceClass)
    fun clickChangeToSystem(jzDataSource: JZDataSource?, mm: Int) {
        Jzvd.releaseAllVideos()
        binding!!.videoPlayer.setUp(jzDataSource, mm, JZMediaSystem::class.java)
        binding!!.videoPlayer.startVideo()
        Toast.makeText(this, "Change to MediaPlayer", Toast.LENGTH_SHORT).show()
    }

    fun clickChangeToExo(jzDataSource: JZDataSource?, mm: Int) {
        Jzvd.releaseAllVideos()
        binding!!.videoPlayer.setUp(jzDataSource, mm, JZMediaExo::class.java)
        binding!!.videoPlayer.startVideo()
        //Toast.makeText(this, "Change to ExoPlayer", Toast.LENGTH_SHORT).show();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_player, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.media_route_menu_item
        )

        mMenuItem = menu

        setMenuIcon(channel!!.liked > 0)

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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.action_fav) {
            //            presenter.isFavoriteChannel(channel, new RepoCallback<Boolean>() {
//                @Override
//                public void successResult(Boolean isFavoriteChannel) {
//                    if (!isFavoriteChannel) {
//                        mMenuItem.getItem(0).setIcon(R.drawable.ic_favorite_fill);
//                        Toast.makeText(DetailsActivity.this, R.string.add_fav, Toast.LENGTH_SHORT).show();
//                        try {
            /*                   Gson GSON = new GsonBuilder().setPrettyPrinting().create();
            * /                    DLog.d(GSON.toJson(channel)); */
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

            if (channel!!.liked <= 0) {
                try {
//                    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
//                    DLog.d(GSON.toJson(channel));
                    presenter!!.addFavorite(channel!!, addToFavoriteCallback)
                } catch (e: Exception) {
                    handleException(e)
                }
            } else {
                setMenuIcon(false)
                Toast.makeText(this, R.string.remove_fav, Toast.LENGTH_SHORT).show()
                presenter!!.deleteFavorite(channel!!)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setMenuIcon(b: Boolean) {
        mMenuItem!![0].setIcon(
            if (b) R.drawable.ic_favorite_fill_red else R.drawable.ic_favorite_border_red
        )
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }


    override fun onResume() {
        super.onResume()

        // Register session manager listener
        castContext!!.sessionManager.addSessionManagerListener(
            sessionManagerListener,
            CastSession::class.java
        )
        val tmp = castContext!!.sessionManager.currentCastSession
        if (tmp != null) {
            castSession = tmp
        }

        if (BuildConfig.DEBUG) {
            Toast.makeText(this, castSession.toString(), Toast.LENGTH_SHORT).show()
        }
        playMedia()
    }

    private fun playMedia() {
        //Toast.makeText(this, "@[connected]@" + isConnectedOrNot(), Toast.LENGTH_SHORT).show();
        if (isConnectedOrNot) {
            d("Connected to Chromecast, starting media playback...")
            mmm()
        } else {
            d("Not connected to Chromecast.")
            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "Chromecast not connected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val isConnectedOrNot: Boolean
        get() {
//        if (castSession == null) {
//            return false;
//        }
//        return castSession.isConnected();
            val session = castContext!!.sessionManager.currentCastSession
            if (BuildConfig.DEBUG) {
                Toast.makeText(
                    this,
                    "00000 $session",
                    Toast.LENGTH_SHORT
                ).show()
            }
            d("00000 $castSession")

            return session != null && session.isConnected
        }

    override fun onPause() {
        castContext!!.sessionManager.removeSessionManagerListener(
            sessionManagerListener,
            CastSession::class.java
        )
        super.onPause()
        Jzvd.goOnPlayOnPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        Jzvd.releaseAllVideos()
    }

    companion object {
        private const val EXTRA_OBJ = "liked"


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
        @JvmStatic
        fun playerIntent(context: Context?, channel: Channel?): Intent {
            val intent = Intent(context, PlrActivity::class.java)
            //String channelImage0 = channel.getLang();
            //String channelImage0 = channel.getImg();
//        String ua = TextUtils.isEmpty(channel.extUserAgent) ? channel.getUa() : channel.extUserAgent;
//        intent.putExtra(HttpHeaders.USER_AGENT, ua);
            intent.putExtra(EXTRA_OBJ, channel)
            return intent
        }
    }
}
