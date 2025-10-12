package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.AllChannelInPlaylistPresenter
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.DetailsActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity.Companion.playerIntent
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream.CaseChannelListFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager


class AllChannelInPlaylistFragment : CaseChannelListFragment() {
    private val THISCLAZZNAME: String = javaClass.simpleName
    private var binding: FragmentHomeBinding? = null


    //DatabaseReference wallpaperReference;
    var prefManager: PrefManager? = null
    var adNetwork: AdNetwork? = null
    var adsPref: AdsPref? = null


    private var dialog: AlertDialog? = null

    private var lm: GridLayoutManager? = null
    private var presenter: AllChannelInPlaylistPresenter? = null


    private var playlistId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        presenter = AllChannelInPlaylistPresenter(handler, context)
        if (arguments != null) {
            playlistId = requireArguments().getLong(ARG_PLAYLIST_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding!!.getRoot()

        adsPref = AdsPref(activity)
        adNetwork = AdNetwork(activity)
        adNetwork!!.loadInterstitialAdNetwork(Constant.INTERSTITIAL_POST_LIST)

        prefManager = PrefManager(activity)
        showRefresh(true)

        //@@@@@@@@@@@binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());

        //@@@loadPictures();
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Register the MenuProvider
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_latest_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                val id = item.itemId
                if (id == R.id.actionDisplayChannels0) {
                    createAlertDialog(requireContext(), prefManager!!)
                    return true
                }
                //                else if (id == R.id.action_settings) {
//                    // Handle settings action
//                    Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
                return false
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED)
    }

    fun createAlertDialog(context: Context, prf: PrefManager) {
        val values = resources.getStringArray(R.array.layout_options)


        val checkeditem = prf.getChannelDisplayItemTypeIndex()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_title_display_channels)
        builder.setSingleChoiceItems(
            values,
            checkeditem,
            DialogInterface.OnClickListener { dialog: DialogInterface?, item: Int ->
                var type = ""
                when (item) {
                    0 -> {
                        val m = 3
                        type = PrefManager.TYPE_GRID
                        prf.setString(Const.KEY_COL_COUNT, type)
                        prf.setInt(Const.KEY_CHANNEL_COLUMNS, m)
                        resumeAdapter(m, type)
                    }

                    1 -> {
                        val m0 = 1
                        type = PrefManager.TYPE_LIST
                        prf.setString(Const.KEY_COL_COUNT, type)
                        prf.setInt(Const.KEY_CHANNEL_COLUMNS, m0)
                        resumeAdapter(m0, type)
                    }
                }
                this.dialog!!.dismiss()
            })
        dialog = builder.create()
        dialog!!.show()
    }

    private fun resumeAdapter(m: Int, type: String?) {
        d("@@@" + type + "@@@" + m)
        val tmp = channelAdapter?.items?:emptyList()
        binding!!.recyclerView.setAdapter(null)
        binding!!.recyclerView.setLayoutManager(null)
        channelAdapter = null

        channelAdapter = ChannelAdapter(requireContext(), tmp)
        lm = GridLayoutManager(context, m)
        binding!!.recyclerView.setLayoutManager(lm)
        binding!!.recyclerView.setAdapter(channelAdapter)
        channelAdapter?.setOnItemClickListener(this)
        channelAdapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    override fun onResume() {
        super.onResume()
        loadPictures0()
    }


    private fun loadPictures0() {
        //binding.recyclerView.setHasFixedSize(true);

        val m = prefManager!!.getInt(Const.KEY_CHANNEL_COLUMNS)
        lm = GridLayoutManager(context, m)
        binding!!.recyclerView.setLayoutManager(lm)
        binding!!.recyclerView.setAdapter(channelAdapter)


        //wallpaperReference = FirebaseDatabase.getInstance().getReference("Channels");
        fetchWallpapers()
        channelAdapter?.setOnItemClickListener(this)
    }

    fun showInterstitialAd() {
        adNetwork!!.showInterstitialAdNetwork(
            Constant.INTERSTITIAL_POST_LIST,
            adsPref!!.interstitialAdInterval
        )
    }


    private fun fetchWallpapers() {
        val sortOption = prefManager!!.getSortOption()
        presenter!!.getChannelsInPlaylist(playlistId, sortOption, this)
    }

    //    private void fetchWallpapers() {
    //        wallpaperReference.addListenerForSingleValueEvent(new ValueEventListener() {
    //            @Override
    //            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    //                showRefresh(false);
    //                if (dataSnapshot.exists()) {
    //                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {
    //                        try {
    //                            DLog.d("0000000000000000");
    //                            String id = wallpaperSnapshot.child("id").getValue(String.class);
    //                            String wallpaper = wallpaperSnapshot.child(Const.CH_IMG).getValue(String.class);
    //                            String name = wallpaperSnapshot.child(Const.CH_NAME).getValue(String.class);
    //                            String category = wallpaperSnapshot.child(Const.CH_CAT).getValue(String.class);
    //                            String type = wallpaperSnapshot.child(Const.CH_TYPE).getValue(String.class);
    //                            String link = wallpaperSnapshot.child(Const.CH_LNK).getValue(String.class);
    //                            String desc = wallpaperSnapshot.child(Const.CH_DESC).getValue(String.class);
    //                            String language = wallpaperSnapshot.child(Const.CH_LANG).getValue(String.class);
    //
    //                            Channel wallpaper1 = new Channel(id, wallpaper, name, category, type, link, desc, language);
    //                            wallpaperList.add(0, wallpaper1);
    //                        } catch (Exception e) {
    //                            DLog.handleException(e);
    //                        }
    //                    }
    //                    channelAdapter.notifyDataSetChanged();
    //                }
    //            }
    //
    //            @Override
    //            public void onCancelled(@NonNull DatabaseError databaseError) {
    //            }
    //        });
    //    }
    //    private void refreshData() {
    //        wallpaperList.clear();
    //        channelAdapter.notifyDataSetChanged();
    //        new Handler().postDelayed(this::loadPictures0, 2000);
    //    }
    private fun showRefresh(show: Boolean) {
//        binding.swipeRefreshLayout.setRefreshing(false);

//        if (show) {
//            binding.swipeRefreshLayout.setRefreshing(true);
//        } else {
//            new Handler().postDelayed(() -> {
//                binding.swipeRefreshLayout.setRefreshing(false);
//            }, 500);
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun successResult(data: List<Channel>) {
        showRefresh(false)
        //binding.noFavorite.setVisibility(tmp.isEmpty() ? View.VISIBLE : View.GONE);
        this@AllChannelInPlaylistFragment.setBadgeText(THISCLAZZNAME, data.size.toString())
        channelAdapter?.swapData(data)
    }

    override fun errorResult(err: String) {
    }

    override fun onItemClick(view: View, obj: Channel, position: Int) {
        val isDetailsMode = prefManager!!.isDetailsMode
        if (isDetailsMode) {
            val channelId = obj._id
            val intent = DetailsActivity.newInstance(requireContext(), channelId)
            startActivity(intent)
            showInterstitialAd()
        } else {
            val intents = playerIntent(context, obj)
            startActivity(intents)
        }
    }

    companion object {
        private const val ARG_PLAYLIST_ID = "ARG_PLAYLIST_ID"
        fun newInstance(playlistId: Long): AllChannelInPlaylistFragment {
            val fragment = AllChannelInPlaylistFragment()
            val args = Bundle()
            args.putLong(ARG_PLAYLIST_ID, playlistId)
            fragment.setArguments(args)
            return fragment
        }
    }
}