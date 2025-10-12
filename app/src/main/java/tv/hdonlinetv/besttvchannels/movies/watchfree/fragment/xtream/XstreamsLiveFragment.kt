package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.m3u.data.parser.xtream.XtreamInput
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.XtreamPresenter
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentFavoriteBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class XstreamsLiveFragment  //private String action;
    : CaseChannelListFragment() {
    private var binding: FragmentFavoriteBinding? = null

    private val THISCLAZZNAME: String = javaClass.simpleName
    private var prf: PrefManager? = null
    private var presenter: XtreamPresenter? = null
    private var xtreamInput: XtreamInput? = null

    //    public static Fragment newInstance(StreamType streamType) {
    //        Fragment fragment = new XStreamsFragment();
    //        Bundle args = new Bundle();
    //        args.putString(STREAM_TYPE, streamType.getAction());
    //        fragment.setArguments(args);
    //        return fragment;
    //    }
    //    public static FavoritesFragment newInstance(long playlistId) {
    //        FavoritesFragment fragment = new FavoritesFragment();
    //        Bundle args = new Bundle();
    //        args.putLong(ARG_PLAYLIST_ID, playlistId);
    //        fragment.setArguments(args);
    //        return fragment;
    //    }
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())

        if (getArguments() != null) {
            xtreamInput = requireArguments().getSerializable(ARG_XTREAM_INPUT) as XtreamInput?
            presenter = XtreamPresenter(handler, requireContext(), xtreamInput!!)
            prf = PrefManager(getContext())
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
            GridLayoutManager(getContext(), prf!!.getInt(Const.KEY_CHANNEL_COLUMNS))
        binding!!.recyclerView.setLayoutManager(gridLayoutManager)
        channelAdapter!!.setOnItemClickListener(this)
        binding!!.recyclerView.setAdapter(channelAdapter)
        return binding!!.getRoot()
    }

    public override fun onResume() {
        super.onResume()
        //initCheck();
        if (BuildConfig.DEBUG) {
            //Toast.makeText(getContext(), "" + playlistId, Toast.LENGTH_SHORT).show();
        }
        presenter!!.getLiveStreams(xtreamInput!!, this)
    }

    override fun successResult(tmp: List<Channel>) {
        if (tmp != null) {
            binding!!.noFavorite.setVisibility(if (tmp.isEmpty()) View.VISIBLE else View.GONE)
            this.setBadgeText(THISCLAZZNAME, tmp.size.toString())
            channelAdapter!!.swapData(tmp)
        }
    }

    override fun errorResult(err: String) {
    }

    override fun onItemClick(view: View, channel: Channel, position: Int) {
        presenter!!.onStreamItemClick(channel, XtreamPresenter.Action.get_live_streams)
    }


    companion object {
        private const val STREAM_TYPE = "STREAM_TYPE"

        const val ARG_XTREAM_INPUT: String = "arg_xtream_input"

        fun newInstance(xtreamInput: XtreamInput?): XstreamsLiveFragment {
            val fragment = XstreamsLiveFragment()
            val args = Bundle()
            args.putSerializable(ARG_XTREAM_INPUT, xtreamInput)
            fragment.setArguments(args)
            return fragment
        }
    }
}
