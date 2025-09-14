package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.serial

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.m3u.data.parser.xtream.XtreamChannelInfo
import com.walhalla.data.model.Channel
import com.walhalla.data.repository.RepoCallback
import com.walhalla.ui.plugins.Launcher.openBrowser
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist.SerialInfoActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.InfoLayoutBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.DataUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class SerialInfoClass : BaseFragment(), RepoCallback<XtreamChannelInfo.Info>, ChannelAdapter.OnItemClickListener {
    protected val THISCLAZZNAME: String = javaClass.simpleName

    var data: XtreamChannelInfo.Info? = null
    protected var binding: InfoLayoutBinding? = null
    private val prefManager: PrefManager? = null
    private val adsPref: AdsPref? = null
    private val adNetwork: AdNetwork? = null
    private var seriesId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = arguments
        if (intent != null && intent.containsKey(KEY_INTENT_CHANNEL_ID)) {
            data = requireArguments().getSerializable(KEY_INTENT_CHANNEL_ID) as XtreamChannelInfo.Info?
            seriesId = requireArguments().getInt(SerialInfoActivity.KEY_SERIES_ID, -99)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = InfoLayoutBinding.inflate(inflater, container, false)
        binding!!.tvNameText.text = data!!.name
        binding!!.tvCastText.text = data!!.cast
        binding!!.tvCategoryIdText.text = data!!.categoryId
        binding!!.tvDirectorText.text = data!!.director
        binding!!.tvEpisodeRunTimeText.text = data!!.episodeRunTime
        binding!!.tvGenreText.text = data!!.genre
        binding!!.tvLastModifiedText.text =
            DataUtils.convertUnixToDate(
                data!!.lastModified
            )
        binding!!.tvPlotText.text = data!!.plot
        binding!!.tvRatingText.text = data!!.rating
        binding!!.tvRating5basedText.text = data!!.rating5based
        binding!!.tvReleaseDateText.text = data!!.releaseDate

        var m = data!!.youtubeTrailer
        if (!TextUtils.isEmpty(m)) {
            if (!m!!.startsWith("http")) {
                m = "https://www.youtube.com/watch?v=$m"
            }
            binding!!.tvYoutubeTrailerText.text = m
            binding!!.tvYoutubeTrailerText.paintFlags =
                binding!!.tvYoutubeTrailerText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            val finalM = m
            binding!!.tvYoutubeTrailerText.setOnClickListener { v: View? ->
                openBrowser(
                    requireContext(), finalM
                )
            }
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    protected fun loadCategory() {
//        setBadgeText(THISCLAZZNAME, String.valueOf(data.size()));
//        categoriesAdapter.swapData(data);
    }

    override fun successResult(data: XtreamChannelInfo.Info) {
    }

    override fun errorResult(err: String) {
    }

    override fun onItemClick(view: View, obj: Channel, position: Int) {
    }

    companion object {
        protected const val KEY_INTENT_CHANNEL_ID: String = "key_data1"
        fun newInstance(seriesId: Int, data: XtreamChannelInfo.Info?): Fragment {
            val fragment = SerialInfoClass()
            val args = Bundle()
            args.putSerializable(KEY_INTENT_CHANNEL_ID, data)
            args.putSerializable(SerialInfoActivity.KEY_SERIES_ID, seriesId)
            fragment.arguments = args
            return fragment
        }
    }
}
