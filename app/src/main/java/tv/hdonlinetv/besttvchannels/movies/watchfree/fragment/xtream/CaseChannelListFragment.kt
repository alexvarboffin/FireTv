package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream

import android.os.Bundle
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.RepoCallback
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment

abstract class CaseChannelListFragment : BaseFragment(), RepoCallback<List<Channel>>, ChannelAdapter.OnItemClickListener {
    @JvmField
    protected var channelAdapter: ChannelAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelAdapter = ChannelAdapter(requireContext(), ArrayList())
    }
}
