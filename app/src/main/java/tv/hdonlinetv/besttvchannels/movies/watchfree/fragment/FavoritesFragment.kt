package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.FavoritePresenter
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.DetailsActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity.Companion.playerIntent
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentFavoriteBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream.CaseChannelListFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class FavoritesFragment : CaseChannelListFragment() {
    private var binding: FragmentFavoriteBinding? = null

    private val THISCLAZZNAME: String = javaClass.getSimpleName()
    private var prf: PrefManager? = null
    private var presenter: FavoritePresenter? = null


    private var playlistId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        presenter = FavoritePresenter(handler, context)
        prf = PrefManager(context)
        if (getArguments() != null) {
            playlistId = requireArguments().getLong(ARG_PLAYLIST_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        //binding.recyclerView.setHasFixedSize(true);
        val gridLayoutManager =
            GridLayoutManager(context, prf!!.getInt(Const.KEY_CHANNEL_COLUMNS))
        binding!!.recyclerView.setLayoutManager(gridLayoutManager)
        channelAdapter?.setOnItemClickListener(this)
        binding!!.recyclerView.setAdapter(channelAdapter)
        return binding!!.getRoot()
    }

    override fun onResume() {
        super.onResume()
        //initCheck();
        if (BuildConfig.DEBUG) {
            //Toast.makeText(getContext(), "" + playlistId, Toast.LENGTH_SHORT).show();
        }
        presenter!!.getFavorite(playlistId, this)
    }


    //    private void initCheck() {
    //        if (prf.loadNightModeState()) {
    //            Log.d("Dark", "MODE");
    //        } else {
    //            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // set status text dark
    //        }
    //    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onItemClick(view: View, channel: Channel, position: Int) {
        val isDetailsMode = prf!!.isDetailsMode
        if (isDetailsMode) {
            val channelId = channel._id
            val intent = DetailsActivity.newInstance(requireContext(), channelId)
            startActivity(intent)
            //@@@showInterstitialAd();
        } else {
            val intents = playerIntent(context, channel)
            startActivity(intents)
        }
    }

    override fun successResult(tmp: List<Channel>) {
        binding!!.noFavorite.visibility = if (tmp.isEmpty()) View.VISIBLE else View.GONE
        this@FavoritesFragment.setBadgeText(THISCLAZZNAME, tmp.size.toString())
        channelAdapter?.swapData(tmp)
    }

    override fun errorResult(err: String) {
    }

    companion object {
        private const val ARG_PLAYLIST_ID = "ARG_PLAYLIST_ID"
        fun newInstance(playlistId: Long): FavoritesFragment {
            val fragment = FavoritesFragment()
            val args = Bundle()
            args.putLong(ARG_PLAYLIST_ID, playlistId)
            fragment.setArguments(args)
            return fragment
        }
    }
}
