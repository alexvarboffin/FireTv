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
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.DetailsActivity.Companion.newInstance
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity.Companion.playerIntent
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityChannelsBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class ChannelActivity : BaseActivity(), ChannelAdapter.OnItemClickListener {
    var prf: PrefManager? = null

    //DatabaseReference wallpaperReference;
    //List<Channel> favList;
    var channelAdapter: ChannelAdapter? = null

    var categoryName: String? = null
    private val TAG: String = ChannelActivity::class.java.simpleName
    var adNetwork: AdNetwork? = null
    var adsPref: AdsPref? = null

    private var binding: ActivityChannelsBinding? = null
    private var presenter: AllChannelPresenter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируйте ViewBinding
        binding = ActivityChannelsBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        val handler = Handler(Looper.getMainLooper())
        presenter = AllChannelPresenter(handler, this)

        prf = PrefManager(this)
        handleIntent0()

        setTitle(categoryName)
        setSupportActionBar(binding!!.toolbar)

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)

        adsPref = AdsPref(this)
        adNetwork = AdNetwork(this)
        adNetwork!!.loadBannerAdNetwork(Constant.BANNER_HOME)
        adNetwork!!.loadInterstitialAdNetwork(Constant.INTERSTITIAL_POST_LIST)

        handleRefresh()

        loadPictures()

        channelAdapter!!.setOnItemClickListener(this)
    }


    private fun handleIntent0() {
        val intent = getIntent()
        categoryName = intent.getStringExtra(Const.KEY_CATEGOTY_NAME)
    }

    fun showInterstitialAd() {
        adNetwork!!.showInterstitialAdNetwork(
            Constant.INTERSTITIAL_POST_LIST,
            adsPref!!.getInterstitialAdInterval()
        )
    }

    private fun loadPictures() {
//        favList = new ArrayList<>();
//        wallpaperList = new ArrayList<>();

        binding!!.recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(this, prf!!.getInt(Const.KEY_CHANNEL_COLUMNS))
        binding!!.recyclerView.setLayoutManager(gridLayoutManager)

        channelAdapter = ChannelAdapter(this, ArrayList<Channel>())
        binding!!.recyclerView.setAdapter(channelAdapter)


        fetchWallpapers(categoryName!!)
    }


    private fun fetchWallpapers(categoryName: String) {
        val handler = Handler(Looper.getMainLooper())
        presenter!!.getChannelsInCategory(
            categoryName,
            object : RepoCallback<List<Channel>> {
                override fun successResult(data: List<Channel>) {
                    if (data.isEmpty()) {
                        binding!!.lytNoItem.getRoot().visibility = View.VISIBLE
                    }
                    channelAdapter!!.swapData(data)
                }

                override fun errorResult(err: String) {
                }
            })
    }

    //    private void fetchWallpapers(final String categoryName) {
    //        wallpaperReference = FirebaseDatabase.getInstance().getReference("Channels");
    //        wallpaperReference.addListenerForSingleValueEvent(new ValueEventListener() {
    //            @Override
    //            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    //                showRefresh(false);
    //                if (dataSnapshot.exists()) {
    //                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {
    //                        binding.lytNoItem.getRoot().setVisibility(View.GONE);
    //                        String id = wallpaperSnapshot.child("id").getValue(String.class);
    //                        String wallpaper = wallpaperSnapshot.child(Const.CH_IMG).getValue(String.class);
    //                        String name = wallpaperSnapshot.child(Const.CH_NAME).getValue(String.class);
    //                        String category = wallpaperSnapshot.child(Const.CH_CAT).getValue(String.class);
    //                        String type = wallpaperSnapshot.child(Const.CH_TYPE).getValue(String.class);
    //                        String link = wallpaperSnapshot.child(Const.CH_LNK).getValue(String.class);
    //                        String desc = wallpaperSnapshot.child(Const.CH_DESC).getValue(String.class);
    //                        String language = wallpaperSnapshot.child(Const.CH_LANG).getValue(String.class);
    //
    //                        Channel wallpaper1 = new Channel(id, wallpaper, name, category, type, link, desc, language);
    //
    //                        favList.add(0, wallpaper1);
    //                    }
    //                    for (int i = 0; i < favList.size(); i++) {
    //                        if (favList.get(i).getCat().equals(categoryName)) {
    //                            wallpaperList.add(favList.get(i));
    //                            if (wallpaperList.size() == 0) {
    //                                Toast.makeText(ChannelActivity.this, "NO DATA", Toast.LENGTH_SHORT).show();
    //                            }
    //                        }
    //                    }
    //                    if (wallpaperList.size() == 0) {
    //                        binding.lytNoItem.getRoot().setVisibility(View.VISIBLE);
    //                    }
    //                    channelAdapter.notifyDataSetChanged();
    //                } else {
    //                    binding.lytNoItem.getRoot().setVisibility(View.VISIBLE);
    //                }
    //            }
    //
    //            @Override
    //            public void onCancelled(DatabaseError databaseError) {
    //                Toast.makeText(ChannelActivity.this, "Empty", Toast.LENGTH_SHORT).show();
    //            }
    //        });
    //    }
    //    private void refreshData() {
    //        wallpaperList.clear();
    //        channelAdapter.notifyDataSetChanged();
    //        new Handler().postDelayed(this::loadPictures, 2000);
    //    }
    private fun handleRefresh() {
//        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());
//        showRefresh(true);
    }

    private fun showRefresh(show: Boolean) {
//        if (show) {
//            binding.swipeRefreshLayout.setRefreshing(true);
//        } else {
//            new Handler().postDelayed(() -> {
//                binding.swipeRefreshLayout.setRefreshing(false)
//            }, 500);
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        initCheck()
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
        val isDetailsMode = prf!!.isDetailsMode()
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