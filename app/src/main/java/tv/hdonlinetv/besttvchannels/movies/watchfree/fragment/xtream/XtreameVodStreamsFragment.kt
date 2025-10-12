package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.m3u.data.parser.xtream.XtreamInput
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.XtreamPresenter
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentFavoriteBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class XtreameVodStreamsFragment : CaseChannelListFragment() {
    private var binding: FragmentFavoriteBinding? = null

    private val THISCLAZZNAME: String = javaClass.simpleName
    private var prf: PrefManager? = null
    private var presenter: XtreamPresenter? = null


    private var xtreamInput: XtreamInput? = null


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        if (getArguments() != null) {
            xtreamInput = requireArguments().getSerializable(XstreamsLiveFragment.Companion.ARG_XTREAM_INPUT) as XtreamInput?
            presenter = XtreamPresenter(handler, requireContext(), xtreamInput!!)
            prf = PrefManager(context)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        //binding.recyclerView.setHasFixedSize(true);
        val gridLayoutManager = GridLayoutManager(context, prf!!.getInt(Const.KEY_CHANNEL_COLUMNS))
        binding!!.recyclerView.setLayoutManager(gridLayoutManager)
        channelAdapter!!.setOnItemClickListener(this)
        binding!!.recyclerView.setAdapter(channelAdapter)
        return binding!!.getRoot()
    }

    public override fun onResume() {
        super.onResume()
        //initCheck();
//        if (BuildConfig.DEBUG) {
//            Toast.makeText(getContext(), "123", Toast.LENGTH_SHORT).show();
//        }
        presenter!!.getVodStreams(xtreamInput!!, this)
    }

    public override fun successResult(tmp: List<Channel>) {
        binding!!.noFavorite.setVisibility(if (tmp.isEmpty()) View.VISIBLE else View.GONE)
        this.setBadgeText(THISCLAZZNAME, tmp.size.toString())
        channelAdapter!!.swapData(tmp)
    }

    override fun errorResult(err: String) {
        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
    }


    override fun onItemClick(view: View, channel: Channel, position: Int) {
        presenter!!.onStreamItemClick(channel, XtreamPresenter.Action.get_vod_streams)
    }

    companion object {
        fun newInstance(xtreamInput: XtreamInput?): XtreameVodStreamsFragment {
            val fragment = XtreameVodStreamsFragment()
            val args = Bundle()
            args.putSerializable(XstreamsLiveFragment.Companion.ARG_XTREAM_INPUT, xtreamInput)
            fragment.setArguments(args)
            return fragment
        }
    }
}
