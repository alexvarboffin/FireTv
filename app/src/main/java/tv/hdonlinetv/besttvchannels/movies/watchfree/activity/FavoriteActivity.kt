package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import android.R
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.AllChannelPresenter
import com.walhalla.data.repository.RepoCallback
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.DetailsActivity.Companion.newInstance
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity.Companion.playerIntent
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityFavoriteBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class FavoriteActivity : BaseActivity(), ChannelAdapter.OnItemClickListener {
    private var binding: ActivityFavoriteBinding? = null
    private var favoriteAdapter: ChannelAdapter? = null


    private var prf: PrefManager? = null
    private var adNetwork: AdNetwork? = null
    private var adsPref: AdsPref? = null
    private val TAG: String = FavoriteActivity::class.java.getSimpleName()
    private var presenter: AllChannelPresenter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируйте ViewBinding
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())


        // Настройка тулбара
        setSupportActionBar(binding!!.toolbar)
        setTitle("Favorite")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)

        // Инициализация других переменных
        prf = PrefManager(this)
        adsPref = AdsPref(this)
        adNetwork = AdNetwork(this)
        adNetwork!!.loadBannerAdNetwork(Constant.BANNER_HOME)
        adNetwork!!.loadInterstitialAdNetwork(Constant.INTERSTITIAL_POST_LIST)


        // Настройка RecyclerView
        binding!!.rec.setHasFixedSize(true)
        binding!!.rec.setLayoutManager(
            GridLayoutManager(
                this,
                prf!!.getInt(Const.KEY_CHANNEL_COLUMNS)
            )
        )
    }

    fun showInterstitialAd() {
        adNetwork!!.showInterstitialAdNetwork(
            Constant.INTERSTITIAL_POST_LIST,
            adsPref!!.getInterstitialAdInterval()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        initCheck()
        val handler = Handler(Looper.getMainLooper())
        presenter = AllChannelPresenter(handler, this)
        presenter!!.getAllFavorite(object : RepoCallback<MutableList<Channel>> {
            override fun successResult(data: MutableList<Channel>) {
                val mm = data
                favoriteAdapter = ChannelAdapter(applicationContext, mm)
                binding!!.rec.setAdapter(favoriteAdapter)

                // Обработка кликов на элементы
                favoriteAdapter!!.setOnItemClickListener(this@FavoriteActivity)
                binding!!.noFavorite.setVisibility(if (mm.isEmpty()) View.VISIBLE else View.GONE)
                d("@@@@")
            }

            override fun errorResult(err: String) {
            }
        })
    }

    private fun initCheck() {
        if (prf!!.loadNightModeState()) {
            Log.d("Dark", "MODE")
        } else {
            getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) // set status text dark
        }
    }

    override fun onItemClick(view: View, channel: Channel, position: Int) {
        val isDetailsMode = prf!!.isDetailsMode
        if (isDetailsMode) {
            val channelId = channel._id
            val intent = newInstance(this, channelId)
            startActivity(intent)
            showInterstitialAd()
        } else {
            val intents = playerIntent(this, channel)
            startActivity(intents)
        }
    }
}
