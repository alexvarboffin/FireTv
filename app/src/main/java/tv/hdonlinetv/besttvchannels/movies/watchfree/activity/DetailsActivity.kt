package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.GsonBuilder
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.AllChannelPresenter
import com.walhalla.data.repository.RepoCallback
import com.walhalla.ui.DLog.d
import com.walhalla.ui.DLog.handleException
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity.Companion.playerIntent
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityDetailsBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class DetailsActivity : BaseActivity(), RepoCallback<Channel> {
    var channelId: Long = 0


    private var prf: PrefManager? = null
    private var mMenuItem: Menu? = null
    private var binding: ActivityDetailsBinding? = null

    private var adNetwork: AdNetwork? = null
    private var adsPref: AdsPref? = null
    private val fullscreen = false
    private var presenter: AllChannelPresenter? = null
    private var viewModel: ChannelViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)

        val handler = Handler(Looper.getMainLooper())
        presenter = AllChannelPresenter(handler, this)

        // Инициализация прочих переменных
        prf = PrefManager(this)
        adsPref = AdsPref(this)
        adNetwork = AdNetwork(this)
        adNetwork!!.loadBannerAdNetwork(Constant.BANNER_HOME)
        adNetwork!!.loadInterstitialAdNetwork(Constant.INTERSTITIAL_POST_LIST)

        viewModel = ViewModelProvider(this).get(
            ChannelViewModel::class.java
        )

        handleIntent0()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    private fun loaddata(channelId: Long) {
        if (BuildConfig.DEBUG) {
            d("@@CHANNEL->>@@" + viewModel!!.channel.value + "@@@" + channelId)
        }
        if (channelId > 0) {
            presenter!!.getChannelById(channelId, this)
        }
    }

    private fun updateUI(channel: Channel) {
        if (BuildConfig.DEBUG) {
            d("@@CHANNEL->>@@" + viewModel!!.channel.value)
        }
        // Установите название активности
        title = channel.name
        setMenuIcon(channel.liked > 0)
        binding!!.channelName.text = channel.name

        var dsk = channel.desc
        if (BuildConfig.DEBUG) {
            val gson = GsonBuilder().setPrettyPrinting().create()
            dsk = gson.toJson(channel)
        }
        binding!!.channelDetails.text = dsk


        binding!!.tvCategory.text = "Category: " + channel.cat

        // Загрузка изображения
        val channelImage = channel.cover
        Glide.with(this)
            .load(channelImage)
            .placeholder(R.drawable.placeholder)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    d("@@@@$channelImage")
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
            }).into(binding!!.channelImage)

        // Обработка клика на кнопку воспроизведения
        binding!!.channelPlay.setOnClickListener { view: View? ->
            if (channel.type == "youtube") {
                val separated =
                    channel.lnk!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val intents = Intent(
                    this@DetailsActivity,
                    ActivityYoutubePlayer::class.java
                )
                intents.putExtra("id", separated[1])
                startActivity(intents)
            } else {
                //                //String channelImage0 = channel.getLang();
//                String channelImage0 = channel.getImg();
//
//                Intent intents = PlayerActivity.newInstance0(
//                        this, channel.getName(),
//                        channelImage0, channel.getLnk()
//                );


                val intents = playerIntent(this, channel)
                startActivity(intents)
            }
        }
    }

    private fun handleIntent0() {
        val intent = intent
        if (intent.hasExtra(KEY_INTENT_CHANNEL_ID)) {
            channelId = intent.getLongExtra(KEY_INTENT_CHANNEL_ID, -1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_favorite, menu)
        mMenuItem = menu
        d("@@@")

        if (viewModel == null) {
            return true
        }
        val channel = viewModel!!.channel.value ?: return true
        setMenuIcon(channel.liked > 0)

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

    private fun setMenuIcon(b: Boolean) {
        if (mMenuItem != null) {
            mMenuItem!!.getItem(0).setIcon(
                if (b) R.drawable.ic_favorite_fill_green else R.drawable.ic_favorite_border
            )
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.action_fav) {
            if (viewModel == null) {
                return super.onOptionsItemSelected(item)
            }
            val channel = viewModel!!.channel.value ?: return super.onOptionsItemSelected(item)

            //            presenter.isFavoriteChannel(channel, new RepoCallback<Boolean>() {
//                @Override
//                public void successResult(Boolean isFavoriteChannel) {
//                    if (!isFavoriteChannel) {
//                        mMenuItem.getItem(0).setIcon(R.drawable.ic_favorite_fill);
//                        Toast.makeText(DetailsActivity.this, R.string.add_fav, Toast.LENGTH_SHORT).show();
//                        try {
            /*                    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
            if (channel.liked <= 0) {
                Toast.makeText(this@DetailsActivity, R.string.add_fav, Toast.LENGTH_SHORT).show()
                try {
//                    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
//                    DLog.d(GSON.toJson(channel));
                    presenter!!.addFavorite(channel, object : RepoCallback<Int> {
                        override fun successResult(data: Int) {
                            setMenuIcon(true)
                        }

                        override fun errorResult(err: String) {
                        }
                    })
                } catch (e: Exception) {
                    handleException(e)
                }
            } else {
                setMenuIcon(false)
                Toast.makeText(this@DetailsActivity, R.string.remove_fav, Toast.LENGTH_SHORT).show()
                presenter!!.deleteFavorite(channel)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        d("@@@")
        initCheck()
        loaddata(channelId)
    }

    private fun initCheck() {
        if (prf!!.loadNightModeState()) {
            Log.d("Dark", "MODE")
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // set status text dark
        }
    }

    override fun successResult(channel: Channel) {
        viewModel!!.channel.observe(
            this
        ) { channel: Channel -> this.updateUI(channel) }
        viewModel!!.setChannel(channel)
    }

    override fun errorResult(err: String) {
    }

    companion object {
        private const val KEY_INTENT_CHANNEL_ID = "channel_id"
        @JvmStatic
        fun newInstance(context: Context, channelId: Long): Intent {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra(KEY_INTENT_CHANNEL_ID, channelId)
            return intent
        }
    }
}
